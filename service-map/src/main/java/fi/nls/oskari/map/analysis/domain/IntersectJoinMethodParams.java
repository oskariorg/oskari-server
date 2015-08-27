package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class IntersectJoinMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "layer-wps-intersect-join.xml";
    private final String retainDataInput1stTemplate =  "<wps:Input><ows:Identifier>first attributes to retain</ows:Identifier><wps:Data><wps:LiteralData>{retainfield}</wps:LiteralData></wps:Data></wps:Input>";
    private final String retainDataInput2ndTemplate =  "<wps:Input><ows:Identifier>second attributes to retain</ows:Identifier><wps:Data><wps:LiteralData>{retainfield}</wps:LiteralData></wps:Data></wps:Input>";

    // xml template paths {}

    private final String RETAIN_FIELD = "{retainfield}";
    private final String RETAIN_FIELDSA = "{retainfieldsA}";
    private final String RETAIN_FIELDSB = "{retainfieldsB}";

    private final String INTERSECTIONMODE = "{intersectionMode}";
    private final String INTERSECT_CONTAINS = "contains";
    private final String INTERSECT_CLIP = "clip";

    private String href2 = "";
    private String xmlns2 = "";
    private String typeName2 = "";
    private String filter2 = "";
    private String geom2 = "";
    private String retainfieldsA = "";
    private String retainfieldsB = "";
    private String properties2 = "";
    private String geojson2 = "";
    private String wps_reference_type2 = "";
    private String intersection_mode = "";  // SECOND intersect features (default) or SECOND_CONTAINS contains features

    public String getRetainfieldsA() {
        return retainfieldsA;
    }

    public void setRetainfieldsA(String retainfieldsA) {
        this.retainfieldsA = retainfieldsA;
    }

    public String getRetainfieldsB() {
        return retainfieldsB;
    }

    public void setRetainfieldsB(String retainfieldsB) {
        this.retainfieldsB = retainfieldsB;
    }

    public String getGeom2() {
        return geom2;
    }

    public void setGeom2(String geom2) {
        this.geom2 = geom2;
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

    public String getIntersection_mode() {
        if (intersection_mode.equals(INTERSECT_CONTAINS)) return "SECOND_CONTAINS";
        else if (intersection_mode.equals(INTERSECT_CLIP)) return "SECOND_CLIP";
        else return "SECOND";  //"INTERSECTION" mode doesn't work
    }

    public void setIntersection_mode(String intersection_mode) {
        this.intersection_mode = intersection_mode;
    }

    public String getGeojson2() {
        if(geojson2 == null) return "";
        return geojson2;
    }

    public void setGeojson2(String geojson2) {
        this.geojson2 = geojson2;
    }

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        return null;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = this.getTemplate(this.analysisMethodTemplate);
        String reference1 = this.getReference1();
        String reference2 = null;

        if (this.getWps_reference_type2().equals(this.REFERENCE_TYPE_GS))
            reference2 = this.getTemplate(this.vectorReferenceTemplate);
        else if (this.getWps_reference_type2().equals(this.REFERENCE_TYPE_WFS))
            reference2 = this.getTemplate(this.wfsReferenceTemplate);
        else if (this.getWps_reference_type2().equals(this.INPUT_GEOJSON))
            reference2 = this.getTemplate(this.dataReferenceTemplate);


        if(doctemp == null || reference1 == null || reference2 == null) return null;

        // Replace {} variables for wps execute .xml  (layer 2 reference)

        reference2 =reference2.replace(HREF, this.getHref2());
        reference2 =reference2.replace(MAXFEATURES, this.getMaxFeatures());
        reference2 =reference2.replace(OUTPUTFORMAT, this.getOutputFormat());
        reference2 =reference2.replace(VERSION, this.getVersion());
        reference2 =reference2.replace(SRSNAME, this.getSrsName());
        reference2 =reference2.replace(XMLNS, this.getXmlns2());
        reference2 =reference2.replace(TYPENAME, this.getTypeName2());
        reference2 = reference2.replace(GEOJSONFEATURES, this.getGeojson2());

        //Properties and filter - reference 1
        if (this.getProperties() != null) {
            reference1 = reference1.replace(PROPERTIES, this.getProperties());
        }
        else
        {
            reference1 = reference1.replace(PROPERTIES, "");
        }
        
        // Filter
        String wfsfilter = this.getWfsFilter1();

        reference1 = reference1.replace(FILTER, wfsfilter);

        //Properties and filter - reference 2
        if (this.getProperties2() != null) {
            reference2 = reference2.replace(PROPERTIES, this.getProperties2());
        }
        else
        {
            reference2 = reference2.replace(PROPERTIES, "");
        }

        String wfsfilter2 = "";
        if (this.getFilter2() != null ) {
            wfsfilter2 = this.getFilter2();
        } else {

            // Bbox filter 2
            String fbbox2 = this.getBboxFilterTemplate();
            fbbox2 = fbbox2.replace(GEOM, this.getGeom2());
            fbbox2 = fbbox2.replace(SRSNAME, this.getSrsName());
            fbbox2 = fbbox2.replace(X_LOWER, this.getX_lower());
            fbbox2 = fbbox2.replace(Y_LOWER, this.getY_lower());
            fbbox2 = fbbox2.replace(X_UPPER, this.getX_upper());
            fbbox2 = fbbox2.replace(Y_UPPER, this.getY_upper());

            wfsfilter2 = fbbox2;
        }

        reference2 = reference2.replace(FILTER, wfsfilter2);

        doctemp = doctemp.replace(REFERENCE1, reference1);
        doctemp = doctemp.replace(REFERENCE2, reference2);

        //Retain fields A
        doctemp = doctemp.replace(RETAIN_FIELDSA, getRetainData(this.getRetainfieldsA(), retainDataInput1stTemplate));
        doctemp = doctemp.replace(RETAIN_FIELDSB, getRetainData(this.getRetainfieldsB(), retainDataInput2ndTemplate));

        // Intersection mode
        doctemp = doctemp.replace(INTERSECTIONMODE, this.getIntersection_mode());


        Document doc = this.getDocument2(doctemp);

        return doc;
    }

    /**
     * generate input sections for retain fields
     * @param fields  field names
     * @param template
     * @return
     */
    private String getRetainData(String fields, String template) {

        StringBuilder data = new StringBuilder();
        for (String field: fields.split(","))
        {
            String input = template.replace(RETAIN_FIELD,field);
            data.append(input);
        }
        return data.toString();
    }
}