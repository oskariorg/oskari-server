package fi.nls.oskari.fe.output.format.json;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fi.nls.oskari.fe.iri.Resource;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class LegacyPairSerializer extends JsonSerializer<Pair<Resource, ?>> {

    @Override
    public void serialize(Pair<Resource, ?> value, JsonGenerator jgen,
                          SerializerProvider provider)
            throws IOException,
                   JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeFieldName(value.getKey().toString());
        jgen.writeObject(value.getValue());
        jgen.writeEndObject();

    }

}
