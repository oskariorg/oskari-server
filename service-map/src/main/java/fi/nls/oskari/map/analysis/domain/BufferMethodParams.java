package fi.nls.oskari.map.analysis.domain;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class BufferMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-buffer.xml";

    // xml template paths {}
    private final String DISTANCE = "{distance}";

    // distance from feature attribute field
    private final String ATTRIBUTENAME = "{attributeName}";
    // private final String HREF = "{href}";

    private String distance = "";
    private String attributeName = "";

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {
        Document doc = this.getDocument(this.analysisMethodTemplate);

        // TODO: set wfs query parameters

        // Set input values
        this.setLiteralDataContent(doc, DISTANCE, this.getDistance());
        this.setLiteralDataContent(doc, ATTRIBUTENAME, this.getAttributeName());

        // add features and inputs to dataInputs node
        return doc;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = this.getTemplate(this.analysisMethodTemplate);

        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, "100"); // capasity problems, if bigger than 100 - replace later with  this.getMaxFeatures());
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAME, this.getTypeName());
        doctemp = doctemp.replace(DISTANCE, this.getDistance());
        doctemp = doctemp.replace(ATTRIBUTENAME, "");

        // Filter
        String wfsfilter = "";
        if (this.getFilter() != null ) {
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

        doctemp = doctemp.replace(FILTER, wfsfilter);

        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}
