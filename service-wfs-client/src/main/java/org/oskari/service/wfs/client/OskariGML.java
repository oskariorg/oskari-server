package org.oskari.service.wfs.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.wfs.GML;
import org.geotools.xsd.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.oskari.xml.XmlHelper;
import net.opengis.wfs.FeatureCollectionType;

/**
 * Customized version of org.geotools.GML that allows
 * username and password to be passed for the configuration
 * 
 * Unfortunately both:
 * SimpleFeatureCollection toFeatureCollection(Object)
 * SimpleFeatureCollection simpleFeatureCollection(Collection<?>
 * are marked private in the GML class, so they are copy-pasted over
 */
public class OskariGML extends GML implements OskariGMLDecoder {

    public OskariGML() {
        super(Version.WFS1_1);
    }

    public SimpleFeatureCollection decodeFeatureCollection(InputStream in, String username, String password)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        in.close();
        Element root = doc.getDocumentElement();
        /**
         * Remove problematic attribute numberOfFeatures, some servers
         * write "unknown" which causes the parser to fail miserably when it
         * expects a xsd:nonNegativeInteger
         */
        root.removeAttribute("numberOfFeatures");
        root.removeAttribute("wfs:numberOfFeatures");
        /**
         * Remove schemaLocation information
         * Complex schemas are tedious to parse and if they happen to be
         * behind authorization there's no way for us to control the authorization
         * information done when fetching other <xsd:include>'d schemas
         */
        //root.removeAttribute("schemaLocation");
        //root.removeAttribute("xsi:schemaLocation");
        OskariWFSConfiguration conf = new OskariWFSConfiguration(username, password);
        for (Object dep : conf.allDependencies()) {
            if (dep instanceof GMLConfiguration) {
                ((GMLConfiguration) dep).setExtendedArcSurfaceSupport(true);
            }
        }

        Parser parser = new Parser(conf);
        Object obj = null;
        try {
            obj = parser.parse(new DOMSource(doc));
        } catch (TransformerException e) {
            new IOException(e.getCause());
        }
        return toFeatureCollection(obj);
    }

    /**
     * Convert parse results into a SimpleFeatureCollection.
     *
     * @param obj SimpleFeatureCollection, Collection<?>, SimpleFeature, etc...
     * @return SimpleFeatureCollection of the results
     */
    private SimpleFeatureCollection toFeatureCollection(Object obj) {
        if (obj == null) {
            return null; // not available?
        }
        if (obj instanceof SimpleFeatureCollection) {
            return (SimpleFeatureCollection) obj;
        }
        if (obj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) obj;
            SimpleFeatureCollection simpleFeatureCollection = simpleFeatureCollection(collection);
            return simpleFeatureCollection;
        }
        if (obj instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) obj;
            return DataUtilities.collection(feature);
        }
        if (obj instanceof FeatureCollectionType) {
            FeatureCollectionType collectionType = (FeatureCollectionType) obj;
            for (Object entry : collectionType.getFeature()) {
                SimpleFeatureCollection collection = toFeatureCollection(entry);
                if (entry != null) {
                    return collection;
                }
            }
            return new DefaultFeatureCollection(); // nothing found, return empty collection
        } else {
            throw new ClassCastException(
                    obj.getClass()
                    + " produced when FeatureCollection expected"
                    + " check schema use of AbstractFeatureCollection");
        }
    }

    /**
     * Go through collection contents and morph contents into SimpleFeatures as required.
     *
     * @param collection
     * @return SimpleFeatureCollection
     */
    private SimpleFeatureCollection simpleFeatureCollection(Collection<?> collection) {
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        SimpleFeatureType schema = null;
        for (Object obj : collection) {
            if (schema == null) {
                schema = simpleType(obj);
            }
            SimpleFeature feature = simpleFeature(obj, schema);
            featureCollection.add(feature);
        }
        return featureCollection;
    }

}
