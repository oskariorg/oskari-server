package fi.nls.oskari.fe.output.format.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.input.jackson.GeometryProperty;
import fi.nls.oskari.fe.input.jackson.GeometryPropertySerializer;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.AbstractOutputStreamProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* Issue with jsonfactory closing stream too early */
/* Issue with json formatting */
public class JsonOutputProcessor extends AbstractOutputStreamProcessor
        implements OutputProcessor {
    /* output */
    long counter = 0;
    protected BufferedWriter ps;
    protected final ObjectMapper json = new ObjectMapper();
    protected ObjectWriter jsonWriter;
    private JsonGenerator jsonGenerator;
    final SimpleModule wktModule;

    public void setOutput(OutputStream out) {
        outs = out;
        ps = new BufferedWriter(new OutputStreamWriter(outs, Charset.forName(
                "UTF-8").newEncoder()));

    }

    public JsonOutputProcessor() {
        json.setSerializationInclusion(Include.NON_NULL);
        wktModule = new SimpleModule("WKTModule", new Version(1, 0, 0, null));
        wktModule.addSerializer(GeometryProperty.class,
                new GeometryPropertySerializer());
        json.registerModule(wktModule);

    }

    @Override
    public void begin() throws IOException {
        // TODO Auto-generated method stub
        jsonGenerator = jsonWriter.getJsonFactory().createGenerator(outs);
        ps.write("{ \"results\": [\n");
    }

    @Override
    public void edge(Resource subject, Resource predicate, Resource value)
            throws IOException {

    }

    @Override
    public void end() throws IOException {
        // TODO Auto-generated method stub
        ps.write("\n");
        ps.write("] }\n");
        ps.flush();
        ps = null;
        jsonGenerator.close();
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
            counter++;
        }
        jsonWriter.writeValue(jsonGenerator, jsonType);
        ps.flush();
    }

    static class JsVertex {
        public Resource iri;
        public Resource type;
        public List<Pair<Resource, Object>> simpleProperties;
        public List<Pair<Resource, Object>> linkProperties;
        public List<Pair<Resource, Geometry>> geometryProperties;
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
            counter++;
        }
        jsonWriter.writeValue(jsonGenerator, jsonVertex);
        ps.flush();
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
        jsonVertex.geometryProperties = null;
        jsonWriter.writeValue(ps, jsonType);

        if (counter != 0L) {
            ps.write(",\n");
            counter++;
        }
        jsonWriter.writeValue(jsonGenerator, jsonVertex);
        ps.flush();

    }

}
