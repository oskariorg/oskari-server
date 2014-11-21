package fi.nls.oskari.fe.input.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeometryPropertySerializer extends
        JsonSerializer<GeometryProperty> {

    @Override
    public void serialize(GeometryProperty value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {

        if (value.geometry == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }

        // Todo support at least GeoJSON and WKT 
        provider.defaultSerializeValue(value.geometry.toText(), jgen);

    }

}
