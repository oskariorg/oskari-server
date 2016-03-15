package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpatialJoinStatisticsMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "layer-wps-spatial-join-statistics.xml";

    // xml template placeholder paths {}
    private final String DATA_ATTRIBUTE = "{dataAttribute}";

    private String href2 = "";
    private String xmlns2 = "";
    private String typeName2 = "";
    private String filter2 = "";
    private String geom2 = "";
    private String dataAttribute = "";
    private String properties2 = "";
    private String geojson2 = "";
    private String wps_reference_type2 = "";
    Map<String, String> localemap = new HashMap<String, String>();
    private String intersection_mode = "";  // SECOND intersect features (default) or SECOND_CONTAINS contains features


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

    public String getDataAttribute() {
        return dataAttribute;
    }

    public void setDataAttribute(String dataAttribute) {
        this.dataAttribute = dataAttribute;
    }

    public Map<String, String> getLocalemap() {
        return localemap;
    }

    public void setLocalemap(Map<String, String> localemap) {
        this.localemap = localemap;
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

        if (this.getNoDataValue() != null) {
            if(isDoNoDataCount()){
                // No data count filter  - use WPS count aggregate method
                // and calculate the count of no data value items
                wfsfilter = this.appendNoDataCountFilter(wfsfilter, this.getDataAttribute());
            } else {
                // Append no_data filter
                wfsfilter = this.appendNoDataFilter(wfsfilter, this.getDataAttribute());
            }
        }

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

        //data attribute name for statistics computation
        doctemp = doctemp.replace(DATA_ATTRIBUTE, this.getDataAttribute());


        Document doc = this.getDocument2(doctemp);

        return doc;
    }


}