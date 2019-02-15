package org.oskari.service.wfs3.client;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geojson.RecordingHandler;
import org.geotools.geojson.feature.FeatureCollectionHandler;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Modified FeatureCollectionHandler implementation that can handle complex properties
 * as well as "links" object found in WFS 3 GeoJSON responses
 */
public class WFS3FeatureCollectionHandler extends FeatureCollectionHandler {

    private List<WFS3Link> links;

    private int level = 0; // Track how deep we are
    
    private boolean seenGeometry;
    private RecordingHandler propertiesRecorder;
    private boolean insideProperties;

    private Deque<String> keyStack;
    private String currentKey;

    private Deque<Object> complexPropertyStack;
    private Object complexProperty;
    
    public WFS3FeatureCollectionHandler(SimpleFeatureType sft) {
        super(sft, null);
        keyStack = new ArrayDeque<>();
        complexPropertyStack = new ArrayDeque<>();
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        level++;

        if (insideProperties) {
            if (complexProperty != null) {
                complexPropertyStack.push(complexProperty);
            }
            complexProperty = new HashMap<String, Object>();
            return true;
        }

        if (level == 3 && "properties".equals(currentKey)) {
            insideProperties = true;
            if (propertiesRecorder != null) {
                propertiesRecorder.startObject();
                return true;
            }
        }

        return super.startObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean endObject() throws ParseException, IOException {
        level--;

        if (insideProperties && level == 2) {
            insideProperties = false;
        }
        
        if (level == 2 && propertiesRecorder != null && "geometry".equals(currentKey)) {
            // geometry just ended
            // let's replay stored properties
            RecordingHandler tmp = propertiesRecorder;
            propertiesRecorder = null;
            tmp.replay(this);
        }

        if (insideProperties) {
            // Maybe the object that just ended was a GeoJSON geometry object
            Geometry geometry = tryConvertToGeometry(complexProperty);
            if (geometry != null) {
                // It was!
                complexProperty = geometry;
            }
            Object containingComplexProperty = complexPropertyStack.poll();
            if (containingComplexProperty == null) {
                // This is the "root" of the complex property
                if (propertiesRecorder != null) {
                    propertiesRecorder.startObjectEntry(currentKey);
                    propertiesRecorder.primitive(complexProperty);
                    return true;
                }
                super.startObjectEntry(currentKey);
                super.primitive(complexProperty);
            } else {
                if (containingComplexProperty instanceof Map) {
                    ((Map<String, Object>) containingComplexProperty).put(currentKey, complexProperty); 
                } else {
                    ((List<Object>) containingComplexProperty).add(complexProperty); 
                }
            }
            complexProperty = containingComplexProperty;
            return true;
        }
        
        boolean b = super.endObject();
        if (delegate instanceof WFS3LinksHandler) {
            WFS3LinksHandler linksHandler = (WFS3LinksHandler) delegate;
            links = linksHandler.getValue();
            delegate = NULL;
        }
        return b;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (insideProperties) {
            if (complexProperty != null) {
                complexPropertyStack.push(complexProperty);
            }
            complexProperty = new ArrayList<Object>();
            return true;
        }

        return super.startArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean endArray() throws ParseException, IOException {
        if (insideProperties) {
            Object containingComplexProperty = complexPropertyStack.poll();
            if (containingComplexProperty == null) {
                // This is the "root" of the complex property
                if (propertiesRecorder != null) {
                    propertiesRecorder.startObjectEntry(currentKey);
                    propertiesRecorder.primitive(complexProperty);
                    return true;
                }
                super.startObjectEntry(currentKey);
                super.primitive(complexProperty);
            } else {
                if (containingComplexProperty instanceof Map) {
                    ((Map<String, Object>) containingComplexProperty).put(currentKey, complexProperty); 
                } else {
                    ((List<Object>) containingComplexProperty).add(complexProperty); 
                }
            }
            complexProperty = containingComplexProperty;
            return true;
        }

        return super.endArray();
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (currentKey != null) {
            keyStack.push(currentKey);
        }
        currentKey = key;
        
        if (level == 2 && "geometry".equals(key)) {
            seenGeometry = true;
        }
        
        if (insideProperties) {
            // Don't send the event to the delegate just yet - the property might be complex
            return true;
        }
        
        if (level == 2 && "properties".equals(key)) {
            if (!seenGeometry) {
                // Let's record properties and then replay it
                propertiesRecorder = new RecordingHandler();
                propertiesRecorder.startObjectEntry(key);
                return true;
            }
        }
        
        // We're interested in the "links" iff the key is found at the FeatureCollection level
        // Only checking for "links" wouldn't work, because when a feature has a property named
        // links this path would activate, and we don't want that (obviously)
        if (level == 1 && "links".equals(key)) {
            delegate = new WFS3LinksHandler();
            return true;
        }

        return super.startObjectEntry(key);
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        currentKey = keyStack.poll();
        return super.endObjectEntry();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (insideProperties) {
            if (complexProperty == null) {
                // Okay the property wasn't complex
                if (propertiesRecorder != null) {
                    propertiesRecorder.startObjectEntry(currentKey);
                    propertiesRecorder.primitive(value);
                    return true;
                }
                super.startObjectEntry(currentKey);
                return super.primitive(value);
            }
            if (complexProperty instanceof Map) {
                ((Map<String, Object>) complexProperty).put(currentKey, value); 
            } else {
                ((List<Object>) complexProperty).add(value); 
            }
            return true;
        }
        return super.primitive(value);
    }

    public List<WFS3Link> getLinks() {
        return links == null ? Collections.emptyList() : links;
    }

    /**
     * Tries to convert a complexProperty to a JTS Geometry
     * @param maybeGeometry complexProperty of type Map<String, Object>
     * @return null if it can't be done, and the Geometry if everything seems OK 
     */
    @SuppressWarnings("unchecked")
    private Geometry tryConvertToGeometry(Object maybeGeometry) {
        if (!(maybeGeometry instanceof Map)) {
            // If it's not a map then it can't be a GeoJSON geometry
            return null;
        }
        return MapToGeoJSONGeometry.tryConvertToGeometry((Map<String, Object>) maybeGeometry);
    }

}
