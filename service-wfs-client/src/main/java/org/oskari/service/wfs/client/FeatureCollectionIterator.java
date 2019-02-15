package org.oskari.service.wfs.client;

import java.io.IOException;
import java.io.Reader;

import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureCollectionHandler;
import org.json.simple.parser.JSONParser;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Extracted from org.geotools.geojson.feature.FeatureJSON$FeatureIterator
 * Use this when you don't want to init FeatureJSON (and GeometryJSON and GeometryFactory etc...)
 */
public class FeatureCollectionIterator implements FeatureIterator<SimpleFeature> {

    private Reader reader;
    private JSONParser parser;
    private FeatureCollectionHandler handler;
    private SimpleFeature next;

    public FeatureCollectionIterator(Reader reader) {
        this.reader = reader;
        this.parser = new JSONParser();
    }

    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        if (handler == null) {
            handler = new FeatureCollectionHandler();
        }
        next = readNext();
        return next != null;
    }

    public SimpleFeature next() {
        SimpleFeature feature = next;
        next = null;
        return feature;
    }

    SimpleFeature readNext() {
        try {
            parser.parse(reader, handler, true);
            return handler.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                // nothing to do
            }
        }
        handler = null;
    }
}
