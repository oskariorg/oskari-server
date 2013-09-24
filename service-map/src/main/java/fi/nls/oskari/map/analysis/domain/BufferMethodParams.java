package fi.nls.oskari.map.analysis.domain;

// WPS execute parameters / case: WFS REFERENCE input 

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class BufferMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-buffer.xml";
    private final String analysisMethodTemplate2 = "analysis2analysis-layer-wps-buffer.xml";

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
        // Deprecated
        Document doc = null;
        if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_GS))
            doc = this.getDocument(this.analysisMethodTemplate2);
        else
            doc = this.getDocument(this.analysisMethodTemplate);

        // TODO: set wfs query parameters

        // Set input values
        this.setLiteralDataContent(doc, DISTANCE, this.getDistance());
        this.setLiteralDataContent(doc, ATTRIBUTENAME, this.getAttributeName());

        // add features and inputs to dataInputs node
        return doc;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = null;
        if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_GS))
            doctemp = this.getTemplate(this.analysisMethodTemplate2);
        else
            doctemp = this.getTemplate(this.analysisMethodTemplate);
        
        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, "100"); // capasity problems, if
                                                       // bigger than 100 -
                                                       // replace later with
                                                       // this.getMaxFeatures());
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAME, this.getTypeName());
        doctemp = doctemp.replace(DISTANCE, this.getDistance());
        doctemp = doctemp.replace(ATTRIBUTENAME, "");
        
        //Properties
        if (this.getProperties() != null) {
            doctemp = doctemp.replace(PROPERTIES, this.getProperties());
        }
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

        doctemp = doctemp.replace(FILTER, wfsfilter);

        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}
