package fi.nls.oskari.map.analysis.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import fi.nls.oskari.map.analysis.service.AnalysisWPSNamespaceContext;

public abstract class AnalysisMethodParams {

    private final String bboxFilterTemplate = "<ogc:Filter><ogc:BBOX><ogc:PropertyName>{geom}</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"{srsName}\"><gml:lowerCorner>{x_lower} {y_lower}</gml:lowerCorner><gml:upperCorner>{x_upper} {y_upper}</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";

    public final String HREF = "{href}";
    public final String MAXFEATURES = "{maxFeatures}";
    public final String OUTPUTFORMAT = "{outputFormat}";
    public final String VERSION = "{version}";
    public final String SRSNAME = "{srsName}";
    public final String XMLNS = "{xmlns}";
    public final String TYPENAME = "{typeName}";
    public final String FILTER = "{filter}";
    public final String PROPERTIES = "{properties}";
    public final String GEOM = "{geom}";
    public final String X_LOWER = "{x_lower}";
    public final String Y_LOWER = "{y_lower}";
    public final String X_UPPER = "{x_upper}";
    public final String Y_UPPER = "{y_upper}";
    public final String REFERENCE_TYPE_WFS = "wfs";
    public final String REFERENCE_TYPE_GS = "gs_vector";

    private String method = "";
    private String wps_reference_type = "";
    private String href = "";
    private int layer_id = 0;
    private String serviceUrl = "";

    private String maxFeatures = "";
    private String outputFormat = "";
    private String version = "";
    private String srsName = "";
    private String xmlns = "";
    private String typeName = "";
    private String properties = "";
    private String filter = "";
    private String filter2 = "";
    private String geom = "";
    private String x_lower = "";
    private String y_lower = "";
    private String x_upper = "";
    private String y_upper = "";

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

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
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

    public String getFilter2() {
        return filter2;
    }

    public void setFilter2(String filter2) {
        this.filter2 = filter2;
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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
