package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class CollectGeometriesMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-collectgeometries.xml";

    // xml template paths {}
   

    public Document getWPSXML() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {
        Document doc = this.getDocument(this.analysisMethodTemplate);

        // Deprecated

       
        // add features and inputs to dataInputs node
        return doc;
    }

    public Document getWPSXML2() throws XPathExpressionException, IOException,
            SAXException, ParserConfigurationException {

        String doctemp = this.getTemplate(this.analysisMethodTemplate);

        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, this.getMaxFeatures()); // capacity problems, if bigger than 100
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAME, this.getTypeName());
       

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
