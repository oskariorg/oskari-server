package org.oskari.map.myfeatures.input;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.locationtech.jts.geom.Geometry;
import org.oskari.map.myfeatures.service.MyFeaturesException;

import fi.nls.oskari.service.ServiceException;

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
        case GPKGParser.SUFFIX:
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
        case GPKGParser.SUFFIX: return new GPKGParser();
        default: return null;
        }
    }

    /**
     * Read Features from FeatureSource to memory (ensure they won't
     * disappear when DataStore#dispose() is called) while transforming
     * their geometries from source projection to target projection
     * @throws MyFeaturesException lots can go wrong
     */
    public static SimpleFeatureCollection read(SimpleFeatureSource src,
            CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws ServiceException, MyFeaturesException {
        MathTransform transform = getTransform(sourceCRS, targetCRS);
        try {
            SimpleFeatureType newSchema = SimpleFeatureTypeBuilder.retype(src.getSchema(), targetCRS);
            SimpleFeatureBuilder b = new SimpleFeatureBuilder(newSchema);
            DefaultFeatureCollection fc = new DefaultFeatureCollection(null, newSchema);
            SimpleFeatureCollection sfc = src.getFeatures();

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
        } catch (Exception e) {
            throw new MyFeaturesException("Failed to read feature collection from source: " + e.getMessage(),
                        MyFeaturesException.ErrorType.PARSER, MyFeaturesException.ErrorType.INVALID_FORMAT);
        }
    }

    public static MathTransform getTransform(
            CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws MyFeaturesException, ServiceException {
        if (sourceCRS == null) {
            throw new MyFeaturesException("sourceCRS must be known!",
                    MyFeaturesException.ErrorType.PARSER, MyFeaturesException.ErrorType.NO_SOURCE_EPSG);
        }
        if (targetCRS == null) {
            throw new ServiceException("targetCRS isn't configured in Oskari properties");
        }
        try {
            return CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new ServiceException("Failed to find math transform for: " + sourceCRS + " to: " + targetCRS);
        }
    }

}
