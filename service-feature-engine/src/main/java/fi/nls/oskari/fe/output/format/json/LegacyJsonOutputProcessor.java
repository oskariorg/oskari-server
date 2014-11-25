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
import org.codehaus.jackson.map.module.SimpleModule;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.input.jackson.GeometryProperty;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.AbstractOutputStreamProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.jackson.LegacyGeometryPropertySerializer;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class LegacyJsonOutputProcessor extends AbstractOutputStreamProcessor
        implements OutputProcessor {
    /* output */
    long counter = 0;
    protected OpenBufferedWriter ps;

    final JsonFactory jsonFactory = new JsonFactory();
    final ObjectMapper json = new ObjectMapper();

    static class PairSerializer extends JsonSerializer<Pair<Resource, ?>> {

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

    static class OpenBufferedWriter extends BufferedWriter {

        public OpenBufferedWriter(Writer out) {
            super(out);

        }

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub

        }

    }

    static class JsonOutputModule extends SimpleModule {
        static <T> Class getClazz(T... param) {
            return param.getClass().getComponentType();
        }

        JsonOutputModule() {
            super("SimpleModule", new Version(1, 0, 0, null));
        }

        private static final long serialVersionUID = -4278178835803000867L;

        @Override
        public void setupModule(SetupContext context) {
            addSerializer(new LegacyGeometryPropertySerializer());
            addSerializer(JsonOutputModule.<Pair<Resource, Object>> getClazz(),
                    new PairSerializer());
            super.setupModule(context);
        }
    };

    public void setOutput(OutputStream out) {
        outs = out;
        ps = new OpenBufferedWriter(new OutputStreamWriter(outs, Charset
                .forName("UTF-8").newEncoder()));
    }

    public LegacyJsonOutputProcessor() {

        JsonOutputModule simpleModule = new JsonOutputModule();

        json.registerModule(simpleModule);
    }

    @Override
    public void begin() throws IOException {
        ps.write("{ \"results\": [\n");
    }

    @Override
    public void edge(Resource subject, Resource predicate, Resource value)
            throws IOException {

    }

    @Override
    public void end() throws IOException {
        ps.flush();
        ps.write("\n");
        ps.write("] }\n");
        ps.flush();

    }

    @Override
    public void flush() throws IOException {
        ps.flush();
    }

    static class JsType {
        public Resource type;
        public List<Pair<Resource, XSDDatatype>> simpleProperties;
        public List<Pair<Resource, Object>> linkProperties;
        public List<Pair<Resource, String>> geometryProperties;
    }

    final JsType jsonType = new JsType();

    @Override
    public void type(Resource type,
            List<Pair<Resource, XSDDatatype>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties,
            List<Pair<Resource, String>> geometryProperties) throws IOException {
        jsonType.type = type;
        jsonType.simpleProperties = simpleProperties;
        jsonType.linkProperties = linkProperties;
        jsonType.geometryProperties = geometryProperties;
        if (counter != 0L) {
            ps.write(",\n");
        }
        counter++;

        json.writeValue(ps, jsonType);

    }

    static class JsVertex {
        public Resource iri;
        public Resource type;
        public List<Pair<Resource, Object>> simpleProperties;
        public List<Pair<Resource, Object>> linkProperties;
        public List<Pair<Resource, Object>> geometryProperties;
    }

    final JsVertex jsonVertex = new JsVertex();

    @Override
    public void vertex(Resource iri, Resource type,
            List<Pair<Resource, Object>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties) throws IOException {

        jsonVertex.iri = iri;
        jsonVertex.type = type;
        jsonVertex.simpleProperties = simpleProperties;
        jsonVertex.linkProperties = linkProperties;
        jsonVertex.geometryProperties = null;
        if (counter != 0L) {
            ps.write(",\n");
        }
        counter++;
        json.writeValue(ps, jsonVertex);

    }

    @Override
    public void vertex(Resource iri, Resource type,
            List<Pair<Resource, Object>> simpleProperties,
            List<Pair<Resource, Object>> linkProperties,
            List<Pair<Resource, Geometry>> geometryProperties)
            throws IOException {

        jsonVertex.iri = iri;
        jsonVertex.type = type;
        jsonVertex.simpleProperties = simpleProperties;
        jsonVertex.linkProperties = linkProperties;
        jsonVertex.geometryProperties = null;// geometryProperties;
        if (geometryProperties != null) {
            jsonVertex.geometryProperties = new ArrayList<Pair<Resource, Object>>(
                    geometryProperties.size());
            for (Pair<Resource, Geometry> p : geometryProperties) {
                jsonVertex.geometryProperties.add(pair(p.getKey(),
                        new GeometryProperty(p.getValue())));
            }
        } else {
            jsonVertex.geometryProperties = null;// geometryProperties;
        }
        if (counter != 0L) {
            ps.write(",\n");
        }
        counter++;
        json.writeValue(ps, jsonVertex);

    }

    public ImmutablePair<Resource, Object> pair(Resource rc, Object val) {
        return new ImmutablePair<Resource, Object>(rc, val);
    }

}
