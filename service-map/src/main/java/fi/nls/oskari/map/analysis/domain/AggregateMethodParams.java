package fi.nls.oskari.map.analysis.domain;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AggregateMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-aggregate.xml";
    private final String analysisMethodTemplate2 = "analysis2analysis-layer-wps-aggregate.xml";
    private final String functionsTemplate = "<wps:Input><ows:Identifier>function</ows:Identifier><wps:Data><wps:LiteralData>{functions}</wps:LiteralData></wps:Data></wps:Input>";

    // xml template paths {}
    private final String AGGREFIELD1 = "{aggreField1}";

    private final String FUNCTIONS = "{functions}";
    private final String AGGREFUNCTIONS = "{aggreFunctions}";

    private String aggreField1 = "";
    private List<String> aggreFunctions = null;

    public String getAggreField1() {
        return aggreField1;
    }

    public void setAggreField1(String aggreField1) {
        this.aggreField1 = aggreField1;
    }

    public List<String> getAggreFunctions() {
        return aggreFunctions;
    }

    public void setAggreFunctions(List<String> aggreFunctions) {
        this.aggreFunctions = aggreFunctions;
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
        else
            doctemp = this.getTemplate(this.analysisMethodTemplate);

        doctemp = doctemp.replace(HREF, this.getHref());
        doctemp = doctemp.replace(MAXFEATURES, this.getMaxFeatures());
        doctemp = doctemp.replace(OUTPUTFORMAT, this.getOutputFormat());
        doctemp = doctemp.replace(VERSION, this.getVersion());
        doctemp = doctemp.replace(SRSNAME, this.getSrsName());
        doctemp = doctemp.replace(XMLNS, this.getXmlns());
        doctemp = doctemp.replace(TYPENAME, this.getTypeName());
        doctemp = doctemp.replace(AGGREFUNCTIONS, this.buildAggreFunctions());
        doctemp = doctemp.replace(AGGREFIELD1, this.getAggreField1());

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

    private String buildAggreFunctions() {
        String aggre_functions = "";
        // Build WPS functions section
        for (String func : this.getAggreFunctions()) {
            String funcx = functionsTemplate;
            funcx = funcx.replace(FUNCTIONS, func);
            aggre_functions = aggre_functions + funcx;
        }

        return aggre_functions;
    }

}
