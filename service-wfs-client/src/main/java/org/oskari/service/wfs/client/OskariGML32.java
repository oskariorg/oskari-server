package org.oskari.service.wfs.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;

import org.geotools.xml.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fi.nls.oskari.util.XmlHelper;
import net.opengis.wfs20.FeatureCollectionType;

public class OskariGML32 {

    public SimpleFeatureCollection decodeFeatureCollection(InputStream in, String username, String password)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        in.close();

        DOMParser parser = new DOMParser(new OskariWFS2Configuration(username, password), doc);
        Object obj = parser.parse();
        return toFeatureCollection(obj);
    }

    private SimpleFeatureCollection toFeatureCollection(Object obj) {
        if (obj == null) {
            return null; // not available?
        }
        if (obj instanceof FeatureCollectionType) {
            FeatureCollectionType collectionType = (FeatureCollectionType) obj;
            List fc = collectionType.getMember();
            return fc.isEmpty() ? new DefaultFeatureCollection() : (SimpleFeatureCollection) fc.get(0);
        } else {
            throw new ClassCastException(
                    obj.getClass()
                            + " produced when FeatureCollection expected"
                            + " check schema use of AbstractFeatureCollection");
        }
    }
}
