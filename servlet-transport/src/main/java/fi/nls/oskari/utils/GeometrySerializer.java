package fi.nls.oskari.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * This uses the Jackson 1.x version since it's used by the current version of CometD.
 * Don't upgrade if not upgrading CometD.
 */
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
