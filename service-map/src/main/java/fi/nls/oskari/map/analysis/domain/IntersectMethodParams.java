package fi.nls.oskari.map.analysis.domain;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class IntersectMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate1 = "wfs2wfs-layer-wps-intersect2.xml";
    private final String analysisMethodTemplate2 = "analysis2analysis-layer-wps-intersect2.xml";
    private final String analysisMethodTemplate3 = "analysis2wfs-layer-wps-intersect2.xml";
    private final String analysisMethodTemplate4 = "wfs2analysis-layer-wps-intersect2.xml";
    private final String bboxFilter2Template = "<ogc:Filter><ogc:BBOX><ogc:PropertyName>{geom2}</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"{srsName}\"><gml:lowerCorner>{x_lower} {y_lower}</gml:lowerCorner><gml:upperCorner>{x_upper} {y_upper}</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter>";

    // xml template paths {}

    private final String HREF2 = "{href2}";
    private final String XMLNS2 = "{xmlns2}";
    private final String TYPENAME2 = "{typeName2}";
    private final String FILTER2 = "{filter2}";
    private final String GEOM2 = "{geom2}";
    private final String FIELDA1 = "{fieldA1}";
    private final String FIELDB1 = "{fieldB1}";
    private final String SRSNAME2 = "{srsName2}";

    private String href2 = "";
    private String xmlns2 = "";
    private String typeName2 = "";
    private String filter2 = "";
    private String geom2 = "";
    private String fieldA1 = "";
    private String fieldB1 = "";
    private String properties2 = "";
    private String wps_reference_type2 = "";

    public String getFieldA1() {
        return fieldA1;
    }

    public void setFieldA1(String fieldA1) {
        this.fieldA1 = fieldA1;
    }

    public String getFieldB1() {
        return fieldB1;
    }

    public void setFieldB1(String fieldB1) {
        this.fieldB1 = fieldB1;
    }

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

    public String getProperties2() {
        return properties2;
    }

    public void setProperties2(String properties2) {
        this.properties2 = properties2;
    }

    public String getWps_reference_type2() {
        return wps_reference_type2;
    }

    public void setWps_reference_type2(String wpsReferenceType2) {
        wps_reference_type2 = wpsReferenceType2;
    }

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        return null;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = null;
        if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_GS) && this.getWps_reference_type2().equals(this.REFERENCE_TYPE_GS))
            doctemp = this.getTemplate(this.analysisMethodTemplate2);
        else if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_WFS) && this.getWps_reference_type2().equals(this.REFERENCE_TYPE_WFS))
            doctemp = this.getTemplate(this.analysisMethodTemplate1);
        else if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_GS) && this.getWps_reference_type2().equals(this.REFERENCE_TYPE_WFS))
            doctemp = this.getTemplate(this.analysisMethodTemplate4);
        else if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_WFS) && this.getWps_reference_type2().equals(this.REFERENCE_TYPE_GS))
            doctemp = this.getTemplate(this.analysisMethodTemplate3);

        if(doctemp == null) return null;

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
        doctemp = doctemp.replace(FIELDA1, this.getFieldA1());
        doctemp = doctemp.replace(FIELDB1, this.getFieldB1());
        doctemp = doctemp.replace(SRSNAME2, this.getSrsName());
        
       
        //Properties
        if (this.getProperties() != null) {
            doctemp = doctemp.replace(PROPERTIES, this.getProperties());
        }
        else
        {
            doctemp = doctemp.replace(PROPERTIES, "");
        }
        
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
        

        String wfsfilter2 = "";
        if (this.getFilter2() != null ) {
            wfsfilter2 = this.getFilter2();
        } else {

            // Bbox filter 2
            String fbbox2 = this.getBboxFilter2Template();
            fbbox2 = fbbox2.replace(GEOM2, this.getGeom2());
            fbbox2 = fbbox2.replace(SRSNAME, this.getSrsName());
            fbbox2 = fbbox2.replace(X_LOWER, this.getX_lower());
            fbbox2 = fbbox2.replace(Y_LOWER, this.getY_lower());
            fbbox2 = fbbox2.replace(X_UPPER, this.getX_upper());
            fbbox2 = fbbox2.replace(Y_UPPER, this.getY_upper());

            wfsfilter2 = fbbox2;
        }

        doctemp = doctemp.replace(FILTER2, wfsfilter2);
        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}