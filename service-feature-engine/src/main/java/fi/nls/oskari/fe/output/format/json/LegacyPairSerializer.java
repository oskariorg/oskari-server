package fi.nls.oskari.fe.output.format.json;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.Module.SetupContext;
import org.codehaus.jackson.map.module.SimpleModule;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.AbstractOutputStreamProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.jackson.LegacyGeometryPropertySerializer;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class LegacyPairSerializer extends JsonSerializer<Pair<Resource, ?>> {

    @Override
    public void serialize(Pair<Resource, ?> value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeFieldName(value.getKey().toString());
        jgen.writeObject(value.getValue());
        jgen.writeEndObject();

    }

}