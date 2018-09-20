package org.oskari.service.mvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.geom.util.GeometryEditor;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;
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
        Geometry tileEnvelopeGeom = GF.toGeometry(tileEnvelope);
        Geometry tileClipGeom = GF.toGeometry(clipEnvelope);
        RectangleIntersects rectIntersects = new RectangleIntersects((Polygon) tileEnvelopeGeom);

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

                geom = TopologyPreservingSimplifier.simplify(geom, resHalf);
                if (geom.isEmpty()) {
                    continue;
                }

                geom = within(geom, tileEnvelope, rectIntersects);
                if (geom == null) {
                    continue;
                }

                if (buffer > 0) {
                    try {
                        geom = tileClipGeom.intersection(geom);
                        if (geom.isEmpty()) {
                            continue;
                        }
                        geom = within(geom, tileEnvelope, rectIntersects);
                        if (geom == null) {
                            continue;
                        }
                    } catch (TopologyException ignore) {
                        continue;
                    }
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

    private static Geometry within(Geometry geom, Envelope rect, RectangleIntersects rectIntersects) {
        if (geom instanceof Point) {
            Coordinate c = ((Point) geom).getCoordinate();
            boolean within = c.x >= rect.getMinX() && c.x <= rect.getMaxX() && c.y >= rect.getMinY() && c.y <= rect.getMaxY();
            return within ? geom : null;
        }

        if (geom instanceof LineString) {
            return rectIntersects.intersects(geom) ? geom : null;
        }

        if (geom instanceof Polygon) {
            Polygon polygon = (Polygon) geom;
            LinearRing exterior = (LinearRing) polygon.getExteriorRing();
            if (!rectIntersects.intersects(exterior)) {
                return null;
            }
            int numInteriorRing = polygon.getNumInteriorRing();
            if (numInteriorRing == 0) {
                return geom;
            }
            LinearRing[] interiorRings = new LinearRing[numInteriorRing];
            int n = 0;
            for (int i = 0; i < numInteriorRing; i++) {
                LinearRing interiorRing = (LinearRing) polygon.getInteriorRingN(i);
                if (rectIntersects.intersects(interiorRing)) {
                    interiorRings[n++] = interiorRing;
                }
            }
            if (n == 0) {
                return GF.createPolygon(exterior);
            }
            if (n == numInteriorRing) {
                return geom;
            }
            return GF.createPolygon(exterior, Arrays.copyOf(interiorRings, n));
        }

        if (geom instanceof MultiPoint) {
            MultiPoint multi = (MultiPoint) geom;
            int num = multi.getNumGeometries();
            Point[] subGeomsWithin = new Point[num];
            int n = 0;
            for (int i = 0; i < num; i++) {
                Geometry subGeom = within(multi.getGeometryN(i), rect, rectIntersects);
                if (subGeom != null) {
                    subGeomsWithin[n++] = (Point) subGeom;
                }
            }
            if (n == 0) {
                return null;
            }
            if (n == 1) {
                return subGeomsWithin[0];
            }
            if (n == num) {
                return geom;
            }
            return GF.createMultiPoint(Arrays.copyOf(subGeomsWithin, n));
        }

        if (geom instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geom;
            int num = multi.getNumGeometries();
            LineString[] subGeomsWithin = new LineString[num];
            int n = 0;
            for (int i = 0; i < num; i++) {
                Geometry subGeom = within(multi.getGeometryN(i), rect, rectIntersects);
                if (subGeom != null) {
                    subGeomsWithin[n++] = (LineString) subGeom;
                }
            }
            if (n == 0) {
                return null;
            }
            if (n == 1) {
                return subGeomsWithin[0];
            }
            if (n == num) {
                return geom;
            }
            return GF.createMultiLineString(Arrays.copyOf(subGeomsWithin, n));
        }

        if (geom instanceof MultiPolygon) {
            MultiPolygon multi = (MultiPolygon) geom;
            int num = multi.getNumGeometries();
            Polygon[] subGeomsWithin = new Polygon[num];
            int n = 0;
            for (int i = 0; i < num; i++) {
                Geometry subGeom = within(multi.getGeometryN(i), rect, rectIntersects);
                if (subGeom != null) {
                    subGeomsWithin[n++] = (Polygon) subGeom;
                }
            }
            if (n == 0) {
                return null;
            }
            if (n == 1) {
                return subGeomsWithin[0];
            }
            if (n == num) {
                return geom;
            }
            return GF.createMultiPolygon(Arrays.copyOf(subGeomsWithin, n));
        }

        // TODO handle
        return null;
    }

}
