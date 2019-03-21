package org.oskari.service.wfs3.geojson;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

public class JSONArrayOfObjectsHandler implements ContentHandler {

    private Deque<String> keyStack;
    private String currentKey;
    private Deque<Object> objectStack;
    private Object currentObject;

    public JSONArrayOfObjectsHandler() {
        this.keyStack = new ArrayDeque<>();
        this.objectStack = new ArrayDeque<>();
    }

    @SuppressWarnings("unchecked")
    public List<Object> getJSONArray() {
        List<Object> jsonArray = (List<Object>) currentObject;
        currentObject = null;
        return jsonArray;
    }

    @Override
    public void startJSON() throws ParseException, IOException {
        // NOP
    }

    @Override
    public void endJSON() throws ParseException, IOException {
        // NOP
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        if (currentObject == null) {
            throw new ParseException(0);
        }
        objectStack.push(currentObject);
        currentObject = new HashMap<String, Object>();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean endObject() throws ParseException, IOException {
        Object containingComplexProperty = objectStack.poll();
        if (containingComplexProperty == null) {
            return false;
        }
        if (containingComplexProperty instanceof Map) {
            ((Map<String, Object>) containingComplexProperty).put(currentKey, currentObject); 
        } else {
            ((List<Object>) containingComplexProperty).add(currentObject); 
        }
        currentObject = containingComplexProperty;
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (currentKey != null) {
            keyStack.push(currentKey);
        }
        currentKey = key;
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        currentKey = keyStack.poll();
        return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (currentObject != null) {
            objectStack.push(currentObject);
        }
        currentObject = new ArrayList<Object>();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean endArray() throws ParseException, IOException {
        Object containingComplexProperty = objectStack.poll();
        if (containingComplexProperty == null) {
            return false;
        }
        if (containingComplexProperty instanceof Map) {
            ((Map<String, Object>) containingComplexProperty).put(currentKey, currentObject); 
        } else {
            ((List<Object>) containingComplexProperty).add(currentObject); 
        }
        currentObject = containingComplexProperty;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (currentObject instanceof Map) {
            ((Map<String, Object>) currentObject).put(currentKey, value);
        } else {
            ((List<Object>) currentObject).add(value);
        }
        return true;
    }

}
