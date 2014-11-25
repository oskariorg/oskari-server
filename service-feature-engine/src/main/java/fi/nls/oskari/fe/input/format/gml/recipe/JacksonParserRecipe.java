package fi.nls.oskari.fe.input.format.gml.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.tuple.Pair;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.jackson.GeometryPropertyDeserializer;
import fi.nls.oskari.fe.input.jackson.GmlMapper;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* helper class to simplify building inspire and rysp schema parsers */
public abstract class JacksonParserRecipe extends StaxMateGMLParserRecipeBase {

    protected final List<Pair<Resource, Object>> EMPTY = new ArrayList<Pair<Resource, Object>>();

    protected final List<Pair<Resource, XSDDatatype>> O_properties = new ArrayList<Pair<Resource, XSDDatatype>>();
    protected final List<Pair<Resource, Object>> O_linkProperties = new ArrayList<Pair<Resource, Object>>();
    protected final List<Pair<Resource, String>> O_geometryProperties = new ArrayList<Pair<Resource, String>>();

    /* input */
    protected final GmlMapper mapper;

    protected JacksonParserRecipe() {
        gml = new org.geotools.gml3.v3_2.GMLConfiguration(true);
        mapper = new GmlMapper(gml);

    }

    public GeometryPropertyDeserializer getGeometryDeserializer() {
        return mapper.getGeometryDeserializer();
    }

    public static final String POSTFIX = "#";

    public String outputNamespace(String ns) {
        return ns.concat(POSTFIX);
    }

    public class FeatureOutputContext {
        protected final Resource O_Geom = JacksonParserRecipe.this.iri(
                "http://oskari.org/spatial#", "location");

        public final Resource featureResource;
        public final String namespace;

        public FeatureOutputContext(final String ns, final String name)
                throws IOException {
            this.namespace = outputNamespace(ns);
            featureResource = iri(name);
            addOutputPrefix("_ns", namespace);
            addOutputType(featureResource);
        }

        public FeatureOutputContext(QName qn) throws IOException {
            this(qn.getNamespaceURI(), qn.getLocalPart());
        }

        public Resource addDefaultGeometryProperty() {
            O_geometryProperties.add(pair(O_Geom, "GEOMETRY"));
            return O_Geom;
        }

        public Resource addGeometryProperty(Resource r) {
            O_geometryProperties.add(pair(r, "GEOMETRY"));
            return r;
        }

        public Resource addOutputProperty(final String p) {
            Resource prop = iri(p);
            O_properties.add(pair(prop, XSDDatatype.XSDany));
            return prop;
        }

        public Resource addOutputStringProperty(final String p) {
            Resource prop = iri(p);
            O_properties.add(pair(prop, XSDDatatype.XSDstring));
            return prop;
        }

        public Resource addOutputProperty(XSDDatatype xsd, final String p) {
            Resource prop = iri(p);
            O_properties.add(pair(prop, xsd));
            return prop;
        }

        public void addOutputType(Resource r) throws IOException {
            output.type(r, O_properties, O_linkProperties, O_geometryProperties);
        }

        protected void addOutputPrefix(String prefix, String ns)
                throws IOException {
            output.prefix(prefix, ns);
        }

        public Resource iri(String name) {
            return JacksonParserRecipe.this.iri(namespace, name);
        }

        public Resource uniqueId(String id) {
            return featureResource.unique(id);
        }
    }

    public class InputFeature<T> {
        Iterator<InputEvent> iterFeature;
        InputEvent inputFeature;
        Class<T> cls;

        public InputFeature(QName qn, Class<T> cls) throws IOException {
            this.cls = cls;
            try {
                iterFeature = iter(((StaxGMLInputProcessor) input).root()
                        .descendantElementCursor(qn));
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }

        public boolean hasNext() {
            return iterFeature.hasNext();
        }

        public T next() throws IOException {
            inputFeature = iterFeature.next();

            return (T) mapper.readValue(inputFeature.crsr.getStreamReader(),
                    cls);

        }

    }

    public class OutputFeature<T> {

        public T feature;
        public Resource output_ID;
        public Resource output_r;
        protected List<Pair<Resource, Object>> output_props = new ArrayList<Pair<Resource, Object>>();
        protected List<Pair<Resource, Geometry>> output_geoms = new ArrayList<Pair<Resource, Geometry>>();
        protected FeatureOutputContext outputContext;

        public OutputFeature(FeatureOutputContext outputContext) {
            this.output_r = outputContext.featureResource;
            this.outputContext = outputContext;
        }

        public OutputFeature<T> setFeature(T f) {
            this.feature = f;
            return this;
        }

        public OutputFeature<T> setId(Resource output_ID) {
            this.output_ID = output_ID;
            return this;
        }

        public OutputFeature<T> build() throws IOException {
            output.vertex(output_ID, output_r, output_props, EMPTY,
                    output_geoms);
            output_props.clear();
            output_geoms.clear();
            output_ID = null;
            return this;

        }

        public OutputFeature<T> addGeometryProperty(Resource r, Geometry geom) {
            if (geom == null) {
                return this;
            }
            output_geoms.add(pair(r, geom));
            return this;
        }

        public OutputFeature<T> addProperty(Resource property,
                List<? extends Object> value) {
            if (value == null || value.isEmpty()) {
                return this;
            }
            output_props.add(pair(property, value));

            return this;
        }

        public OutputFeature<T> addProperty(Resource property, Object value) {
            if (value == null) {
                return this;
            }
            output_props.add(pair(property, value));

            return this;
        }

    }

}
