package org.oskari.map.userlayer.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.gpx.GPX;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.gpx.gpx10.GPX10;
import org.geotools.gpx.gpx10.GPX10Configuration;
import org.geotools.referencing.CRS;
import org.geotools.xsd.PullParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.map.userlayer.service.UserLayerException;
import org.xml.sax.SAXException;

import fi.nls.oskari.service.ServiceException;

public class GPXParser implements FeatureCollectionParser {

    public static final String SUFFIX = "GPX";

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        try {
            // GPX always lon,lat 4326
            sourceCRS = CRS.decode("EPSG:4326", true);
        } catch (FactoryException e) {
            throw new ServiceException("Failed to decode sourceCrs (EPSG:4326) for GPXParser");
        }

        try {
            SimpleFeatureCollection parsed;
            // Tracks > Routes > Waypoints
            parsed = parse(file, GPX.trkType);
            if (!parsed.isEmpty()) {
                return new ReprojectingFeatureCollection(parsed, sourceCRS, targetCRS);
            }
            parsed = parse10(file, GPX10.trkType);
            if (!parsed.isEmpty()) {
                return new ReprojectingFeatureCollection(parsed, sourceCRS, targetCRS);
            }

            parsed = parse(file, GPX.rteType);
            if (!parsed.isEmpty()) {
                return new ReprojectingFeatureCollection(parsed, sourceCRS, targetCRS);
            }
            parsed = parse10(file, GPX10.rteType);
            if (!parsed.isEmpty()) {
                return new ReprojectingFeatureCollection(parsed, sourceCRS, targetCRS);
            }

            parsed = parse(file, GPX.wptType);
            if (!parsed.isEmpty()) {
                return new ReprojectingFeatureCollection(parsed, sourceCRS, targetCRS);
            }
            parsed = parse10(file, GPX10.wptType);
            if (!parsed.isEmpty()) {
                return new ReprojectingFeatureCollection(parsed, sourceCRS, targetCRS);
            }

            // Return the empty FeatureCollection
            return parsed;
        } catch (Exception e) {
            throw new UserLayerException("Failed to parse GPX: " + e.getMessage(),
                    UserLayerException.ErrorType.PARSER, UserLayerException.ErrorType.INVALID_FORMAT);
        }
    }

    private SimpleFeatureCollection parse(File file, QName type) throws FileNotFoundException, IOException, XMLStreamException, SAXException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            PullParser parser = new PullParser(new GPXConfiguration(), in, type);
            SimpleFeature f = (SimpleFeature) parser.parse();
            if (f == null) {
                return new EmptyFeatureCollection(null);
            }
            ListFeatureCollection collection = new ListFeatureCollection(f.getFeatureType());
            collection.add(f);
            while ((f = (SimpleFeature) parser.parse()) != null) {
                collection.add(f);
            }
            return collection;
        }
    }

    private SimpleFeatureCollection parse10(File file, QName type) throws FileNotFoundException, IOException, XMLStreamException, SAXException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            PullParser parser = new PullParser(new GPX10Configuration(), in, type);
            SimpleFeature f = (SimpleFeature) parser.parse();
            if (f == null) {
                return new EmptyFeatureCollection(null);
            }
            ListFeatureCollection collection = new ListFeatureCollection(f.getFeatureType());
            collection.add(f);
            while ((f = (SimpleFeature) parser.parse()) != null) {
                collection.add(f);
            }
            return collection;
        }
    }

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

}
