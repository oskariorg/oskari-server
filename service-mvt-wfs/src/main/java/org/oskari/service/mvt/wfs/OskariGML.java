package org.oskari.service.mvt.wfs;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.GML;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.xml.StreamingParser;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

public class OskariGML extends GML {

    private OskariGMLConfiguration cfg;

    public OskariGML(Version version, OskariGMLConfiguration cfg) {
        super(version);
        this.cfg = cfg;
    }

    @Override
    public SimpleFeatureIterator decodeFeatureIterator(InputStream in)
            throws IOException, ParserConfigurationException, SAXException {
        StreamingParser p = new StreamingParser(cfg, in, SimpleFeature.class);
        return iterator(p);
    }

}
