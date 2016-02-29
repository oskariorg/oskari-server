package fi.nls.oskari.fe.input.format.gml.recipe;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMInputCursor;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

/* todo need Exception from within iterable to be exposed */
public abstract class StaxMateXMLParserRecipeBase {

    public class InputEvent {
        public SMEvent next;
        public SMInputCursor crsr;
        public QName qn;

        public InputEvent(SMEvent next, SMInputCursor crsr)
                throws XMLStreamException {
            this.next = next;
            this.crsr = crsr;
            if (next != null && next.hasQName()) {
                this.qn = crsr.getQName();
            } else if (next == null) {

            }
        }

        public String attr(String localName) throws XMLStreamException {
            return crsr.getAttrValue(localName);
        }

        public String attr(String ns, String localName)
                throws XMLStreamException {
            return crsr.getAttrValue(ns, localName);
        }

        public SMInputCursor childElementCursor() throws XMLStreamException {
            return crsr.childElementCursor();
        }

        public SMInputCursor childElementCursor(QName qname)
                throws XMLStreamException {
            return crsr.childElementCursor(qname);
        }

        public SMInputCursor descendantElementCursor()
                throws XMLStreamException {
            return crsr.descendantElementCursor();
        }

        public SMInputCursor descendantElementCursor(QName qname)
                throws XMLStreamException {
            return crsr.descendantElementCursor(qname);
        }

        public QName getQName() {
            return qn;
        }

        public boolean hasNext() {
            return next != null;
        }

        public Object parseGeometry(
                Map<QName, FEPullParser.PullParserHandler> handlers)
                throws XMLStreamException, IOException, SAXException {

            return StaxMateXMLParserRecipeBase.this.parseGeometry(handlers,
                    crsr);

        }

        public Object parsePrimitive(Map<QName, Resource> primitive)
                throws XMLStreamException {
            if (primitive.get(crsr.getQName()) != null) {

                return crsr.getElemStringValue();
            }
            return null;
        }

        /**
         * TBD returns title for now
         * 
         * @return
         * @throws XMLStreamException
         */
        public Object parseXlink() throws XMLStreamException {
            return crsr.getAttrValue(W3XLink.XMLNS_W3_ORG_1999_XLINK,
                    W3XLink.title.toString());
        }

        public Iterator<InputEvent> readChildren() throws XMLStreamException {
            SMInputCursor childCrsr = childElementCursor();
            return iter(childCrsr);
        }

        public Iterator<InputEvent> readDescendants() throws XMLStreamException {
            return iter(descendantElementCursor());
        }

        public Iterator<InputEvent> readDescendants(QName qn)
                throws XMLStreamException {
            return iter(descendantElementCursor(qn));
        }

        public InputEvent readFirstChild() throws XMLStreamException {
            SMInputCursor childCrsr = childElementCursor();
            return iter(childCrsr).next();
        }

        public void readFirstChildGeometry(
                Map<QName, FEPullParser.PullParserHandler> handlers,
                List<Pair<Resource, Geometry>> output, Resource rc)
                throws XMLStreamException, IOException, SAXException {
            SMInputCursor childCrsr = childElementCursor();
            iter(childCrsr).next().readGeometry(handlers, output, rc);
        }

        public void readGeometry(
                Map<QName, FEPullParser.PullParserHandler> handlers,
                List<Pair<Resource, Geometry>> output, Resource rc)
                throws XMLStreamException, IOException, SAXException {
            Geometry obj = (Geometry) parseGeometry(handlers);

            if (obj != null) {
                output.add(pair(rc, obj));
            }
        }

        public void readPrimitive(Map<QName, Resource> primitive,
                List<Pair<Resource, Object>> output, Resource rc)
                throws XMLStreamException {
            Object obj = parsePrimitive(primitive);

            if (obj != null) {
                output.add(pair(rc, obj));
            }
        }

        /**
         * reads current as XLink to some TBD returns title for now
         * 
         * @param output
         * @throws XMLStreamException
         */
        public void readXlink(List<Pair<Resource, Object>> output, Resource rc)
                throws XMLStreamException {
            Object obj = parseXlink();

            if (obj != null) {
                output.add(pair(rc, obj));
            }
        }

    };

