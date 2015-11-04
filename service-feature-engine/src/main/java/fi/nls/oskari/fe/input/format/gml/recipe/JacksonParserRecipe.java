package fi.nls.oskari.fe.input.format.gml.recipe;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.jackson.GeometryPropertyDeserializer;
import fi.nls.oskari.fe.input.jackson.GmlMapper;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;
import fi.nls.oskari.fi.rysp.generic.WFS11_path_parse_worker;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.xml.Configuration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* helper class to simplify building inspire and rysp schema parsers */
public abstract class JacksonParserRecipe extends StaxMateGMLParserRecipeBase {

    protected final List<Pair<Resource, Object>> EMPTY = new ArrayList<Pair<Resource, Object>>();

    protected final List<Pair<Resource, XSDDatatype>> O_properties = new ArrayList<Pair<Resource, XSDDatatype>>();
    protected final List<Pair<Resource, Object>> O_linkProperties = new ArrayList<Pair<Resource, Object>>();
    protected final List<Pair<Resource, String>> O_geometryProperties = new ArrayList<Pair<Resource, String>>();
    protected ELF_path_parse_worker parseWorker = null;
    protected WFS11_path_parse_worker wfs11ParseWorker = null;

    public static abstract class GML32 extends JacksonParserRecipe {
        public GML32() {
            super(new org.geotools.gml3.v3_2.GMLConfiguration(true));
        }

        @Override
        public void setupGeometryMapper(
                GeometryPropertyDeserializer geometryPropertyDeserializer) {
            geometryPropertyDeserializer.mapGeometryTypes(
                    "http://www.opengis.net/gml/3.2", "Polygon", "Surface",
                    "PolyhedralSurface", "TriangulatedSurface", "Tin",
                    "OrientableSurface", "CompositeSurface", "LineString",
                    "Curve", "CompositeCurve", "OrientableCurve", "MultiCurve",
                    "Envelope",
                    "Point",
                    "MultiSurface",
                    "MultiPoint");
        }

    }

    public static abstract class GML31 extends JacksonParserRecipe {
        public GML31() {
            super(new GML31_Configuration());
        }

        @Override
        public void setupGeometryMapper(
                GeometryPropertyDeserializer geometryPropertyDeserializer) {

            geometryPropertyDeserializer.mapGeometryTypes(
                    "http://www.opengis.net/gml", "Polygon", "Surface",
                    "PolyhedralSurface", "TriangulatedSurface", "Tin",
                    "OrientableSurface", "CompositeSurface", "Curve",
                    "LineString", "LinearRing", "MultiLineString",
                    "Envelope",
                    "MultiSurface",
                    "MultiCurve", "Point", "MultiPoint");

        }

    }

    /* input */
    protected final GmlMapper mapper;

    protected JacksonParserRecipe(Configuration conf) {
        gml = conf;
        mapper = new GmlMapper(gml, false);
        setupGeometryMapper(getGeometryDeserializer());
    }

    public void setParseWorker(ELF_path_parse_worker worker) {
        this.parseWorker = worker;
    }
    public void setWFS11ParseWorker(WFS11_path_parse_worker worker) {
        this.wfs11ParseWorker = worker;
    }

    public void setLenient(boolean l) {
        mapper.setLenient(l);
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

        protected void addOutputType(Resource r) throws IOException {
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

        public void build() throws IOException {
            addOutputType(featureResource);

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
        public XMLStreamReader getStreamReader(QName qn) throws IOException
        {

                return ((StaxGMLInputProcessor) input).root().getStreamReader();

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
            feature = null;
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

    public GmlMapper getMapper() {
        return mapper;
    }

    public abstract void setupGeometryMapper(
            GeometryPropertyDeserializer geometryPropertyDeserializer);

}
