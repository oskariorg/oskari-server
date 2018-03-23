package org.oskari.map.userlayer.input;

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

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        ShapefileDataStore store = null;
        try {
            store = new ShapefileDataStore(file.toURI().toURL());
            store.setCharset(getCharset(file));
            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);
            CoordinateReferenceSystem crs = source.getSchema()
                    .getGeometryDescriptor()
                    .getCoordinateReferenceSystem();
            if (crs != null) {
                sourceCRS = crs;
            }
            return FeatureCollectionParsers.read(source, sourceCRS, targetCRS);
        } catch (Exception e) {
            throw new ServiceException("Failed to parse SHP", e);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    private Charset getCharset(File file) {
        String pathShp = file.getAbsolutePath();
        int i = pathShp.lastIndexOf('.');
        String pathCpg = pathShp.substring(0, i) + ".cpg";
        Path path = Paths.get(pathCpg);
        if (!Files.exists(path)) {
            return StandardCharsets.ISO_8859_1;
        }
        try {
            byte[] b = Files.readAllBytes(path);
            String csName = new String(b, StandardCharsets.US_ASCII).trim();
            LOG.debug("Charset name in CPG file:", csName);
            try {
                return Charset.forName(csName);
            } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                LOG.warn(csName, "was invalid");
            }
        } catch (IOException e) {
            LOG.warn("IOException occured while reading CPG file, using default charset");
        }
        return StandardCharsets.ISO_8859_1;
    }

}
