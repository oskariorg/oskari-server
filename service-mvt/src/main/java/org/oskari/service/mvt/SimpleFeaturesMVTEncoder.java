package org.oskari.service.mvt;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.geom.util.GeometryEditor;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.GeomMinSizeFilter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

public class SimpleFeaturesMVTEncoder {

    private static final GeometryFactory GF = new GeometryFactory();

    public static byte[] encodeToByteArray(SimpleFeatureCollection sfc,
            String layer, double[] bbox, int extent, int buffer) {
        return encode(sfc, layer, bbox, extent, buffer).toByteArray();
    }

    public static VectorTile.Tile encode(SimpleFeatureCollection sfc,
            String layer, double[] bbox, int extent, int buffer) {
        List<Geometry> mvtGeoms = asMVTGeoms(sfc, bbox, extent, buffer);

        VectorTile.Tile.Layer.Builder layerBuilder = VectorTile.Tile.Layer.newBuilder();
        layerBuilder.setVersion(2);
        layerBuilder.setName(layer);
        layerBuilder.setExtent(extent);

        MvtLayerProps layerProps = new MvtLayerProps();
        IUserDataConverter converter = new SimpleFeatureConverter();
        List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(mvtGeoms, layerProps, converter);

        layerBuilder.addAllFeatures(features);
        MvtLayerBuild.writeProps(layerBuilder, layerProps);

        VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();
        tileBuilder.addLayers(layerBuilder.build());

        return tileBuilder.build();
    }

    public static List<Geometry> asMVTGeoms2(SimpleFeatureCollection sfc, double[] bbox, int extent, int buffer) {
        Envelope tileEnvelope = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        Envelope clipEnvelope = new Envelope(tileEnvelope);
        if (buffer > 0) {
            double bufferSizePercent = (double) buffer / extent;
            double dx = bufferSizePercent * tileEnvelope.getWidth();
            double dy = bufferSizePercent * tileEnvelope.getHeight();
            clipEnvelope.expandBy(dx, dy);
        }

        List<Geometry> geoms = new ArrayList<>();
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature sf = it.next();
                Object g = sf.getDefaultGeometry();
                if (g == null) {
                    continue;
                }

                Geometry geom = (Geometry) g;
                geom.setUserData(sf);
                geoms.add(geom);
            }
        }

        return JtsAdapter.createTileGeom(geoms, tileEnvelope, clipEnvelope, GF,
                new MvtLayerParams(512, extent), new GeomMinSizeFilter(6, 6)).mvtGeoms;
    }

    public static List<Geometry> asMVTGeoms(SimpleFeatureCollection sfc, double[] bbox, int extent, int buffer) {
        Envelope tileEnvelope = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        Envelope clipEnvelope = new Envelope(tileEnvelope);
        if (buffer > 0) {
            double bufferSizePercent = (double) buffer / extent;
            double dx = bufferSizePercent * tileEnvelope.getWidth();
            double dy = bufferSizePercent * tileEnvelope.getHeight();
            clipEnvelope.expandBy(dx, dy);
        }
        Geometry tileClipGeom = GF.toGeometry(clipEnvelope);

        double res = tileEnvelope.getWidth() / extent;
        double resHalf = res / 2;

        double tx = tileEnvelope.getMinX();
        double ty = tileEnvelope.getMaxY();
        double sx = (double) extent / tileEnvelope.getWidth();
        double sy = -((double) extent / tileEnvelope.getHeight());
        ToMVTSpace snapToGrid = new ToMVTSpace(tx, ty, sx, sy);
        GeometryEditor editor = new GeometryEditor(GF);

        List<Geometry> mvtGeoms = new ArrayList<>();
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature sf = it.next();
                Object g = sf.getDefaultGeometry();
                if (g == null) {
                    continue;
                }
                Geometry geom = (Geometry) g;
                if (geom.isEmpty()) {
                    continue;
                }
                Envelope geomEnvelope = geom.getEnvelopeInternal();

                // Quickly check that atleast the envelopes intersect
                if (!tileEnvelope.intersects(geom.getEnvelopeInternal())) {
                    continue;
                }

                if (geom instanceof LineString
                        || geom instanceof Polygon
                        || geom instanceof MultiLineString
                        || geom instanceof MultiPolygon) {
                    double bboxWidth = geomEnvelope.getWidth();
                    double bboxHeight = geomEnvelope.getHeight();
                    if (bboxWidth * bboxWidth + bboxHeight * bboxHeight < resHalf * resHalf) {
                        continue;
                    }
                }

                // TODO: Remove repeated points with tolerance of resHalf

                geom = TopologyPreservingSimplifier.simplify(geom, resHalf);
                if (geom.isEmpty()) {
                    continue;
                }

                try {
                    geom = tileClipGeom.intersection(geom);
                } catch (TopologyException ignore) {
                    continue;
                }

                if (geom.isEmpty()) {
                    continue;
                }

                geom = editor.edit(geom, snapToGrid);
                if (geom.isEmpty()) {
                    continue;
                }

                geom.setUserData(sf);
                mvtGeoms.add(geom);
            }
        }
        return mvtGeoms;
    }

}
