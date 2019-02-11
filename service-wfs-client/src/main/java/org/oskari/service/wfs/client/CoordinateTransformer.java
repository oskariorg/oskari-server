package org.oskari.service.wfs.client;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class CoordinateTransformer {

    private final CoordinateReferenceSystem a;
    private final CoordinateReferenceSystem b;
    private final boolean needsTransform;
    private final MathTransform fromAtoB;
    private final MathTransform fromBtoA;

    public CoordinateTransformer(String a, String b) throws NoSuchAuthorityCodeException, FactoryException {
        this.a = CRS.decode(a, true);
        this.b = CRS.decode(b, true);
        this.needsTransform = !CRS.equalsIgnoreMetadata(this.a, this.b);
        if (needsTransform) {
            fromAtoB = CRS.findMathTransform(this.a, this.b, true);
            fromBtoA = CRS.findMathTransform(this.b, this.a, true);
        } else {
            fromAtoB = null;
            fromBtoA = null;
        }
    }

    public Envelope transformToB(Envelope envelope)
            throws TransformException {
        return transform(envelope, fromAtoB);
    }

    public Envelope transformToA(Envelope envelope)
            throws TransformException {
        return transform(envelope, fromBtoA);
    }

    private Envelope transform(Envelope envelope, MathTransform transform)
            throws TransformException {
        if (!needsTransform) {
            return envelope;
        }
        return JTS.transform(envelope, transform);
    }

    public SimpleFeatureCollection transformToB(SimpleFeatureCollection sfc)
            throws MismatchedDimensionException, TransformException {
        return transform(sfc, fromAtoB, b);
    }

    public SimpleFeatureCollection transformToA(SimpleFeatureCollection sfc)
            throws MismatchedDimensionException, TransformException {
        return transform(sfc, fromBtoA, a);
    }

    private SimpleFeatureCollection transform(SimpleFeatureCollection sfc,
            MathTransform t, CoordinateReferenceSystem crs)
                    throws MismatchedDimensionException, TransformException {
        if (sfc == null || sfc.isEmpty() || !needsTransform) {
            return sfc;
        }
        SimpleFeatureType newSchema = SimpleFeatureTypeBuilder.retype(sfc.getSchema(), crs);
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(newSchema);
        DefaultFeatureCollection fc = new DefaultFeatureCollection(null, newSchema);
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                for (int i = 0; i < f.getAttributeCount(); i++) {
                    b.set(i, f.getAttribute(i));
                }
                SimpleFeature copy = b.buildFeature(f.getID());
                Object g = f.getDefaultGeometry();
                if (g != null) {
                    Geometry transformed = JTS.transform((Geometry) g, t);
                    copy.setDefaultGeometry(transformed);
                }
                fc.add(copy);
            }
        }
        return fc;
    }

}
