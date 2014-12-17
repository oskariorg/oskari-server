package fi.nls.oskari.map.analysis.domain;

// WPS execute parameters / case: WFS REFERENCE input 

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class ZoneSectorMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "layer-wps-zone.xml";

    // xml template paths {}
    private final String DISTANCE = "{distance}";

    // distance from feature attribute field
    private final String ZONE_COUNT = "{zone_count}";
    private final String SECTOR_COUNT = "{sector_count}";
    // private final String HREF = "{href}";

    private String distance = "";
    private String zone_count = "";
    private String sector_count = "";

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public String getZone_count() {
        return zone_count;
    }

    public void setZone_count(String zone_count) {
        this.zone_count = zone_count;
    }

    public String getSector_count() {
        return sector_count;
    }

    public void setSector_count(String sector_count) {
        this.sector_count = sector_count;
    }


    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        return null;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = this.getTemplate(this.analysisMethodTemplate);
        String reference1 = this.getReference1();

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

        doctemp = doctemp.replace(REFERENCE1, reference1);

        //Method spesific params
        doctemp = doctemp.replace(DISTANCE, this.getDistance());
        doctemp = doctemp.replace(ZONE_COUNT, this.getZone_count());
        doctemp = doctemp.replace(SECTOR_COUNT, this.getSector_count());

        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}
