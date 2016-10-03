package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class EurostatIndicatorsParser {
    private final static Logger LOG = LogFactory.getLogger(EurostatIndicatorsParser.class);

    private EurostatConfig config;
    SimpleNamespaceContext NAMESPACE_CTX = new SimpleNamespaceContext();



    public EurostatIndicatorsParser(EurostatConfig config)throws java.io.IOException {
        this.config = config;
        NAMESPACE_CTX.addNamespace(XMLConstants.DEFAULT_NS_PREFIX, "http://www.sdmx.org/resources/sdmxml/schemas/v2_1");
        NAMESPACE_CTX.addNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        NAMESPACE_CTX.addNamespace("mes", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message");
        NAMESPACE_CTX.addNamespace("str", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/structure");
        NAMESPACE_CTX.addNamespace("com", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/common");

    }



    public List<EurostatIndicator> parse(List<DatasourceLayer> layers) {


        List<EurostatIndicator> list = new ArrayList<>();

        XMLStreamReader reader = null;
        try (InputStreamReader inputReader = new InputStreamReader(IOHelper.getConnection(config.getUrl()).getInputStream())){



            reader = XMLInputFactory.newInstance().createXMLStreamReader(inputReader);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement ele=builder.getDocumentElement();

            AXIOMXPath xpath_indicator = XmlHelper.buildXPath("/mes:Structure/mes:Structures/str:Dataflows/str:Dataflow", NAMESPACE_CTX);
            AXIOMXPath xpath_names = XmlHelper.buildXPath("com:Name", NAMESPACE_CTX);

            List<OMElement> indicatorsElement = xpath_indicator.selectNodes(ele);

            for(OMElement indicator : indicatorsElement) {
                EurostatIndicator item = new EurostatIndicator();
                String id = indicator.getAttributeValue(QName.valueOf("id"));
                System.out.println(id);
                item.setId(id);
                list.add(item);
                List<OMElement> names = xpath_names.selectNodes(indicator);
                for(OMElement name : names) {
                    String language = XmlHelper.getAttributeValue(name, "lang");
                    System.out.println(language);
                    String indicatorName = name.getText();
                    System.out.println(indicatorName);
                    item.addLocalizedName(language, indicatorName);
                }
            }

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {}
            }
        }
        return list;
    }

    private void setupMetadata(EurostatIndicator indicator, String path) {
        final StatisticalIndicatorSelectors selectors = new StatisticalIndicatorSelectors();
        indicator.setSelectors(selectors);
        // TODO: caching!!
        final JSONObject json = getMetadata(indicator, path);
        if(json == null) {
            // TODO: throw an error maybe? same with unexpected response
            return;
        }

        try {
            JSONArray variables = json.optJSONArray("variables");
            if (variables == null) {
                // TODO: throw an error maybe? same with connection error
                return;
            }
            for (int i = 0; i < variables.length(); i++) {
                JSONObject var = variables.optJSONObject(i);
                final String id = var.optString("code");

                StatisticalIndicatorSelector selector = new StatisticalIndicatorSelector(id);
                selector.setName(var.optString("text"));

                JSONArray values = var.optJSONArray("values");
                JSONArray valueTexts = var.optJSONArray("valueTexts");
                for (int j = 0; j < values.length(); j++) {
                    selector.addAllowedValue(values.optString(j), valueTexts.optString(j));
                }
                selectors.addSelector(selector);
            }
        } catch (Exception ex) {
            LOG.error(ex, "Error parsing indicator metadata from Pxweb datasource:", json);
        }
    }
    private JSONObject getMetadata(EurostatIndicator indicator, String path) {
        String url = config.getUrl() + indicator.getId();
        try {
            String metadata = IOHelper.getURL(url);
            return JSONHelper.createJSONObject(metadata);
        } catch (IOException ex) {
            LOG.error(ex, "Error getting indicator metadata from Pxweb datasource:", url);
        }
        return null;

    }

}
