package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class DifferenceMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate1 = "difference-wfs-join.xml";

    private final String filter2Template = "<fes:Filter><fes:And><fes:PropertyIsEqualTo><fes:ValueReference>layer1/{keyA1}</fes:ValueReference><fes:ValueReference>layer2/{keyB1}</fes:ValueReference></fes:PropertyIsEqualTo>{BBOX}</fes:And></fes:Filter>";
    private final String bboxFilter2Template = "<fes:BBOX><fes:ValueReference>layer1/{geom}</fes:ValueReference><gml:Envelope srsDimension=\"2\" srsName=\"{srsName}\"><gml:lowerCorner>{x_lower} {y_lower}</gml:lowerCorner><gml:upperCorner>{x_upper} {y_upper}</gml:upperCorner></gml:Envelope></fes:BBOX>";

    // xml template paths {}
    private final String TYPENAMES = "{typenames}";
    private final String FIELDA1 = "{fieldA1}";
    private final String FIELDB1 = "{fieldB1}";
    private final String KEYA1 = "{keyA1}";
    private final String KEYB1 = "{keyB1}";
    private final String BBOX = "{BBOX}";


    private String fieldA1 = "";
    private String fieldB1 = "";
    private String keyA1 = "";
    private String keyB1 = "";
    private String wps_reference_type2 = "";
    private String href2 = "";
    private String xmlns2 = "";
    private String typeName2 = "";
    private String filter2 = "";
    private String geom2 = "";
    private Boolean isBbox = true;
    private String noDataValue = null;


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

    public String getKeyA1() {
        return keyA1;
    }

    public void setKeyA1(String keyA1) {
        this.keyA1 = keyA1;
    }

    public String getKeyB1() {
        return keyB1;
    }

    public void setKeyB1(String keyB1) {
        this.keyB1 = keyB1;
    }

    public String getWps_reference_type2() {
        return wps_reference_type2;
    }

    public void setWps_reference_type2(String wps_reference_type2) {
        this.wps_reference_type2 = wps_reference_type2;
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

    public String getGeom2() {
        return geom2;
    }

    public void setGeom2(String geom2) {
        this.geom2 = geom2;
    }

    public String getBboxFilter2Template() {
        return bboxFilter2Template;
    }

    public String getFilter2Template() {
        return filter2Template;
    }
    public Boolean getBbox() {
        return isBbox;
    }

    public void setBbox(Boolean bbox) {
        isBbox = bbox;
    }

    public String getNoDataValue() {
        return noDataValue;
    }

    public void setNoDataValue(String noDataValue) {
        this.noDataValue = noDataValue;
    }

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        return null;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {
        return null;
    }


    public String getWFSXML2() throws IOException, ParserConfigurationException  {

        String doctemp = null;

        if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_WFS) && this.getWps_reference_type2().equals(this.REFERENCE_TYPE_WFS))
            doctemp = this.getTemplate(this.analysisMethodTemplate1);


        if(doctemp == null) return null;

        // Replace {} variables in wps execute .xml
        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, this.getMaxFeatures());
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAMES, this.getTypeName()+ " " + this.getTypeName2());


        doctemp = doctemp.replace(FIELDA1, this.getFieldA1());
        doctemp = doctemp.replace(FIELDB1, this.getFieldB1());

        //Join filter
        String wfs2filter = this.getFilter2Template();
        // Replace keys
        wfs2filter = wfs2filter.replace(KEYA1, this.getKeyA1());
        wfs2filter = wfs2filter.replace(KEYB1, this.getKeyB1());
            // Bbox filter
        String fbbox = "";
        if (this.getBbox()) {
            fbbox = this.getBboxFilter2Template();
            fbbox = fbbox.replace(GEOM, this.getGeom());
            fbbox = fbbox.replace(SRSNAME, this.getSrsName());
            fbbox = fbbox.replace(X_LOWER, this.getX_lower());
            fbbox = fbbox.replace(Y_LOWER, this.getY_lower());
            fbbox = fbbox.replace(X_UPPER, this.getX_upper());
            fbbox = fbbox.replace(Y_UPPER, this.getY_upper());
        }
        wfs2filter = wfs2filter.replace(BBOX, fbbox);

        doctemp = doctemp.replace(FILTER, wfs2filter);


        return doctemp;
    }

}

