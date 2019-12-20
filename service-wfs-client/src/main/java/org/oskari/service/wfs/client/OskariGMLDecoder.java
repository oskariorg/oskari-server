package org.oskari.service.wfs.client;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.xml.sax.SAXException;

public interface OskariGMLDecoder {

    public SimpleFeatureCollection decodeFeatureCollection(InputStream in, String username, String password) throws IOException, SAXException, ParserConfigurationException;

}
