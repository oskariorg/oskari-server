package fi.nls.oskari.fe.output.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import fi.nls.oskari.fe.gml.util.GeometryProperty;

public class LegacyGeometryPropertySerializer extends
        JsonSerializer<GeometryProperty> {

    @Override
    public void serialize(GeometryProperty value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        if (value.getGeometry() == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }

        // Todo support at least GeoJSON and WKT
        provider.defaultSerializeValue(value.getGeometry().toText(), jgen);

    }

    @Override
    public Class<GeometryProperty> handledType() {
        return GeometryProperty.class;
    }

}
