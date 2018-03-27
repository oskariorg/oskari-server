package org.oskari.map.userlayer.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

/**
 * Parse Google KML
 */
public class KMLParser implements FeatureCollectionParser {

    private static final Logger LOG = LogFactory.getLogger(KMLParser.class);

    public static final String SUFFIX = "KML";

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            Parser parser = new Parser(new KMLConfiguration());
            DefaultFeatureCollection fc = new DefaultFeatureCollection();
            SimpleFeature f = (SimpleFeature) parser.parse(in);
            fc.add(f);
            return fc;
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (SAXException e) {
            LOG.warn(e, "Failed to parse KML file");
            throw new ServiceException("Invalid KML file: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new ServiceException("KML parser failed to initialize");
        }
    }

}
