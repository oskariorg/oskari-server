package org.oskari.service.wfs3.geojson;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.oskari.service.wfs3.model.WFS3Link;

public class WFS3FeatureCollectionHandler extends NOPContentHandler {

    private boolean isFeatureCollection;
    private Deque<String> keyStack;
    private JSONObjectHandler featureDelegate;
    private JSONArrayOfObjectsHandler linksDelegate;
    private List<WFS3Link> links;

    public WFS3FeatureCollectionHandler() {
        this.keyStack = new ArrayDeque<>();
    }

    public boolean isFeatureCollection() {
        return isFeatureCollection;
    }

    public SimpleFeature getFeature() {
        if (featureDelegate == null) {
            return null;
        }
        Map<String, Object> map = featureDelegate.getJSONObject();
        SimpleFeature f = MapToGeoJSONFeature.tryConvertToSimpleFeature(map);
        return f;
    }

    public List<WFS3Link> getLinks() {
        return links == null ? Collections.emptyList() : links;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        if (featureDelegate != null) {
            return featureDelegate.startObject();
        }
        if (linksDelegate != null) {
            return linksDelegate.startObject();
        }

        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        if (featureDelegate != null) {
            return featureDelegate.endObject();
        }
        if (linksDelegate != null) {
            return linksDelegate.endObject();
        }

        return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (featureDelegate != null) {
            return featureDelegate.startArray();
        }
        if (linksDelegate != null) {
            return linksDelegate.startArray();
        }

        int n = keyStack.size();
        if (n == 1 && "features".equals(keyStack.peek())) {
            featureDelegate = new JSONObjectHandler();
            return true;
        }
        if (n == 1 && "links".equals(keyStack.peek())) {
            linksDelegate = new JSONArrayOfObjectsHandler();
            linksDelegate.startArray();
            return true;
        }

        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        if (featureDelegate != null) {
            boolean shouldContinue = featureDelegate.endArray();
            if (shouldContinue) {
                return true;
            }
            featureDelegate = null;
        }
        if (linksDelegate != null) {
            boolean shouldContinue = linksDelegate.endArray();
            if (shouldContinue) {
                return true;
            }
            links = MapToWFS3Links.tryConvertToLinks(linksDelegate.getJSONArray());
            linksDelegate = null;
        }

        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (featureDelegate != null) {
            return featureDelegate.startObjectEntry(key);
        }
        if (linksDelegate != null) {
            return linksDelegate.startObjectEntry(key);
        }

        keyStack.push(key);
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        if (featureDelegate != null) {
            return featureDelegate.endObjectEntry();
        }
        if (linksDelegate != null) {
            return linksDelegate.endObjectEntry();
        }

        keyStack.pop();
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (featureDelegate != null) {
            return featureDelegate.primitive(value);
        }
        if (linksDelegate != null) {
            return linksDelegate.primitive(value);
        }

        int n = keyStack.size();
        if (n == 0) {
            throw new ParseException(0);
        }
        if (n == 1 && "type".equals(keyStack.peek())) {
            if (!"FeatureCollection".equals(value)) {
                throw new ParseException(0);
            }
            isFeatureCollection = true;
        }
        return true;
    }

}

