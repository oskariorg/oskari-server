package fi.nls.oskari.map.analysis.domain;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class UnionMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-union.xml";
    private final String bboxFilter2Template = "<ogc:Filter><ogc:BBOX><ogc:PropertyName>{geom2}</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"{srsName}\"><gml:lowerCorner>{x_lower} {y_lower}</gml:lowerCorner><gml:upperCorner>{x_upper} {y_upper}</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";

    // xml template paths {}

    private final String HREF2 = "{href2}";
    private final String XMLNS2 = "{xmlns2}";
    private final String TYPENAME2 = "{typeName2}";
    private final String FILTER2 = "{filter2}";
    private final String GEOM2 = "{geom2}";

    private String href2 = "";
    private String xmlns2 = "";
    private String typeName2 = "";
    private String filter2 = "";
    private String geom2 = "";

    public String getGeom2() {
        return geom2;
    }

    public void setGeom2(String geom2) {
        this.geom2 = geom2;
    }

    public String getBboxFilter2Template() {
        return bboxFilter2Template;
    }

    public String getHref2() {
        return href2;
    }

    public void setHref2(String href2) {
        this.href2 = href2;
    }

    public String getXmlns2() {
        return xmlns2;
    }

    public void setXmlns2(String xmlns2) {
        this.xmlns2 = xmlns2;
    }

    public String getTypeName2() {
        return typeName2;
    }

    public void setTypeName2(String typeName2) {
        this.typeName2 = typeName2;
    }

    public String getFilter2() {
        return filter2;
    }

    public void setFilter2(String filter2) {
        this.filter2 = filter2;
    }

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        return null;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = this.getTemplate(this.analysisMethodTemplate);

        // Replace {} variables in wps execute .xml
        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, this.getMaxFeatures());
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAME, this.getTypeName());

        doctemp = doctemp.replace(HREF2, this.getHref2());
        doctemp = doctemp.replace(XMLNS2, this.getXmlns2());
        doctemp = doctemp.replace(TYPENAME2, this.getTypeName2());

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

        // Bbox filter 2
        String fbbox2 = this.getBboxFilter2Template();
        fbbox2 = fbbox2.replace(GEOM2, this.getGeom2());
        fbbox2 = fbbox2.replace(SRSNAME, this.getSrsName());
        fbbox2 = fbbox2.replace(X_LOWER, this.getX_lower());
        fbbox2 = fbbox2.replace(Y_LOWER, this.getY_lower());
        fbbox2 = fbbox2.replace(X_UPPER, this.getX_upper());
        fbbox2 = fbbox2.replace(Y_UPPER, this.getY_upper());

        doctemp = doctemp.replace(FILTER2, fbbox2);

        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}