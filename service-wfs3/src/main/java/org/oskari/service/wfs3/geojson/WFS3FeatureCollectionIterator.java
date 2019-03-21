package org.oskari.service.wfs3.geojson;

import java.io.IOException;
import java.io.Reader;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.json.simple.parser.JSONParser;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Customized version of org.geotools.geojson.feature.FeatureJSON$FeatureIterator
 * creates a modified FeatureCollectionHandler implementation that can handle "links"
 * object in WFS 3 GeoJSON responses
 */
public class WFS3FeatureCollectionIterator implements SimpleFeatureIterator {

    private final Reader reader;
    private final JSONParser parser;
    private WFS3FeatureCollectionHandler handler;
    private SimpleFeature next;

    public WFS3FeatureCollectionIterator(Reader reader) {
        this.reader = reader;
        this.parser = new JSONParser();
    }

    public WFS3FeatureCollectionHandler getHandler() {
        return handler;
    }

    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        if (handler == null) {
            handler = new WFS3FeatureCollectionHandler();
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
            return handler.getFeature();
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
