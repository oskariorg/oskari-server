package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class UnionGeomMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-union-geom.xml";

    // xml template paths {}
    private final String GEOM_COLLECTION = "{geom_collection}";
    
    private String geomCollection = null;
   

    public String getGeomCollection() {
        return geomCollection;
    }

    public void setGeomCollection(String geomCollection) {
        this.geomCollection = geomCollection;
    }

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
      
        doctemp = doctemp.replace(GEOM_COLLECTION, this.getGeomCollection());
              
        Document doc = this.getDocument2(doctemp);

        return doc;
    }

}
