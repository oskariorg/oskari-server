package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class UnionMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-geomunion.xml";
    private final String analysisMethodTemplate2 = "analysis2analysis-layer-wps-geomunion.xml";
    private final String analysisMethodTemplate3 = "analysis2geojson-layer-wps-geomunion.xml";

    private static final String DEFAULT_MIMETYPE = "text/xml; subtype=wfs-collection/1.1";
    private static final String MIMETYPE = "{mimetype}";

    private String mimeTypeFormat = null;

    public String getMimeTypeFormat() {
        return mimeTypeFormat;
    }

    public void setMimeTypeFormat(String mimeTypeFormat) {
        this.mimeTypeFormat = mimeTypeFormat;
    }

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        return null;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = null;
        if (this.getWps_reference_type().equals(this.REFERENCE_TYPE_GS))
            doctemp = this.getTemplate(this.analysisMethodTemplate2);
        else if (this.getWps_reference_type().equals(this.INPUT_GEOJSON))
            doctemp = this.getTemplate(this.analysisMethodTemplate3);
        else
            doctemp = this.getTemplate(this.analysisMethodTemplate);


        // Replace {} variables in wps execute .xml
        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, this.getMaxFeatures());
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAME, this.getTypeName());
        doctemp = doctemp.replace(LOCALTYPENAME, this.getLocalTypeName());
        doctemp = doctemp.replace(GEOJSONFEATURES, this.getGeojson());

        //Final response output format
        if(this.getMimeTypeFormat() != null){
            doctemp = doctemp.replace(MIMETYPE, this.getMimeTypeFormat());
        } else {
            doctemp = doctemp.replace(MIMETYPE, DEFAULT_MIMETYPE);
        }

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
        
        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}