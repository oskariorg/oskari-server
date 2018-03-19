package org.oskari.service.userlayer.input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

/**
 * Parse ESRI ShapeFiles with GeoTools
 */
public class SHPParser implements FeatureCollectionParser {

    private static final Logger LOG = LogFactory .getLogger(SHPParser.class);
    
    public static final String SUFFIX = "SHP";
    
    private CoordinateReferenceSystem crs;

    @Override
    public SimpleFeatureCollection parse(File file) throws ServiceException {
        ShapefileDataStore store = null;
        try {
            store = new ShapefileDataStore(file.toURI().toURL());
            store.setCharset(getCharset(file));
            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);
            SimpleFeatureType schema = source.getSchema();
            crs = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
            return source.getFeatures();
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    @Override
    public CoordinateReferenceSystem getDeterminedProjection() {
        return crs;
    }

    private Charset getCharset(File file) {
        String pathShp = file.getAbsolutePath();
        int i = pathShp.lastIndexOf('.');
        String pathCpg = pathShp.substring(0, i) + ".cpg";
        Path path = Paths.get(pathCpg);
        if (!Files.exists(path)) {
            return StandardCharsets.ISO_8859_1;
        }
        String csName = null;
        try {
            byte[] b = Files.readAllBytes(path);
            csName = new String(b, StandardCharsets.US_ASCII).trim();
            LOG.debug("Charset name in CPG file:", csName);
            return Charset.forName(csName);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            LOG.warn(csName, "was invalid");
        } catch (IOException e) {
            LOG.warn("IOException occured while reading CPG file, using default charset");
        }
        return StandardCharsets.ISO_8859_1;
    }
    
}
