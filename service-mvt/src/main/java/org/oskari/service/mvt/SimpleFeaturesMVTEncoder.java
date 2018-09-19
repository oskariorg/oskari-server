package org.oskari.service.mvt;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

public class SimpleFeaturesMVTEncoder {

    private static final GeometryFactory GF = new GeometryFactory();

    public static byte[] encodeToByteArray(SimpleFeatureCollection sfc,
            String layer, double[] bbox, int extent, int buffer) {
        return encode(sfc, layer, bbox, extent, buffer).toByteArray();
    }

    public static VectorTile.Tile encode(SimpleFeatureCollection sfc,
            String layer, double[] bbox, int extent, int buffer) {
        Envelope tileEnvelope = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        Envelope clipEnvelope = new Envelope(tileEnvelope);
        if (buffer > 0) {
            double bufferSizePercent = (double) buffer / extent;
            double dx = bufferSizePercent * tileEnvelope.getWidth();
            double dy = bufferSizePercent * tileEnvelope.getHeight();
            clipEnvelope.expandBy(dx, dy);
        }
        Geometry tileClipGeom = GF.toGeometry(clipEnvelope);

        double tx = tileEnvelope.getMinX();
        double ty = tileEnvelope.getMaxY();
        double sx = (double) extent / tileEnvelope.getWidth();
        double sy = -1.0 * extent / tileEnvelope.getHeight();
        SnapToGrid snapToGrid = new SnapToGrid(tx, ty, sx, sy);

        List<Geometry> mvtGeoms = new ArrayList<>();
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature sf = it.next();
                Object g = sf.getDefaultGeometry();
                if (g == null) {
                    continue;
                }

                Geometry geom = (Geometry) g;

                // Quickly check that atleast the envelopes intersect
                if (!tileEnvelope.intersects(geom.getEnvelopeInternal())) {
                    continue;
                }

                // Clip
                Geometry clipped;
                try {
                    clipped = tileClipGeom.intersection(geom);
                    if (clipped.isEmpty()) {
                        continue;
                    }
                } catch (TopologyException e) {
                    // Ignore
                    continue;
                }

                clipped.apply(snapToGrid);

                if (clipped.getArea() > 0 && clipped.getArea() < 6) {
                    // Drop very small polygons
                    // Note that this is not 6px^2 but 6 square units in MVT space
                    continue;
                }

                if (clipped.getLength() > 0 && clipped.getLength() < 6) {
                    // Drop very small lines
                    // Note that this is not 6px but 6 units in MVT space
                    // With tileSize 512 6 units corresponds to 0.75px
                }

                // Simplify transformed geometry (6 should be fine with the 4096 extent)
                // TODO: Figure out if it's better to simplify before calculating the intersection
                // This is quite handy here, because it removes duplicated points,
                // though it also would be trivial to ignore them in the actual encoding phase 
                Geometry simplified = TopologyPreservingSimplifier.simplify(clipped, 6);
                
                simplified.setUserData(sf);
                mvtGeoms.add(simplified);
            }
        }

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

}
