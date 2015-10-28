package fi.nls.oskari.fe.input.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import org.geotools.xml.Configuration;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GeometryPropertyDeserializer extends
        JsonDeserializer<GeometryProperty> {

    protected boolean goDeep = true;
    protected boolean ignoreProps = false;
    protected Configuration gml;
    protected FEPullParser parserAny;
    protected final Map<QName, PullParserHandler> handlers = new HashMap<QName, PullParserHandler>();

    public GeometryPropertyDeserializer(Configuration gml,
            FEPullParser parserAny) {
        this.gml = gml;
        this.parserAny = parserAny;
    }

    public Map<QName, PullParserHandler> mapGeometryType(
            final QName qname) {
        Map<QName, PullParserHandler> handlers = new HashMap<QName, PullParserHandler>();
        handlers.put(qname, new FEPullParser.ElementPullParserHandler(qname,
                gml));
        return handlers;
    }

    public Map<QName, PullParserHandler> getHandlers() {

            return handlers;
}

    public void mapGeometryTypes(final String ns, final String... localNames) {

        for (String localPart : localNames) {
            QName qname = new QName(ns, localPart);
            handlers.put(qname, new FEPullParser.ElementPullParserHandler(
                    qname, gml));
        }
    }


    public Object parseGeometry(
            Map<QName, PullParserHandler> handlers,
            QName parentQn, XMLStreamReader reader) throws XMLStreamException,
            IOException, SAXException {
        QName qn = reader.getName();
        PullParserHandler handler = handlers.get(qn);
        Object obj = null;
        if (handler != null) {
            parserAny.setHandler(handler);
            parserAny.setPp(reader);

            obj = parserAny.parse();

        } else if( goDeep ) {
            // try one step deeper as we SHALL get a geometry...
            reader.nextTag();
            qn = reader.getName();
            handler = handlers.get(qn);

            if (handler != null) {
                parserAny.setHandler(handler);
                parserAny.setPp(reader);

                obj = parserAny.parse();
            } else {
                System.err.println("NO HANDLER in deep CONTEXT? " + qn
                        + " / " + parentQn + " - ");
            }
        } else {
            System.err.println("NO HANDLER at " + qn
                    + " / " + parentQn + " - ");
        }

        return obj;
    }


    @Override
    public GeometryProperty deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException {
        Geometry geom = null;

        FromXmlParser parser = (FromXmlParser) ctxt.getParser();

        XMLStreamReader reader = parser.getStaxReader();

        try {

            if(isIgnoreProps()){
              parser.skipChildren();
            } else {

               QName parentQn = parser.getParentQName();
                geom = (Geometry) parseGeometry(handlers, parentQn, reader);
                parser.resume();
            }

        } catch (IOException e) {
            throw new IOException(e);
        } catch (Exception e) {
            throw new IOException(e);

        } finally {

        }

        return new GeometryProperty(geom);
    }


    public boolean isIgnoreProps() {
        return ignoreProps;
    }

    public void setIgnoreProps(boolean ignoreProps) {
        this.ignoreProps = ignoreProps;
    }

    public boolean isGoDeep() {
        return goDeep;
    }

    public void setGoDeep(boolean goDeep) {
        this.goDeep = goDeep;
    }


}
