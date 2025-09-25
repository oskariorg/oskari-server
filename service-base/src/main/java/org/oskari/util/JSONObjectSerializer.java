package org.oskari.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JSONObjectSerializer extends JsonSerializer<JSONObject> {

    @Override
    public void serialize(JSONObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writeValue(value, gen);
    }

    private static void writeValue(Object value, JsonGenerator gen) throws IOException {
        if (value == null || value.equals(null)) {
            gen.writeNull();
        } else if (value instanceof String) {
            gen.writeString(value.toString());
        } else if (value instanceof Integer i) {
            gen.writeNumber(i);
        } else if (value instanceof Long l) {
            gen.writeNumber(l);
        } else if (value instanceof Number n) {
            gen.writeNumber(n.doubleValue());
        } else if (value instanceof Boolean b) {
            gen.writeBoolean(b);
        } else if (value instanceof Enum<?> e) {
            gen.writeString(e.name());
        } else if (value instanceof JSONObject j) {
            gen.writeStartObject();
            for (String key : j.keySet()) {
                gen.writeFieldName(key);
                writeValue(j.opt(key), gen);
            }
            gen.writeEndObject();
        } else if (value instanceof JSONArray a) {
            gen.writeStartArray();
            for (Object o : a) {
                writeValue(o, gen);
            }
            gen.writeEndArray();
        } else if (value instanceof Map m) {
            writeValue(new JSONObject(m), gen);
        } else if (value instanceof Collection c) {
            writeValue(new JSONArray(c), gen);
        } else if (value.getClass().isArray()) {
            writeValue(new JSONArray(value), gen);
        } else {
            writeValue(value.toString(), gen);
        }
    }

}
