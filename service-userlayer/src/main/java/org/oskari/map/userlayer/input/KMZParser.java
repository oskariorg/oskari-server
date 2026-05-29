package org.oskari.map.userlayer.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.oskari.map.userlayer.service.UserLayerException;

import fi.nls.oskari.service.ServiceException;

/**
 * KMZ is a ZIP archive containing a KML file (typically doc.kml) plus optional
 * resources. Extracts the first .kml entry and delegates to {@link KMLParser}.
 */
public class KMZParser implements FeatureCollectionParser {

    public static final String SUFFIX = "KMZ";

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        File kmlFile = null;
        try {
            kmlFile = extractKml(file);
            return new KMLParser().parse(kmlFile, sourceCRS, targetCRS);
        } finally {
            if (kmlFile != null) {
                kmlFile.delete();
            }
        }
    }

    private File extractKml(File kmzFile) throws UserLayerException {
        try (InputStream in = Files.newInputStream(kmzFile.toPath());
                ZipInputStream zis = new ZipInputStream(in, StandardCharsets.UTF_8)) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                if (ze.getName().toLowerCase().endsWith(".kml")) {
                    File out = File.createTempFile("kmz-", ".kml");
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        zis.transferTo(fos);
                    }
                    return out;
                }
            }
            throw new UserLayerException("KMZ file does not contain a .kml entry",
                    UserLayerException.ErrorType.NO_FILE);
        } catch (IOException e) {
            throw new UserLayerException("Failed to read KMZ file: " + e.getMessage(),
                    UserLayerException.ErrorType.INVALID_ZIP);
        }
    }
}
