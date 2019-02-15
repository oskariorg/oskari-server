package org.oskari.service.wfs3.client;

import java.io.IOException;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

public class StringifyHandler implements ContentHandler {

    private StringBuilder sb = new StringBuilder();

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
        sb.append('{');
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        sb.append('}');
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        sb.append('"').append(key).append('"').append(':');
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        sb.append('[');
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        sb.append(']');
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (value instanceof String) {
            sb.append('"').append(value).append('"');
        } else {
            sb.append(value);
        }
        return true;
    }

    public String stringify() {
        return sb.toString();
    }

}
