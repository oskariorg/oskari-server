package fi.nls.oskari.work.fe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.utils.URIBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.WFSMapLayerJob;

public class FERequestTemplate {

    class RequestNSContext implements NamespaceContext {

        Map<String, String> ns2prefix = new HashMap<String, String>();
        Map<String, String> prefix2ns = new HashMap<String, String>();

        RequestNSContext() {

        }

        public void add(String prefix, String ns) {
            ns2prefix.put(ns, prefix);
            prefix2ns.put(prefix, ns);
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null)
                throw new NullPointerException("Null prefix");

            String ns = prefix2ns.get(prefix);

            return ns != null ? ns : XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        public Iterator<String> getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }

    }

    public String templateResource;
    public boolean isPost;
    public FEQueryArgsBuilder argsBuilder;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    RequestNSContext nscontext = new RequestNSContext();
    XPathFactory xFactory = XPathFactory.newInstance();

    TransformerFactory tFactory = TransformerFactory.newInstance();
    private String srsName;
    private String featureNs;
    private String featureName;
    private String wfsVer;
    private String geomProp;
    private String geomNs;

    public FERequestTemplate(FEQueryArgsBuilder argsBuilder) {
        this.templateResource = null;
        this.isPost = false;
        this.argsBuilder = argsBuilder;
    }

    public FERequestTemplate(String templateResource) {
        this.templateResource = templateResource;
        this.isPost = true;
        this.argsBuilder = null;
    }

    public FERequestTemplate(String url, boolean isPost) {
        this.templateResource = url;
        this.isPost = isPost;
        this.argsBuilder = null;
    }

    protected void buildBBOXRequest_XPath(StringBuffer params, InputStream inp,
            OutputStream outs, BoundingBox bbox)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, TransformerException {
        String lowerCorner = Double.toString(bbox.getLowerCorner()
                .getCoordinate()[0])
                + " "
                + Double.toString(bbox.getLowerCorner().getCoordinate()[1]);

        String upperCorner = Double.toString(bbox.getUpperCorner()
                .getCoordinate()[0])
                + " "
                + Double.toString(bbox.getUpperCorner().getCoordinate()[1]);

        factory.setNamespaceAware(true);
        factory.setValidating(false); // we have placeholders in XML

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inp);

        XPath xpath = xFactory.newXPath();
        xpath.setNamespaceContext(nscontext);

        {
            XPathExpression expr = xpath.compile("//*[.='[LOWER_CORNER]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);

            nd.setTextContent(lowerCorner);

        }

        {
            XPathExpression expr = xpath.compile("//*[.='[UPPER_CORNER]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);

            nd.setTextContent(upperCorner);

        }

        if (srsName != null) {
            XPathExpression expr = xpath.compile("//*[@srsName='[SRSNAME]']");
            
            String _srsName = srsName;
            if( srsName.indexOf("3857") != -1) {
                  _srsName = "EPSG:900913"; // GeoTools vs Deegree vs PostGIS interop - Google messed this up 
            }
                
            NodeList nds = (NodeList) expr
                    .evaluate(doc, XPathConstants.NODESET);

            if (nds != null && nds.getLength() > 0) {

                for (int n = 0; n < nds.getLength(); n++) {
                    Node nd = nds.item(n);
                    nd.getAttributes().getNamedItem("srsName")
                            .setTextContent(_srsName);
                }
            }

        }

        if (geomProp != null) {

            XPathExpression expr = xpath.compile("//*[.='[GEOMETRYNAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.setTextContent(geomProp);
            }

        }

        if (featureName != null) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='tns:[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent("tns:" + featureName);
            }

        }

        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outs);
        transformer.transform(source, result);

    }

    public void buildParams(StringBuffer params,
            final OWSMapLayerJob.Type type, final WFSLayerStore layer,
            final SessionStore session, final List<Double> bounds,
            final MathTransform transformService,
            final CoordinateReferenceSystem crs) throws TransformException,
            IOException, XPathExpressionException,
            ParserConfigurationException, SAXException, TransformerException {

        BoundingBox bbox = null;

        if (type == WFSMapLayerJob.Type.MAP_CLICK) {

            Coordinate c = session.getMapClick();

            ReferencedEnvelope env = new ReferencedEnvelope(new Envelope(c),
                    crs);
            env.expandBy(400);
            bbox = env.toBounds(layer.getCrs());

        } else if (type == WFSMapLayerJob.Type.HIGHLIGHT) {

            Coordinate c = session.getMapClick();

            ReferencedEnvelope env = new ReferencedEnvelope(new Envelope(c),
                    crs);
            env.expandBy(400);
            bbox = env.toBounds(layer.getCrs());

        } else if (bounds != null) {

            ReferencedEnvelope env = new ReferencedEnvelope(
                    new Envelope(bounds.get(0), bounds.get(2), bounds.get(1),
                            bounds.get(3)), crs);
            bbox = env.toBounds(layer.getCrs());

        } else {
            ReferencedEnvelope env = session.getLocation().getEnvelope();
            bbox = env.toBounds(layer.getCrs());
        }

        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        InputStream inp = getClass().getResourceAsStream(templateResource);
        try {
            buildBBOXRequest_XPath(params, inp, outs, bbox);

        } finally {
            inp.close();
        }

        outs.flush();
        final String query = new String(outs.toByteArray());

        params.append(query);

    }

    public void buildParams(URIBuilder builder, OWSMapLayerJob.Type type,
            WFSLayerStore layer, SessionStore session, List<Double> bounds,
            MathTransform transform, CoordinateReferenceSystem crs) {

        argsBuilder.buildParams(builder, type, layer, session, bounds,
                transform, crs);

    }

    public void setRequestFeatures(String srsName, String featureNs,
            String featureName, String wFSver, String geomProp, String geomNs) {
        this.srsName = srsName;
        this.featureNs = featureNs;
        this.featureName = featureName;
        this.wfsVer = wFSver;
        this.geomProp = geomProp;
        this.geomNs = geomNs;

    }

}