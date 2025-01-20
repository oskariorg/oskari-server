package org.oskari.service.wfs3;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONFeatureCollection;

import java.util.ArrayList;
import java.util.List;

public class CoordinateTransformer {

    private final CoordinateReferenceSystem from;
    private final CoordinateReferenceSystem to;
    private final MathTransform transform;

    public CoordinateTransformer(String from, String to) throws NoSuchAuthorityCodeException, FactoryException {
        this(CRS.decode(from, true), CRS.decode(to, true));
    }

    public CoordinateTransformer(CoordinateReferenceSystem from, CoordinateReferenceSystem to) throws FactoryException {
        this.from = from;
        this.to = to;
        boolean needsTransform = !CRS.equalsIgnoreMetadata(from, to);
        this.transform = needsTransform ? CRS.findMathTransform(from, to) : null;
    }

    public CoordinateReferenceSystem getA() {
        return from;
    }

    public CoordinateReferenceSystem getB() {
        return to;
    }

    public boolean needsTransform() {
        return transform != null;
    }

    public SimpleFeatureCollection transform(SimpleFeatureCollection sfc)
            throws MismatchedDimensionException, TransformException {
        if (sfc == null || sfc.isEmpty() || !needsTransform()) {
            return sfc;
        }

        SimpleFeatureType newSchema = SimpleFeatureTypeBuilder.retype(sfc.getSchema(), to);
        List<SimpleFeature> fc = new ArrayList<>();
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                // need to create a new builder for each feature because of (possibly) varying geometry types.
                SimpleFeatureBuilder b = new SimpleFeatureBuilder(f.getFeatureType());
                for (int i = 0; i < newSchema.getAttributeCount(); i++) {
                    AttributeDescriptor ad = newSchema.getDescriptor(i);
                    if (ad.getLocalName().equals(GeoJSON.GEOMETRY)) {
                        b.set(i, f.getFeatureType().getDescriptor(GeoJSON.GEOMETRY));
                    } else {
                        b.set(i, f.getAttribute(ad.getLocalName()));
                    }
                }
                SimpleFeature copy = b.buildFeature(f.getID());
                Object g = f.getDefaultGeometry();
                if (g != null) {
                    Geometry transformed = JTS.transform((Geometry) g, transform);
                    copy.setDefaultGeometry(transformed);
                }
                fc.add(copy);
            }
        }
        return new GeoJSONFeatureCollection(fc, newSchema);
    }

}
