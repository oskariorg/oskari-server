package fi.nls.oskari.fe.input.format.gml.recipe;

import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.xml.Configuration;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class StaxMateGMLParserRecipeBase extends
        StaxMateXMLParserRecipeBase implements ParserRecipe {

    protected Configuration gml;

    protected FEPullParser parserAny;

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryType(
            final QName qname) {
        Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();
        handlers.put(qname, new FEPullParser.ElementPullParserHandler(qname,
                gml));
        return handlers;
    }

    public Map<QName, FEPullParser.PullParserHandler> mapGeometryTypes(
            final String ns, final String... localNames) {
        Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();

        for (String localPart : localNames) {
            QName qname = new QName(ns, localPart);
            handlers.put(qname, new FEPullParser.ElementPullParserHandler(
                    qname, gml));
        }

        return handlers;

    }

    public Object parseGeometry(
            Map<QName, FEPullParser.PullParserHandler> handlers,
            SMInputCursor crsr) throws XMLStreamException, IOException,
            SAXException {
        QName qn = crsr.getQName();
        PullParserHandler handler = handlers.get(qn);
        Object obj = null;
        if (handler != null) {
            parserAny.setHandler(handler);
            parserAny.setPp(crsr.getStreamReader());

            obj = parserAny.parse();
        }

        return obj;
    }
}
