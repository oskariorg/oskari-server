package fi.nls.oskari.fe.input.format.gml;

import org.geotools.xml.Configuration;
import org.geotools.xml.impl.ElementHandler;
import org.geotools.xml.impl.NodeImpl;
import org.geotools.xml.impl.ParserHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * XML pull parser capable of streaming.
 * <p>
 * Similar in nature to {@link StreamingParser} but based on XPP pull parsing
 * rather than SAX.
 * 
 * @author Justin Deoliveira, OpenGeo (Original Author)
 * @author nls-jajuko modications
 */
public class FEPullParser {

    class Attributes implements org.xml.sax.Attributes {

        public int getIndex(String qName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if (pp.getAttributeName(i).equals(qName)) {
                    return i;
                }

            }
            return -1;
        }

        public int getIndex(String uri, String localName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if (pp.getAttributeNamespace(i).equals(uri)
                        && pp.getAttributeName(i).equals(localName)) {
                    return i;
                }

            }
            return -1;
        }

        public int getLength() {
            return pp.getAttributeCount();
        }

        public String getLocalName(int index) {
            return pp.getAttributeLocalName(index);
        }

        public String getQName(int index) {
            final String prefix = pp.getAttributePrefix(index);
            if (prefix != null) {
                return prefix + ':' + pp.getAttributeName(index);
            } else {
                return str(pp.getAttributeName(index));
            }
        }

        public String getType(int index) {
            return pp.getAttributeType(index);
        }

        public String getType(String qName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if (pp.getAttributeName(i).equals(qName)) {
                    return pp.getAttributeType(i);
                }

            }
            return null;
        }

        public String getType(String uri, String localName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if (pp.getAttributeNamespace(i).equals(uri)
                        && pp.getAttributeName(i).equals(localName)) {
                    return pp.getAttributeType(i);
                }

            }
            return null;
        }

        public String getURI(int index) {
            return pp.getAttributeNamespace(index);
        }

        public String getValue(int index) {
            return pp.getAttributeValue(index);
        }

        public String getValue(String qName) {
            return pp.getAttributeValue(null, qName);
        }

        public String getValue(String uri, String localName) {
            return pp.getAttributeValue(uri, localName);
        }
    }

    static class ElementIgnoringNamespacePullParserHandler extends
            ElementPullParserHandler {
        public ElementIgnoringNamespacePullParserHandler(QName element,
                Configuration config) {
            super(element, config);
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            return element.getLocalPart().equals(
                    handler.getComponent().getName());
        }

    }

    public static class ElementPullParserHandler extends PullParserHandler {
        QName element;

        public ElementPullParserHandler(QName element, Configuration config) {
            super(config);
            this.element = element;
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            boolean equal = false;
            if (element.getNamespaceURI() != null) {
                equal = element.getNamespaceURI().equals(
                        handler.getComponent().getNamespace());
            } else {
                equal = handler.getComponent().getNamespace() == null;
            }
            return equal
                    && element.getLocalPart().equals(
                    localPart(handler.getComponent().getName()));
        }
        String localPart(String Name) {
            return Name != null ? Name.split(":")[Name.split(":").length-1] : null;
        }
    }

    // aggregate the other handlers, and stop if any of them want to stop
    static class OrPullParserHandler extends PullParserHandler {
        private final Collection<PullParserHandler> parserHandlers;

        public OrPullParserHandler(Configuration config, Object... handlerSpecs) {
            super(config);
            Collection<PullParserHandler> handlers = new ArrayList<PullParserHandler>(
                    handlerSpecs.length);
            for (Object spec : handlerSpecs) {
                if (spec instanceof Class) {
                    handlers.add(new TypePullParserHandler((Class<?>) spec,
                            config));
                } else if (spec instanceof QName) {
                    // TODO ignoring the namespace
                    handlers.add(new ElementIgnoringNamespacePullParserHandler(
                            (QName) spec, config));
                } else if (spec instanceof PullParserHandler) {
                    handlers.add((PullParserHandler) spec);
                } else {
                    throw new IllegalArgumentException("Unknown element: "
                            + spec.toString() + " of type: " + spec.getClass());
                }
            }
            parserHandlers = Collections.unmodifiableCollection(handlers);
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            for (PullParserHandler pph : parserHandlers) {
                if (pph.stop(handler)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static abstract class PullParserHandler extends ParserHandler {

        FEPullParser parser;
        Object object;

        public PullParserHandler(Configuration config) {
            super(config);
        }

        @Override
        protected void endElementInternal(ElementHandler handler) {
            object = null;
            if (stop(handler)) {
                object = handler.getParseNode().getValue();

                // remove this node from parse tree
                if (handler.getParentHandler() instanceof ElementHandler) {
                    ElementHandler parent = (ElementHandler) handler
                            .getParentHandler();
                    ((NodeImpl) parent.getParseNode()).removeChild(handler
                            .getParseNode());
                }
            }
        }

        public Object getObject() {
            return object;
        }

        protected abstract boolean stop(ElementHandler handler);

    }

    static class TypePullParserHandler extends PullParserHandler {
        Class<?> type;

        public TypePullParserHandler(Class<?> type, Configuration config) {
            super(config);
            this.type = type;
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            return type.isInstance(handler.getParseNode().getValue());
        }
    }

    PullParserHandler handler;

    XMLStreamReader pp;

    Attributes atts = new Attributes();

    public FEPullParser(Configuration config, XMLStreamReader input) {
        this(config, input, (PullParserHandler) null);
    }

    public FEPullParser(Configuration config, XMLStreamReader input,
            Class<?> type) {
        this(config, input, new TypePullParserHandler(type, config));
    }

    public FEPullParser(Configuration config, XMLStreamReader input,
            Object... handlerSpecs) {
        this(config, input, new OrPullParserHandler(config, handlerSpecs));
    }

    public FEPullParser(Configuration config, XMLStreamReader input,
            PullParserHandler handler) {
        this.handler = handler;
        pp = input;
    }

    public FEPullParser(Configuration config, XMLStreamReader input,
            QName element) {
        this(config, input, new ElementPullParserHandler(element, config));
    }

    XMLStreamReader createPullParser(InputStream input) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        try {
            return factory.createXMLStreamReader(input);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error creating pull parser", e);
        }
    }

    public PullParserHandler getHandler() {
        return handler;
    }

    public XMLStreamReader getPp() {
        return pp;
    }

    public Object parse() throws XMLStreamException, IOException, SAXException {
        if (handler.getLogger() == null) {
            handler.startDocument();
        }

        int depth = 0;

        LOOP: do {
            int e = pp.getEventType();// .next();

            switch (e) {
            case XMLStreamReader.START_ELEMENT:

                int count = pp.getNamespaceCount();
                for (int i = 0; i < count; i++) {
                    String pre = pp.getNamespacePrefix(i);
                    handler.startPrefixMapping(pre != null ? pre : "",
                            pp.getNamespaceURI(i));
                }

                {
                    QName qName = pp.getName();
                    // System.out.println("                > ".substring(0,depth*2)+qName.getLocalPart());
                    handler.startElement(pp.getNamespaceURI(),
                            pp.getLocalName(), str(qName), atts);
                }
                depth++;
                break;

            case XMLStreamReader.CHARACTERS:
                char[] chars = pp.getTextCharacters();
                handler.characters(chars, pp.getTextStart(), pp.getTextLength());
                break;

            case XMLStreamReader.END_ELEMENT:
                depth--;

                {
                    QName qName = pp.getName();
                    handler.endElement(pp.getNamespaceURI(), pp.getLocalName(),
                            str(qName));

                    // System.out.println("                < ".substring(0,depth*2)+qName.getLocalPart());
                }

                count = pp.getNamespaceCount();
                // undeclare them in reverse order
                for (int i = count - 1; i >= 0; i--) {
                    handler.endPrefixMapping(pp.getNamespacePrefix(i));
                }

                // check whether to break out
                if (handler.getObject() != null) {
                    // System.out.println("Current obj "+handler.getObject());
                }

                /*
                 * if (depth == 0) { return handler.getObject(); }
                 */
                if (depth == 0) {
                    return handler.getObject();
                }
                break;
            case XMLStreamReader.END_DOCUMENT:
                break LOOP;
            }

            pp.next();

        } while (true);

        return null;
    }

    QName qName(String prefix, String name, XMLStreamReader pp2) {
        if (prefix != null) {
            return new QName(pp.getNamespaceURI(prefix), name, prefix);
        } else {
            return new QName(name);
        }
    }

    public void setHandler(PullParserHandler handler) {
        this.handler = handler;
    }

    public void setPp(XMLStreamReader pp) {
        this.pp = pp;
    }

    String str(QName qName) {
        return qName.getPrefix() != null ? qName.getPrefix() + ":"
                + qName.getLocalPart() : qName.getLocalPart();
    }

}