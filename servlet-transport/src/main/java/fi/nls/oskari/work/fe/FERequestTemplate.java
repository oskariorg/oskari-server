package fi.nls.oskari.work.fe;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobHelper;
import fi.nls.oskari.work.JobType;
import org.apache.http.client.utils.URIBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FERequestTemplate {

    public static final double CONVERSION_FACTOR = 2.54/1200; // 12th of an inch  ??
    public static final double M_TO_DEGREE = 8.96861E-06d;
    public static final String DEGREE_UNIT = "°";
    // GetFeature Rid filter template
    public static final String GETFEATURE_RID_TEMPLATE = "/fi/nls/oskari/fe/input/request/wfs/generic/generic_wfs_rid_template.xml";
    // GetFeature Intersect filter template
    public static final String GETFEATURE_INTERSECT_TEMPLATE = "/fi/nls/oskari/fe/input/request/wfs/generic/generic_wfs_intersect_template.xml";


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
    DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
    RequestNSContext nscontext = new RequestNSContext();
    XPathFactory xFactory = XPathFactory.newInstance();

    TransformerFactory tFactory = TransformerFactory.newInstance();
    private String srsName;
    private String featureNs;
    private String featurePrefix;
    private String featureName;
    private String wfsVer;
    private String geomProp;
    private String geomNs;
    private String maxcount;
    private Boolean resolveDepth = false;


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
                                          OutputStream outs, BoundingBox bbox, boolean longSrsName)
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
            if(longSrsName){
                _srsName = ProjectionHelper.longSyntaxEpsg(_srsName);
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
        if (featureName != null && featurePrefix != null && !featurePrefix.isEmpty()) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent(featurePrefix+ ":" + featureName);
            }
            XPathExpression expr2 = xpath
                    .compile("//*[@typeName='[FEATURENAME]']");

            Node nd2 = (Node) expr2.evaluate(doc, XPathConstants.NODE);
            if (nd2 != null) {
                nd2.getAttributes().getNamedItem("typeName")
                        .setTextContent(featurePrefix + ":" + featureName);
            }
        }

        else if (featureName != null) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='tns:[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent("tns:" + featureName);
            }
            XPathExpression expr2 = xpath
                    .compile("//*[@typeName='[FEATURENAME]']");

            Node nd2 = (Node) expr2.evaluate(doc, XPathConstants.NODE);
            if (nd2 != null) {
                nd2.getAttributes().getNamedItem("typeName")
                        .setTextContent(featureName);
            }
        }


        String addns = doc.getDocumentElement().getAttribute("xmlns:tns");

        if (addns != null && "[ADD_NSURI]".equals(addns) && featureName != null && featurePrefix != null ) {
            doc.getDocumentElement().removeAttribute("xmlns:tns");
            doc.getDocumentElement().setAttribute("xmlns:"+featurePrefix,featureNs);
        }
        if (maxcount != null) {
            XPathExpression expr = xpath
                    .compile("//*[@count='[MAXCOUNT]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("count")
                        .setTextContent(maxcount);
            }
            XPathExpression expr2 = xpath
                    .compile("//*[@maxFeatures='[MAXCOUNT]']");

            Node nd2 = (Node) expr2.evaluate(doc, XPathConstants.NODE);
            if (nd2 != null) {
                nd2.getAttributes().getNamedItem("maxFeatures")
                        .setTextContent(maxcount);
            }
        }
        if (!resolveDepth) {
            XPathExpression expr = xpath
                    .compile("//*[@resolveDepth='*']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().removeNamedItem("resolveDepth");
            }
        }

        if (featureNs != null) {
            XPathExpression expr = xpath
                    .compile("//*[@targetNamespace='[ADD_NSURI]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("targetNamespace")
                        .setTextContent(featureNs);
            }

        }

        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outs);
        transformer.transform(source, result);

    }
    protected void buildIntersectRequest_XPath(StringBuffer params, InputStream inp,
                                          OutputStream outs, Polygon polygon, String serviceGeomProp, boolean longSrsName)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, TransformerException {
        String coordinates = polygon.toText().replace("POLYGON","").replace("((","").replace("))","").replace(",","");

        factory.setNamespaceAware(true);
        factory.setValidating(false); // we have placeholders in XML

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inp);

        XPath xpath = xFactory.newXPath();
        xpath.setNamespaceContext(nscontext);

        {
            XPathExpression expr = xpath.compile("//*[.='[COORDINATES]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);

            nd.setTextContent(coordinates);

        }


        if (srsName != null) {
            XPathExpression expr = xpath.compile("//*[@srsName='[SRSNAME]']");

            String _srsName = srsName;
            if( srsName.indexOf("3857") != -1) {
                _srsName = "EPSG:900913"; // GeoTools vs Deegree vs PostGIS interop - Google messed this up
            }
            if(longSrsName){
                _srsName = ProjectionHelper.longSyntaxEpsg(_srsName);
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

        if (serviceGeomProp != null) {
            String[] prefixTest = serviceGeomProp.split(":");
            XPathExpression expr = xpath.compile("//*[.='[GEOMETRYNAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                if (prefixTest.length > 1) {
                    nd.setTextContent(serviceGeomProp);
                } else{
                    nd.setTextContent(featurePrefix + ":" + serviceGeomProp);
                }
            }

        }
        if (featureName != null && featurePrefix != null && !featurePrefix.isEmpty()) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent(featurePrefix+ ":" + featureName);
            }

        }

        else if (featureName != null) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='tns:[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent("tns:" + featureName);
            }

        }


        String addns = doc.getDocumentElement().getAttribute("xmlns:tns");

        if (addns != null && "[ADD_NSURI]".equals(addns) && featureName != null && featurePrefix != null ) {
            doc.getDocumentElement().removeAttribute("xmlns:tns");
            doc.getDocumentElement().setAttribute("xmlns:"+featurePrefix,featureNs);
        }
        if (maxcount != null) {
            XPathExpression expr = xpath
                    .compile("//*[@count='[MAXCOUNT]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("count")
                        .setTextContent(maxcount);
            }

        }


        if (featureNs != null) {
            XPathExpression expr = xpath
                    .compile("//*[@targetNamespace='[ADD_NSURI]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("targetNamespace")
                        .setTextContent(featureNs);
            }

        }

        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outs);
        transformer.transform(source, result);

    }


    protected void buildRIDRequest_XPath(StringBuffer params, InputStream inp,
                                          OutputStream outs, List<String> rids, boolean longSrsName)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, TransformerException {

        factory.setNamespaceAware(true);
        factory.setValidating(false); // we have placeholders in XML

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inp);

        XPath xpath = xFactory.newXPath();
        nscontext.add("fes","http://www.opengis.net/fes/2.0");
        xpath.setNamespaceContext(nscontext);


        if(rids != null && rids.size() < 1){
            return;
        }

        if (srsName != null) {
            XPathExpression expr = xpath.compile("//*[@srsName='[SRSNAME]']");

            String _srsName = srsName;
            if( srsName.indexOf("3857") != -1) {
                _srsName = "EPSG:900913"; // GeoTools vs Deegree vs PostGIS interop - Google messed this up
            }
            if(longSrsName){
                _srsName = ProjectionHelper.longSyntaxEpsg(_srsName);
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


        if (featureName != null && featurePrefix != null && !featurePrefix.isEmpty()) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent(featurePrefix+ ":" + featureName);
            }

        }

        else if (featureName != null) {
            XPathExpression expr = xpath
                    .compile("//*[@typeNames='tns:[FEATURENAME]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("typeNames")
                        .setTextContent("tns:" + featureName);
            }
        }


        String addns = doc.getDocumentElement().getAttribute("xmlns:tns");

        if (addns != null && "[ADD_NSURI]".equals(addns) && featureName != null && featurePrefix != null ) {
            doc.getDocumentElement().removeAttribute("xmlns:tns");
            doc.getDocumentElement().setAttribute("xmlns:"+featurePrefix,featureNs);
        }


        if (featureNs != null) {
            XPathExpression expr = xpath
                    .compile("//*[@targetNamespace='[ADD_NSURI]']");

            Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (nd != null) {
                nd.getAttributes().getNamedItem("targetNamespace")
                        .setTextContent(featureNs);
            }

        }
        // Add ids
        XPathExpression expr = xpath
                .compile("//fes:Filter");

        Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (nd != null) {
          for(String rid : rids){
              Node elem = nd.getOwnerDocument().createElement("fes:ResourceId");
              ((Element) elem).setAttribute("rid", rid);

              nd.appendChild(elem);

          }
        }

        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outs);
        transformer.transform(source, result);

    }

    public void buildParams(StringBuffer params,
                            final JobType type, final WFSLayerStore layer,
                            final SessionStore session, final List<Double> bounds,
                            final MathTransform transformService,
                            final CoordinateReferenceSystem crs) throws TransformException,
            IOException, XPathExpressionException,
            ParserConfigurationException, SAXException, TransformerException {

        BoundingBox bbox = null;
        Polygon polygon = null;
        String serviceGeomProp = null;
        List<String> featureIds = null;
        String template = templateResource;

        if (type == JobType.MAP_CLICK) {

            Coordinate c = session.getMapClick();

            ReferencedEnvelope env = new ReferencedEnvelope(new Envelope(c),
                    crs);
            env.expandBy(GetSearchTolerance(session, crs.getCoordinateSystem().getAxis(0).getUnit().toString()));
            bbox = env.toBounds(crs);
            if (wfsVer.equals("2.0.0") && !isCascading(layer)){
                //  intersect filter not supported in elf  cascading
                template = GETFEATURE_INTERSECT_TEMPLATE;
                polygon = JTS.toGeometry(bbox);
                serviceGeomProp = layer.getGMLGeometryPropertyNoNamespace();
            }

        } else if (type == JobType.HIGHLIGHT) {
            // Use feature Ids
            featureIds = session.getLayers().get(layer.getLayerId()).getHighlightedFeatureIds();
            template = GETFEATURE_RID_TEMPLATE;

        } else if (bounds != null) {
            ReferencedEnvelope env = new ReferencedEnvelope(
                    new Envelope(bounds.get(0), bounds.get(2), bounds.get(1),
                            bounds.get(3)), crs);
            bbox = env.toBounds(crs);

        } else {
            ReferencedEnvelope env = session.getLocation().getEnvelope();
            bbox = env.toBounds(crs);
        }
        String query = null;

            ByteArrayOutputStream outs = new ByteArrayOutputStream();

            InputStream inp = getClass().getResourceAsStream(template);
        try {
            if (polygon != null) {
                if(layer.isReverseXY(session.getLocation().getSrs())){
                    ProjectionHelper.flipFeatureYX(polygon);
                }
                buildIntersectRequest_XPath(params, inp, outs, polygon, serviceGeomProp, layer.isLongSrsName(this.srsName));
            } else if (featureIds != null) {
                buildRIDRequest_XPath(params, inp, outs, featureIds, layer.isLongSrsName(this.srsName));
            } else if (bbox != null) {
                if(layer.isReverseXY(session.getLocation().getSrs())){
                    bbox = new ReferencedEnvelope(bbox.getMinY(), bbox.getMaxY(), bbox.getMinX(), bbox.getMaxX(),crs).toBounds(crs);
                }
                buildBBOXRequest_XPath(params, inp, outs, bbox, layer.isLongSrsName(this.srsName) );
            }


        } finally {
            inp.close();
        }

            outs.flush();
            query = new String(outs.toByteArray());



        params.append(query);

    }

    public void buildParams(URIBuilder builder, JobType type,
                            WFSLayerStore layer, SessionStore session, List<Double> bounds,
                            MathTransform transform, CoordinateReferenceSystem crs) {

        argsBuilder.buildParams(builder, type, layer, session, bounds,
                transform, crs);

    }

    public void setRequestFeatures(String srsName, String featureNs, String featurePrefix,
                                   String featureName, String wFSver, String geomProp, String geomNs, String maxCount,
                                   Boolean resolveDepth) {
        this.srsName = srsName;
        this.featureNs = featureNs;
        this.featurePrefix = featurePrefix;
        this.featureName = featureName;
        this.wfsVer = wFSver;
        this.geomProp = geomProp;
        this.geomNs = geomNs;
        this.maxcount = maxCount;
        this.resolveDepth = resolveDepth;

    }
    private double GetSearchTolerance(final SessionStore session, final String unit){
        // scale unit is always m
        double tol = session.getMapScales().get((int) session.getLocation().getZoom())*CONVERSION_FACTOR;
        if(unit.equals(DEGREE_UNIT)){
            tol = tol * M_TO_DEGREE;
        }
        return tol;
    }

    /**
     * Check, if layer is cascading service layer
     * @param layer
     * @return
     */
    private boolean isCascading( final WFSLayerStore layer){
        return layer.getAttributes().has(JobHelper.PARAM_CASCADING);
    }
}