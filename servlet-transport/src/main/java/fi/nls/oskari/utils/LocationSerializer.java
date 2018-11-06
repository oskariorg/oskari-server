package fi.nls.oskari.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fi.nls.oskari.pojo.Location;

import java.io.IOException;

/**
 * Created by SMAKINEN on 6.11.2018.
 */
public class LocationSerializer extends StdSerializer<Location> {
    public LocationSerializer() {
        this(null);
    }

    public LocationSerializer (Class<Location> t) {
        super(t);
    }

    /**
     *  {
         "srs": "EPSG:3067",
         "bbox": [66950.2376524756, 6695838.708160009, 685446.2376524756, 7572382.708160009],
         "zoom": 0
         }
     * @param value
     * @param jgen
     * @param provider
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public void serialize(
            Location value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField("srs", value.getSrs());
        jgen.writeArrayFieldStart("bbox");
        for (double coord : value.getBboxArray()) {
            jgen.writeNumber(coord);
        }
        jgen.writeEndArray();
        jgen.writeNumberField("zoom", value.getZoom());
        jgen.writeEndObject();
    }

    @Override
    public Class<Location> handledType() {
        return Location.class;
    }
}
