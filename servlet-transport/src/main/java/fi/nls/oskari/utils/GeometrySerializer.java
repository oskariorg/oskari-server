package fi.nls.oskari.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;

@SuppressWarnings("serial")
public class GeometrySerializer extends StdSerializer<Geometry> {

    protected GeometrySerializer(Class<Geometry> t) {
        super(t);
    }

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
