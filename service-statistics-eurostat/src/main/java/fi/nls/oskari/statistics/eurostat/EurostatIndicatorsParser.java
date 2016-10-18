package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.XmlHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;

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
    SimpleNamespaceContext NAMESPACE_CTX = new SimpleNamespaceContext();
    private EurostatConfig config;


    public EurostatIndicatorsParser(EurostatConfig config) throws java.io.IOException {
        this.config = config;
        NAMESPACE_CTX.addNamespace(XMLConstants.DEFAULT_NS_PREFIX, "http://www.sdmx.org/resources/sdmxml/schemas/v2_1");
        NAMESPACE_CTX.addNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        NAMESPACE_CTX.addNamespace("mes", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message");
        NAMESPACE_CTX.addNamespace("str", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/structure");
        NAMESPACE_CTX.addNamespace("com", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/common");

    }


    public void setMetadata(EurostatIndicator indicator, String dataStructureID) throws Exception {

        XMLStreamReader reader = null;


        try (InputStreamReader inputReader = new InputStreamReader(IOHelper.getConnection(this.getURL("/SDMX/diss-web/rest/datastructure/ESTAT/") + dataStructureID).getInputStream())) {

            reader = XMLInputFactory.newInstance().createXMLStreamReader(inputReader);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement eleMeta = builder.getDocumentElement();

            AXIOMXPath xpath_Codelist = XmlHelper.buildXPath("/mes:Structure/mes:Structures/str:Codelists/str:Codelist", NAMESPACE_CTX);
            AXIOMXPath xpath_codelistName = XmlHelper.buildXPath("com:Name", this.NAMESPACE_CTX);
            AXIOMXPath xpath_codeID = XmlHelper.buildXPath("str:Code", this.NAMESPACE_CTX);
            AXIOMXPath xpath_codeName = XmlHelper.buildXPath("com:Name", this.NAMESPACE_CTX);

            List<OMElement> indicators = xpath_Codelist.selectNodes(eleMeta);


            final StatisticalIndicatorSelectors selectors = new StatisticalIndicatorSelectors();
            indicator.setSelectors(selectors);
            for (OMElement indicator1 : indicators) {
                String id = indicator1.getAttributeValue(QName.valueOf("id"));
                List<OMElement> IDnames = xpath_codelistName.selectNodes(indicator1);
                StatisticalIndicatorSelector selector = new StatisticalIndicatorSelector(id);

                for (OMElement name : IDnames) {

                    String IDName = name.getText();
                    selector.setName(IDName);
                }
                List<OMElement> codeID = xpath_codeID.selectNodes(indicator1);
                for (OMElement codeID1 : codeID) {
                    String nameID = codeID1.getAttributeValue(QName.valueOf("id"));
                    List<OMElement> codeName = xpath_codeName.selectNodes(codeID1);
                    for (OMElement selectValue : codeName) {
                        String selectValues = selectValue.getText();
                        selector.addAllowedValue(nameID, selectValues);

                    }

                }
                selectors.addSelector(selector);
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public String getURL(final String pUrl) throws IOException {
        return config.getUrl() + pUrl;
    }


    public List<EurostatIndicator> parse(List<DatasourceLayer> layers) {


        List<EurostatIndicator> list = new ArrayList<>();

        XMLStreamReader reader = null;
        try (InputStreamReader inputReader = new InputStreamReader(IOHelper.getConnection(config.getUrl() + "/SDMX/diss-web/rest/dataflow/ESTAT/all/latest").getInputStream())) {


            reader = XMLInputFactory.newInstance().createXMLStreamReader(inputReader);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement ele = builder.getDocumentElement();

            AXIOMXPath xpath_indicator = XmlHelper.buildXPath("/mes:Structure/mes:Structures/str:Dataflows/str:Dataflow", NAMESPACE_CTX);
            AXIOMXPath xpath_names = XmlHelper.buildXPath("com:Name", NAMESPACE_CTX);

            List<OMElement> indicatorsElement = xpath_indicator.selectNodes(ele);
            int count = 0;
            for (OMElement indicator : indicatorsElement) { // str:Dataflow
                count++;
                if (count > 5) {
                    break;
                }
                EurostatIndicator item = new EurostatIndicator();
                String id = indicator.getAttributeValue(QName.valueOf("id"));
                item.setId(id);
                list.add(item);


                List<OMElement> names = xpath_names.selectNodes(indicator);
                for (OMElement name : names) {                                     // com:Name
                    String language = XmlHelper.getAttributeValue(name, "lang");

                    String indicatorName = name.getText();

                    item.addLocalizedName(language, indicatorName);
                }
                OMElement struct = XmlHelper.getChild(indicator, "Structure");
                OMElement ref = XmlHelper.getChild(struct, "Ref");
                String DSDid = ref.getAttributeValue(QName.valueOf("id"));
                setMetadata(item, DSDid);
            }

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            }
        }
        return list;
    }

}
