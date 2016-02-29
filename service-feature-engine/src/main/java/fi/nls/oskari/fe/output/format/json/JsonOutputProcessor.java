package fi.nls.oskari.fe.output.format.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.AbstractOutputStreamProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.output.jackson.GeometryPropertySerializer;
import fi.nls.oskari.fe.schema.XSDDatatype;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonOutputProcessor extends AbstractOutputStreamProcessor
        implements OutputProcessor {

    static class PairSerializer extends JsonSerializer<Pair<Resource, ?>> {

        @Override
        public void serialize(Pair<Resource, ?> value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {

            jgen.writeStartObject();
            provider.defaultSerializeField(
                    value.getKey().toString(), value.getValue(),jgen);
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
            addSerializer(new GeometryPropertySerializer());
            addSerializer(JsonOutputModule.<Pair<Resource, Object>> getClazz(),
                    new PairSerializer());
            super.setupModule(context);
        }
    };

    /* output */
    long counter = 0;

    protected OpenBufferedWriter ps;

    protected final ObjectMapper json = new ObjectMapper();

    public void setOutput(OutputStream out) {
        outs = out;
        ps = new OpenBufferedWriter(new OutputStreamWriter(outs, Charset
                .forName("UTF-8").newEncoder()));

    }

    public JsonOutputProcessor() {
        json.setSerializationInclusion(Include.NON_NULL);
        json.disable(SerializationFeature.CLOSE_CLOSEABLE);
        json.enable(SerializationFeature.INDENT_OUTPUT);

        JsonOutputModule simpleModule = new JsonOutputModule();

        json.registerModule(simpleModule);

    }

    @Override
    public void begin() throws IOException {
        // TODO Auto-generated method stub
        ps.write("{ \"results\": [\n");
    }

    @Override
    public void edge(Resource subject, Resource predicate, Resource value)
            throws IOException {

    }

    @Override
    public void end() throws IOException {
        // TODO Auto-generated method stub
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
        jsonVertex.geometryProperties = new ArrayList<Pair<Resource, Object>>();

        if (geometryProperties != null) {
            for (Pair<Resource, Geometry> p : geometryProperties) {
                jsonVertex.geometryProperties.add(pair(p.getKey(), p.getValue()
                        .toText()));
            }
        }

        if (counter != 0L) {
            ps.write(",\n");
        }
        counter++;
        json.writeValue(ps, jsonVertex);

    }

    public ImmutablePair<Resource, Object> pair(Resource rc, String val) {
        return new ImmutablePair<Resource, Object>(rc, val);
    }

    public void merge(List<JSONObject> list, Resource res) {

    }
    public void equalizePropertyArraySize(Map<String,Integer> multiElemmap,  Map<String, Resource> resmap) {

    }
}
