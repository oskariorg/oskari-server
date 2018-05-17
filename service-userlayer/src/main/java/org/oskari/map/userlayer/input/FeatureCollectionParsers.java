package org.oskari.map.userlayer.input;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureCollectionParsers {

    private FeatureCollectionParsers() {}

    public static boolean hasByFileExt(String fileExt) {
        if (fileExt == null) {
            return false;
        }
        fileExt = fileExt.toUpperCase();
        switch (fileExt) {
        case GPXParser.SUFFIX:
        case KMLParser.SUFFIX:
        case MIFParser.SUFFIX:
        case SHPParser.SUFFIX:
            return true;
        default:
            return false;
        }
    }

    public static FeatureCollectionParser getByFileExt(String fileExt) {
        if (fileExt == null) {
            return null;
        }
        fileExt = fileExt.toUpperCase();
        switch (fileExt) {
        case GPXParser.SUFFIX: return new GPXParser();
        case KMLParser.SUFFIX: return new KMLParser();
        case MIFParser.SUFFIX: return new MIFParser();
        case SHPParser.SUFFIX: return new SHPParser();
        default: return null;
        }
    }

    /**
     * Read Features from FeatureSource to memory (ensure they won't
     * disappear when DataStore#dispose() is called) while transforming
     * their geometries from source projection to target projection
     * @throws Exception lots can go wrong
     */
    public static SimpleFeatureCollection read(SimpleFeatureSource src,
            CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws Exception {
        SimpleFeatureType newSchema = SimpleFeatureTypeBuilder.retype(src.getSchema(), targetCRS);
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(newSchema);
        DefaultFeatureCollection fc = new DefaultFeatureCollection(null, newSchema);
        SimpleFeatureCollection sfc = src.getFeatures();
        MathTransform transform = getTransform(sourceCRS, targetCRS);
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                for (int i = 0; i < f.getAttributeCount(); i++) {
                    b.set(i, f.getAttribute(i));
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
        return fc;
    }

    private static MathTransform getTransform(
            CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws IllegalArgumentException, FactoryException {
        if (sourceCRS == null || targetCRS == null) {
            throw new IllegalArgumentException("Both sourceCRS and targetCRS must be known!");
        }
        return CRS.findMathTransform(sourceCRS, targetCRS);
    }

}
