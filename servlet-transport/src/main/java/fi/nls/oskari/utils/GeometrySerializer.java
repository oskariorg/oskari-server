package fi.nls.oskari.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Geometry;

class GeometrySerializer extends JsonSerializer<Geometry> {

    @Override
    public void serialize(Geometry value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        if (value == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }

        // Todo support at least GeoJSON and WKT
        provider.defaultSerializeValue(value.toText(), jgen);

    }

    @Override
    public Class<Geometry> handledType() {
        return Geometry.class;
    }

}
