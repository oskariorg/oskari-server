package fi.nls.oskari.map.analysis.domain;

import fi.nls.oskari.map.analysis.service.AnalysisWPSNamespaceContext;
import fi.nls.oskari.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public abstract class AnalysisMethodParams {

    private final String bboxFilterTemplate = "<ogc:Filter><ogc:BBOX><ogc:PropertyName>{geom}</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"{srsName}\"><gml:lowerCorner>{x_lower} {y_lower}</gml:lowerCorner><gml:upperCorner>{x_upper} {y_upper}</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";
    private static final String  NO_DATA_FILTER_TEMPLATE = "<ogc:And><ogc:PropertyIsNotEqualTo matchCase=\"false\"><ogc:PropertyName>{propertyName}</ogc:PropertyName><ogc:Literal>{propertyValue}</ogc:Literal></ogc:PropertyIsNotEqualTo></ogc:And></ogc:And></ogc:Filter>";
    private static final String  NO_DATACOUNT_FILTER_TEMPLATE = "<ogc:And><ogc:PropertyIsEqualTo matchCase=\"false\"><ogc:PropertyName>{propertyName}</ogc:PropertyName><ogc:Literal>{propertyValue}</ogc:Literal></ogc:PropertyIsEqualTo></ogc:And></ogc:And></ogc:Filter>";

    public final String wfsReferenceTemplate = "wfs-reference.xml";
    public final String dataReferenceTemplate = "data-reference.xml";
    public final String vectorReferenceTemplate = "vector-reference.xml";

    public final String REFERENCE1 = "{Reference1}";
    public final String REFERENCE2 = "{Reference2}";
    public final String HREF = "{href}";
    public final String MAXFEATURES = "{maxFeatures}";
    public final String OUTPUTFORMAT = "{outputFormat}";
    public final String VERSION = "{version}";
    public final String SRSNAME = "{srsName}";
    public final String XMLNS = "{xmlns}";
    public final String TYPENAME = "{typeName}";
    public final String LOCALTYPENAME = "{localTypeName}";
    public final String FILTER = "{filter}";
    public final String PROPERTIES = "{properties}";
    public final String GEOM = "{geom}";
    public final String X_LOWER = "{x_lower}";
    public final String Y_LOWER = "{y_lower}";
    public final String X_UPPER = "{x_upper}";
    public final String Y_UPPER = "{y_upper}";
    public final String GEOJSONFEATURES = "{geoJsonFeatures}";
    public final String CLEAN_CHARS = "(\\r|\\n)";
    public final String FILTER_END ="</ogc:And></ogc:Filter>";

    public final String REFERENCE_TYPE_WFS = "wfs";
    public final String REFERENCE_TYPE_GS = "gs_vector";
    public final String INPUT_GEOJSON = "geojson";

    private String method = "";
    private String wps_reference_type = "";
    private String href = "";
    private int layer_id = 0;
    private String layer_id2 = null;
    private String serviceUrl = "";
    private String serviceUser = "";
    private String servicePw = "";

    private String maxFeatures = "";
    private String outputFormat = "";
    private String version = "";
    private String srsName = "";
    private String xmlns = "";
    private String typeName = "";
    private String localTypeName = "";
    private String properties = "";
    private String filter = "";
    private String geom = "";
    private String x_lower = "";
    private String y_lower = "";
    private String x_upper = "";
    private String y_upper = "";
    private String geojson = "";
    private String responsePrefix = "feature";
    private String noDataValue = null;
    private boolean doNoDataCount = false;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getWps_reference_type() {
        return wps_reference_type;
    }

    public void setWps_reference_type(String wpsReferenceType) {
        wps_reference_type = wpsReferenceType;
    }

    public String getBboxFilterTemplate() {
        return bboxFilterTemplate;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public int getLayer_id() {
        return layer_id;
    }

    public void setLayer_id(int layerId) {
        layer_id = layerId;
    }

    public String getLayer_id2() {
        return layer_id2;
    }

    public void setLayer_id2(String layer_id2) {
        this.layer_id2 = layer_id2;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getServiceUser() {
        return serviceUser;
    }

    public void setServiceUser(String serviceUser) {
        this.serviceUser = serviceUser;
    }

    public String getServicePw() {
        return servicePw;
    }

    public void setServicePw(String servicePw) {
        this.servicePw = servicePw;
    }

    public String getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(String maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getLocalTypeName() {
        return localTypeName;
    }

    public void setLocalTypeName(String localTypeName) {
        this.localTypeName = localTypeName;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getX_lower() {
        return x_lower;
    }

    public void setX_lower(String xLower) {
        x_lower = xLower;
    }

    public String getY_lower() {
        return y_lower;
    }

    public void setY_lower(String yLower) {
        y_lower = yLower;
    }

    public String getX_upper() {
        return x_upper;
    }

    public void setX_upper(String xUpper) {
        x_upper = xUpper;
    }

    public String getY_upper() {
        return y_upper;
    }

    public void setY_upper(String yUpper) {
        y_upper = yUpper;
    }

    public String getNoDataValue() {
        return noDataValue;
    }

    public void setNoDataValue(String noDataValue) {
        this.noDataValue = noDataValue;
    }

    public boolean isDoNoDataCount() {
        return doNoDataCount;
    }

    public void setDoNoDataCount(boolean doNoDataCount) {
        this.doNoDataCount = doNoDataCount;
    }

    public String getGeojson() {
        if(geojson == null) return "";
        return geojson;
    }

    public void setGeojson(String geojson) {
        this.geojson = geojson;
    }

    /**
     * prefix of featuretypes in wps response or in other unknown feature case (e.g. geojson gml encode)
     * @return
     */
    public String getResponsePrefix() {
        return responsePrefix;
    }

    public void setResponsePrefix(String responsePrefix) {
        this.responsePrefix = responsePrefix;
    }

    protected XPath getXPath() {
        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xpath = xFactory.newXPath();
        AnalysisWPSNamespaceContext nscontext = new AnalysisWPSNamespaceContext();

        xpath.setNamespaceContext(nscontext);
        return xpath;
    }

    protected Node setLiteralDataContent(Document doc, String key, String value)
            throws XPathExpressionException {
        XPath xpath = this.getXPath();

        XPathExpression expr = xpath.compile("//wps:Data[wps:LiteralData='"
                + key + "']/wps:LiteralData");

        Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (nd == null)
            return null;
        nd.setTextContent(value);
        return nd;
    }

    protected Node setQueryDataContent(Document doc, String key, String value)
            throws XPathExpressionException {
        XPath xpath = this.getXPath();

        XPathExpression expr = xpath.compile("//wps:Data[wps:LiteralData='"
                + key + "']/wps:LiteralData");

        Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (nd == null)
            return null;
        nd.setTextContent(value);
        return nd;
    }

    protected Document getDocument(String templatePath)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
        DocumentBuilder builder;
        Document doc = null;

        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        InputStream inp = this.getClass().getResourceAsStream(templatePath);
        doc = builder.parse(inp);
        inp.close();

        return doc;
    }

    protected Document getDocument2(String templatedoc)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
        DocumentBuilder builder;
        Document doc = null;


        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();

        doc = builder.parse(new InputSource(new ByteArrayInputStream(
                templatedoc.getBytes("utf-8"))));

        return doc;
    }

    protected String getTemplate(String templatePath)
            throws ParserConfigurationException, IOException {
        String template = "";

        InputStream inp = this.getClass().getResourceAsStream(templatePath);
        template = new Scanner(inp).useDelimiter("\\A").next();
        inp.close();

        return template;
    }

    /**
     *  Set input reference for wps execute
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     */

    protected String getReference1() throws ParserConfigurationException, IOException {
        String reference1 = null;
        if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_GS))
            reference1 = this.getTemplate(this.vectorReferenceTemplate);
        else if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_WFS))
            reference1 = this.getTemplate(this.wfsReferenceTemplate);
        else if (this.getWps_reference_type().equals(this.INPUT_GEOJSON))
            reference1 = this.getTemplate(this.dataReferenceTemplate);

        if (reference1 != null) {
            // Replace {} variables in wps execute .xml
            reference1 = reference1.replace(HREF, this.getHref());
            reference1 = reference1.replace(MAXFEATURES, this.getMaxFeatures());
            reference1 = reference1.replace(OUTPUTFORMAT, this.getOutputFormat());
            reference1 = reference1.replace(VERSION, this.getVersion());
            reference1 = reference1.replace(SRSNAME, this.getSrsName());
            reference1 = reference1.replace(XMLNS, this.getXmlns());
            reference1 = reference1.replace(TYPENAME, this.getTypeName());
            reference1 = reference1.replace(GEOJSONFEATURES, this.getGeojson());
        }
        return reference1;

    }

    protected String getWfsFilter1() {
        // Filter
        String wfsfilter = "";
        if (this.getFilter() != null) {
            wfsfilter = this.getFilter();
        } else {

            String fbbox = this.getBboxFilterTemplate();
            fbbox = fbbox.replace(GEOM, this.getGeom());
            fbbox = fbbox.replace(SRSNAME, this.getSrsName());
            fbbox = fbbox.replace(X_LOWER, this.getX_lower());
            fbbox = fbbox.replace(Y_LOWER, this.getY_lower());
            fbbox = fbbox.replace(X_UPPER, this.getX_upper());
            fbbox = fbbox.replace(Y_UPPER, this.getY_upper());
            wfsfilter = fbbox;
        }
        return wfsfilter;
    }

    protected String appendNoDataFilter(String wfsfilter_in, String field) {
        String wfsfilter = wfsfilter_in.replaceAll(CLEAN_CHARS, "");
        String nodatafilter = NO_DATA_FILTER_TEMPLATE.replace("{propertyName}", field);
        nodatafilter = nodatafilter.replace("{propertyValue}", this.getNoDataValue());

        if (wfsfilter.indexOf(FILTER_END) == -1) {
            // no and conditions
            wfsfilter = wfsfilter.replace("<ogc:Filter>", "<ogc:Filter><ogc:And>");
            wfsfilter = wfsfilter.replace("</ogc:Filter>", nodatafilter);
        } else {
            wfsfilter = wfsfilter.replace(FILTER_END, nodatafilter);
        }
        return wfsfilter;
    }
    protected String appendNoDataCountFilter(String wfsfilter, String field){
        String nodatafilter = NO_DATACOUNT_FILTER_TEMPLATE.replace("{propertyName}", field);
        nodatafilter =  nodatafilter.replace("{propertyValue}", this.getNoDataValue());
        wfsfilter = wfsfilter.replaceAll( CLEAN_CHARS, "");
        if (wfsfilter.indexOf(FILTER_END) == -1) {
            // no and conditions
            wfsfilter = wfsfilter.replace("<ogc:Filter>", "<ogc:Filter><ogc:And>");
            wfsfilter = wfsfilter.replace("</ogc:Filter>", nodatafilter);
        } else {
            wfsfilter = wfsfilter.replace(FILTER_END, nodatafilter);
        }
        return wfsfilter;
    }

    /**
     * Returns the WPS XML Document
     *
     * @return Returns a Web Processing Service XML Document
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public abstract Document getWPSXML() throws XPathExpressionException,
            IOException, SAXException, ParserConfigurationException;

    /**
     * Returns the WPS XML Document
     *
     * @return Returns a Web Processing Service XML Document
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public abstract Document getWPSXML2() throws XPathExpressionException,
            IOException, SAXException, ParserConfigurationException;

}