    public static enum W3XLink {
        /*
         * actuate [/~{http://www.w3.org/1999/xlink}actuate] [0..1] arcrole
         * [/~{http://www.w3.org/1999/xlink}arcrole] [0..1] href
         * [/~{http://www.w3.org/1999/xlink}href] [0..1] nilReason[0..1]
         * owns[0..1] remoteSchema
         * [/~{http://www.opengis.net/gml/3.2}remoteSchema] [0..1] role
         * [/~{http://www.w3.org/1999/xlink}role] [0..1] show
         * [/~{http://www.w3.org/1999/xlink}show] [0..1] title
         * [/~{http://www.w3.org/1999/xlink}title] [0..1] type
         * [/~{http://www.w3.org/1999/xlink}type] [0..1]
         */

        actuate, arcrole, href, remoteSchema, role, show, title, type

        ;

        public static String XMLNS_W3_ORG_1999_XLINK = "http://www.w3.org/1999/xlink";
    }

    public static final List<ImmutablePair<Resource, Geometry>> EMPTY = new ArrayList<ImmutablePair<Resource, Geometry>>();
    protected InputProcessor input;

    protected OutputProcessor output;

    public List<Pair<Resource, Geometry>> geometries(
            Pair<Resource, Geometry>... pairs) {
        /* FIX THIS */
        return new ArrayList<Pair<Resource, Geometry>>(Arrays.asList(pairs));
    }

    public List<Pair<Resource, String>> geometryTypes(
            Pair<Resource, String>... pairs) {
        return Arrays.asList(pairs);

    }

    public Resource iri() {
        return Resource.iri();
    }

    public Resource iri(String base) {
        return Resource.iri(base);
    }

    public Resource iri(String base, String localPart) {
        return Resource.iri(base, localPart);
    }

    public Iterator<InputEvent> iter(final SMInputCursor crsr) {

        return new Iterator<InputEvent>() {

            private InputEvent n;
            private boolean started = false;

            public boolean hasNext() {
                try {
                    n = new InputEvent(crsr.getNext(), crsr);
                    started = true;

                } catch (XMLStreamException e) {
                    e.printStackTrace();
                    return false;
                }
                return n.hasNext();
            }

            public InputEvent next() {
                if (!started) {
                    hasNext();
                }
                return n;
            }

            public void remove() {
            }

        };
    }

    public Map<QName, Resource> mapPrimitiveType(final Resource type,
            final String ns, final String... localNames) {
        Map<QName, Resource> primitive = new HashMap<QName, Resource>();
        for (String localPart : localNames) {
            primitive.put(new QName(ns, localPart), type);
        }

        return primitive;
    }

    public Map<QName, Resource> mapPrimitiveTypes(XSDDatatype type, String ns,
            String... localNames) {
        Map<QName, Resource> primitive = new HashMap<QName, Resource>();
        for (String localPart : localNames) {
            Resource rc = type.toResource();
            primitive.put(new QName(ns, localPart), rc);
        }
        return primitive;
    }

    public Pair<Resource, Geometry> pair(Resource rc, Geometry val) {
        return new ImmutablePair<Resource, Geometry>(rc, val);
    }

    public Pair<Resource, Object> pair(Resource rc, Object val) {
        return new ImmutablePair<Resource, Object>(rc, val);
    }

    public ImmutablePair<Resource, String> pair(Resource rc, String val) {
        return new ImmutablePair<Resource, String>(rc, val);
    }

    public ImmutablePair<Resource, XSDDatatype> pair(Resource rc,
            XSDDatatype val) {
        return new ImmutablePair<Resource, XSDDatatype>(rc, val);
    }

    public abstract Object parseGeometry(
            Map<QName, FEPullParser.PullParserHandler> handlers,
            SMInputCursor crsr) throws XMLStreamException, IOException,
            SAXException;

    public List<Pair<Resource, Object>> properties(
            Pair<Resource, Object>... pairs) {
        /* FIX THIS */
        return new ArrayList<Pair<Resource, Object>>(Arrays.asList(pairs));
    }

    public QName qn(final String ns, final String localName) {
        return new QName(ns, localName);
    }

    public void setInputOutput(InputProcessor inp, OutputProcessor out) {
        input = inp;
        output = out;
    }

    public List<Pair<Resource, Resource>> simpleTypes(
            Pair<Resource, Resource>... pairs) {

        return Arrays.asList(pairs);

    }

    public Resource xsd(XSDDatatype xsd) {
        return XSDDatatype.RESOURCE.get(xsd);
    }

}
