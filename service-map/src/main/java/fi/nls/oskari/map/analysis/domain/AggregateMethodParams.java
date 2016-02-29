package fi.nls.oskari.map.analysis.domain;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;

public class AggregateMethodParams extends AnalysisMethodParams {

    private final String analysisMethodTemplate = "analysis-layer-wps-aggregate.xml";
    private final String analysisMethodTemplate2 = "analysis2analysis-layer-wps-aggregate.xml";
    private final String analysisMethodTemplate3 = "analysis2geojson-layer-wps-aggregate.xml";
    private final String bboxAggreFilterTemplate = "<ogc:Filter><ogc:And><ogc:BBOX><ogc:PropertyName>{geom}</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"{srsName}\"><gml:lowerCorner>{x_lower} {y_lower}</gml:lowerCorner><gml:upperCorner>{x_upper} {y_upper}</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:And></ogc:Filter>";
    private final String functionsTemplate = "<wps:Input><ows:Identifier>function</ows:Identifier><wps:Data><wps:LiteralData>{functions}</wps:LiteralData></wps:Data></wps:Input>";

    // xml template paths {}
    private final String AGGREFIELD1 = "{aggreField1}";

    private final String FUNCTIONS = "{functions}";
    private final String AGGREFUNCTIONS = "{aggreFunctions}";

    private String aggreField1 = "";
    private String noDataValue = null;

    private List<String> aggreFunctions = null;

    public String getAggreField1() {
        return aggreField1;
    }

    public void setAggreField1(String aggreField1) {
        this.aggreField1 = aggreField1;
    }

    public String getNoDataValue() {
        return noDataValue;
    }

    public void setNoDataValue(String noDataValue) {
        this.noDataValue = noDataValue;
    }

    public String getBboxAggreFilterTemplate() {
        return bboxAggreFilterTemplate;
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
        else if (this.getWps_reference_type().equals(this.INPUT_GEOJSON))
            doctemp = this.getTemplate(this.analysisMethodTemplate3);
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
        doctemp = doctemp.replace(GEOJSONFEATURES, this.getGeojson());

        // Filter
        String wfsfilter = "";
        if (this.getFilter() != null) {
            wfsfilter = this.getFilter();
        } else {

            String fbbox = this.getBboxFilterTemplate();
            if (this.getNoDataValue() != null) fbbox =this.getBboxAggreFilterTemplate();
            fbbox = fbbox.replace(GEOM, this.getGeom());
            fbbox = fbbox.replace(SRSNAME, this.getSrsName());
            fbbox = fbbox.replace(X_LOWER, this.getX_lower());
            fbbox = fbbox.replace(Y_LOWER, this.getY_lower());
            fbbox = fbbox.replace(X_UPPER, this.getX_upper());
            fbbox = fbbox.replace(Y_UPPER, this.getY_upper());
            wfsfilter = fbbox;
        }

        if (this.getNoDataValue() != null) {
            if(isDoNoDataCount()){
                // No data count filter  - use WPS count aggregate method
                // and calculate the count of no data value items
                wfsfilter = this.appendNoDataCountFilter(wfsfilter, this.getAggreField1());
            } else {
                // Append no_data filter
                wfsfilter = this.appendNoDataFilter(wfsfilter, this.getAggreField1());
            }
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
