package fi.nls.oskari.map.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.List;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.service.ServiceRuntimeException;

import static fi.nls.oskari.domain.map.OskariLayer.*;

public class VectorStyleHelper {
    private static final List<String> TYPES = Arrays.asList(TYPE_WFS, TYPE_3DTILES, TYPE_VECTOR_TILE);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    public static VectorStyle readJSON(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, VectorStyle.class);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't parse vector style from: " + json, ex);
        }
    }

    public static String writeJSON(List<VectorStyle> style) {
        try {
            return OBJECT_MAPPER.writeValueAsString(style);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't write vector style to JSON", ex);
        }
    }

    public static boolean isVectorLayer (OskariLayer layer) {
        return TYPES.contains(layer.getType());
    }
}
