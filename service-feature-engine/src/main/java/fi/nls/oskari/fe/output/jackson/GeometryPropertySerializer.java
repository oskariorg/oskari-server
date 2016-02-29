package fi.nls.oskari.fe.output.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fi.nls.oskari.fe.gml.util.GeometryProperty;

import java.io.IOException;

public class GeometryPropertySerializer extends
        JsonSerializer<GeometryProperty> {

    @Override
    public void serialize(GeometryProperty value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        if (value.getGeometry() == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }

        // Todo support at least GeoJSON and WKT
        provider.defaultSerializeValue(value.getGeometry().toText(), jgen);

    }

    @Override
    public Class<GeometryProperty> handledType() {
        // TODO Auto-generated method stub
        return GeometryProperty.class;
    }

}
