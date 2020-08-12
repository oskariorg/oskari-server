package org.oskari.service.wfs3;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSONFeatureCollection;

import org.locationtech.jts.geom.Geometry;

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
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(newSchema);
        List<SimpleFeature> fc = new ArrayList<>();
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                for (int i = 0; i < newSchema.getAttributeCount(); i++) {
                    AttributeDescriptor ad = newSchema.getDescriptor(i);
                    b.set(i, f.getAttribute(ad.getLocalName()));
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
