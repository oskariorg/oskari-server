package org.oskari.service.mvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.util.GeometryEditor;
import org.locationtech.jts.operation.predicate.RectangleIntersects;
import org.locationtech.jts.simplify.VWSimplifier;
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

    /* Not currently used - this uses asMVTGeoms() is an optimized version of this function
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
                new MvtLayerParams(256, extent), new GeomMinSizeFilter(6, 6)).mvtGeoms;
    }
     */

    public static List<Geometry> asMVTGeoms(SimpleFeatureCollection sfc, double[] bbox, int extent, int buffer) {
        if (sfc.isEmpty()) {
            return Collections.emptyList();
        }

        Envelope tileEnvelope = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        Envelope clipEnvelope = new Envelope(tileEnvelope);
        if (buffer > 0) {
            double bufferSizePercent = (double) buffer / extent;
            double deltaX = bufferSizePercent * tileEnvelope.getWidth();
            double deltaY = bufferSizePercent * tileEnvelope.getHeight();
            clipEnvelope.expandBy(deltaX, deltaY);
        }

        RectangleIntersects tileIntersects = new RectangleIntersects((Polygon) GF.toGeometry(tileEnvelope));

        Envelope mvtBufferedEnvelope = new Envelope(-buffer, extent + buffer, -buffer, extent + buffer);
        Geometry mvtClipGeom = GF.toGeometry(mvtBufferedEnvelope);

        double translateX = tileEnvelope.getMinX();
        double translateY = tileEnvelope.getMaxY();
        double scaleX = (double) extent / tileEnvelope.getWidth();
        double scaleY = -((double) extent / tileEnvelope.getHeight());
        ToMVTSpace snapToGrid = new ToMVTSpace(translateX, translateY, scaleX, scaleY);

        GeometryEditor editor = new GeometryEditor(GF);

        List<Geometry> mvtGeoms = new ArrayList<>();
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature sf = it.next();
                Geometry geom = (Geometry) sf.getDefaultGeometry();
                if (geom == null || geom.isEmpty()) {
                    continue;
                }

                geom = multiGeometriesWithOneGeometryToSingle(geom);

                if (geom instanceof Point) {
                    // Check that clipEnvelope (buffered) and the geometry's envelope are not disjoint
                    if (!clipEnvelope.intersects(geom.getEnvelopeInternal())) {
                        continue;
                    }
                } else if (geom instanceof MultiPoint) {
                    // Check that clipEnvelope (buffered) and the geometry's envelope are not disjoint
                    if (!clipEnvelope.intersects(geom.getEnvelopeInternal())) {
                        continue;
                    }
                    geom = removePointsOutsideOfEnvelope((MultiPoint) geom, clipEnvelope);
                } else {
                    // Check that tileEnvelope and geometry's envelope are not disjoint
                    if (!tileEnvelope.intersects(geom.getEnvelopeInternal())) {
                        continue;
                    }

                    // Remove parts of the geometry that are disjoint with our tileEnvelope
                    geom = notDisjoint(tileIntersects, geom);
                    if (geom == null || geom.isEmpty()) {
                        continue;
                    }
                }

                // Snap the geometry to MVT grid (integer coordinates)
                geom = editor.edit(geom, snapToGrid);
                if (geom == null || geom.isEmpty()) {
                    // Which might make the geometry disappear (for example LineString collapsed to a Point)
                    continue;
                }

                geom = multiGeometriesWithOneGeometryToSingle(geom);

                if (!(geom instanceof Point || geom instanceof MultiPoint)) {
                    geom = VWSimplifier.simplify(geom, 0.5);
                    try {
                        // Calculate the intersection with our buffered envelope
                        geom = mvtClipGeom.intersection(geom);
                        if (geom == null || geom.isEmpty()) {
                            // Which might not exist - skip the geometry
                            continue;
                        }
                    } catch (TopologyException ignore) {
                        // Calculating the intersection failed
                        continue;
                    }
                }

                geom.setUserData(sf);
                mvtGeoms.add(geom);
            }
        }
        return mvtGeoms;
    }

    private static Geometry notDisjoint(RectangleIntersects rectIntersects, Geometry geom) {
        if (geom instanceof LineString) {
            return rectIntersects.intersects(geom) ? geom : null;
        }

        if (geom instanceof Polygon) {
            Polygon polygon = (Polygon) geom;
            LinearRing exterior = (LinearRing) polygon.getExteriorRing();
            Polygon exteriorRingAsPolygon = GF.createPolygon(exterior);
            if (!rectIntersects.intersects(exteriorRingAsPolygon)) {
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
                if (rectIntersects.intersects(GF.createPolygon(interiorRing))) {
                    interiorRings[n++] = interiorRing;
                }
            }
            if (n == 0) {
                return exteriorRingAsPolygon;
            }
            if (n == numInteriorRing) {
                return geom;
            }
            return GF.createPolygon(exterior, Arrays.copyOf(interiorRings, n));
        }

        if (geom instanceof MultiLineString) {
            MultiLineString multi = (MultiLineString) geom;
            int num = multi.getNumGeometries();
            LineString[] subGeomsWithin = new LineString[num];
            int n = 0;
            for (int i = 0; i < num; i++) {
                Geometry subGeom = notDisjoint(rectIntersects, multi.getGeometryN(i));
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
                Geometry subGeom = notDisjoint(rectIntersects, multi.getGeometryN(i));
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

        return null;
    }

    private static Geometry multiGeometriesWithOneGeometryToSingle(Geometry geom) {
        if (geom instanceof GeometryCollection) {
            GeometryCollection c = (GeometryCollection) geom;
            if (c.getNumGeometries() == 1) {
                return c.getGeometryN(0);
            }
        }
        return geom;
    }

    private static Geometry removePointsOutsideOfEnvelope(MultiPoint mp, Envelope envelope) {
        BitSet indexes = new BitSet(mp.getNumGeometries());
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            Point p = (Point) mp.getGeometryN(i);
            if (envelope.contains(p.getCoordinate())) {
                indexes.set(i);
            }
        }
        int len = indexes.length();
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return mp.getGeometryN(indexes.nextSetBit(0));
        }
        if (len == mp.getNumGeometries()) {
            return mp;
        }
        Point[] points = new Point[len];
        int j = 0;
        for (int i = indexes.nextSetBit(0); i >= 0; i = indexes.nextSetBit(i+1)) {
            points[j++] = (Point) mp.getGeometryN(i);
        }
        return GF.createMultiPoint(points);
    }

}
