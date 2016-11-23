package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    public boolean setMetadata(EurostatIndicator indicator, String dataStructureID) throws Exception {
// here should return boolean
        XMLStreamReader reader = null;
        boolean hasGEO = false;

        try (InputStreamReader inputReader =
                     new InputStreamReader(
                             getMetadata(this.getURL("/SDMX/diss-web/rest/datastructure/ESTAT/"), dataStructureID))) {

            reader = XMLInputFactory.newInstance().createXMLStreamReader(inputReader);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement eleMeta = builder.getDocumentElement();
            AXIOMXPath xpath_Codelist = XmlHelper.buildXPath("/mes:Structure/mes:Structures/str:Codelists/str:Codelist", NAMESPACE_CTX);
            AXIOMXPath xpath_codeID = XmlHelper.buildXPath("str:Code", this.NAMESPACE_CTX);
            AXIOMXPath xpath_codeName = XmlHelper.buildXPath("com:Name", this.NAMESPACE_CTX);
            AXIOMXPath dimensionPath = XmlHelper.buildXPath("/mes:Structure/mes:Structures/str:DataStructures/str:DataStructure/str:DataStructureComponents/str:DimensionList/str:Dimension", NAMESPACE_CTX);
            List<OMElement> codelists = xpath_Codelist.selectNodes(eleMeta);// here we have all the codelist indicators
            List<OMElement> dimension = dimensionPath.selectNodes(eleMeta);
            final StatisticalIndicatorSelectors selectors = new StatisticalIndicatorSelectors();
            indicator.setSelectors(selectors);

            for (OMElement dimension1 : dimension) {
                String idDimension = dimension1.getAttributeValue(QName.valueOf("id"));// idDimension is { FREQ, UNIT, GEO}
                if (idDimension.equals("GEO")) {
                    // we need to know that this indicator has a region dimension ("GEO")
                    hasGEO = true;
                    // but we don't pass it as a parameter for the frontend -> skip to next one
                    continue;
                }
                StatisticalIndicatorSelector selector = new StatisticalIndicatorSelector(idDimension);
                selector.setName(idDimension);
                OMElement localRepresentationOme = XmlHelper.getChild(dimension1, "LocalRepresentation");
                OMElement enumerationOme = XmlHelper.getChild(localRepresentationOme, "Enumeration");
                OMElement refOme = XmlHelper.getChild(enumerationOme, "Ref");
                String refId = refOme.getAttributeValue(QName.valueOf("id")); // { refId is CL_FREQ, CL_UNIT}

                for (OMElement codelistElem : codelists) {
                    String id = codelistElem.getAttributeValue(QName.valueOf("id"));// Codelist ID = CL_FREQ

                    if (!id.equals(refId)) {
                        continue;
                    }

                    List<OMElement> codeID = xpath_codeID.selectNodes(codelistElem);
                    for (OMElement codeID1 : codeID) {
                        String nameID = codeID1.getAttributeValue(QName.valueOf("id"));
                        List<OMElement> codeName = xpath_codeName.selectNodes(codeID1);  // Code ID = "D" or "W"
                        for (OMElement selectValue : codeName) {
                            String selectValues = selectValue.getText();                 // selectedValues = codeName "Daily, weekly "
                            selector.addAllowedValue(nameID, selectValues);
                        }
                    }
                    selectors.addSelector(selector);
                }

            }
            populateTimeDimension(selectors);
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
        return hasGEO;
    }

    private void populateTimeDimension(StatisticalIndicatorSelectors selectors) {

        StatisticalIndicatorSelector selector = new StatisticalIndicatorSelector("time");
        selector.setName("time");
        // fixme: hardcoded
        selector.addAllowedValue("2014");
        selector.addAllowedValue("2015");
        selector.addAllowedValue("2013");
        selector.addAllowedValue("2012");
        selectors.addSelector(selector);
    }

    public String getURL(final String pUrl) throws IOException {
        return config.getUrl() + pUrl;
    }

    public List<EurostatIndicator> parse(List<DatasourceLayer> layers) {


        List<EurostatIndicator> list = new ArrayList<>();

        XMLStreamReader reader = null;

        InputStream is = null;
        try {
            // "http://ec.europa.eu/eurostat
            is = IOHelper.getConnection(config.getUrl() + "/SDMX/diss-web/rest/dataflow/ESTAT/all/latest").getInputStream();
            is = IOHelper.debugResponse(is);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Couldn't get indicator list from " + config.getUrl(), e);
        }

        try (InputStreamReader inputReader = new InputStreamReader(is)) {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(inputReader);
            System.out.println(config.getUrl());

            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement ele = builder.getDocumentElement();
            //System.out.println(ele.getText());
            AXIOMXPath xpath_indicator = XmlHelper.buildXPath("/mes:Structure/mes:Structures/str:Dataflows/str:Dataflow", NAMESPACE_CTX);
            AXIOMXPath xpath_names = XmlHelper.buildXPath("com:Name", NAMESPACE_CTX);

            List<OMElement> indicatorsElement = xpath_indicator.selectNodes(ele);
            int count= 0;
            for (OMElement indicator : indicatorsElement) {
                // str:Dataflow
                count++;
                if (count < 550 || count>600) {
                    // at this range there should be indicators with NUTS areas as regions
                    continue;
                }
                if (list.size() > 10) {
                    // have 10 indicators with geo:
                    // break so we don't need to wait all day for the rest to load
                    break;
                }
                EurostatIndicator item = new EurostatIndicator();
                String id = indicator.getAttributeValue(QName.valueOf("id"));
                item.setId(id);// itemId = nama_gdp_c


                List<OMElement> names = xpath_names.selectNodes(indicator);
                for (OMElement name : names) {                                     // com:Name item (language)="en", item(indicatorName)= "sold production..."
                    String language = XmlHelper.getAttributeValue(name, "lang");

                    String indicatorName = name.getText();

                    item.addLocalizedName(language, indicatorName);
                }
                OMElement struct = XmlHelper.getChild(indicator, "Structure");
                OMElement ref = XmlHelper.getChild(struct, "Ref");
                String DSDid = ref.getAttributeValue(QName.valueOf("id"));
                boolean indicatorHasRegion = setMetadata(item, DSDid);
                if (indicatorHasRegion) {
                    list.add(item);
                }
                for (DatasourceLayer layer : layers) {
                    item.addLayer(new EurostatStatisticalIndicatorLayer(layer.getMaplayerId(), item.getId(), config.getUrl()));
                }
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

    private InputStream getMetadata(String pUrl, String dataStructureID) {
        final String url = pUrl + dataStructureID;
        final String cacheKey = "stats:" + config.getId() + ":metadata:" + url;
        try {
            String metadata = JedisManager.get(cacheKey);
            if(metadata == null) {
                metadata = IOHelper.getURL(url);
                JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, metadata);
            }
            return new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOG.error(ex, "Error getting indicator metadata from Pxweb datasource:", url);
        }
        return null;

    }
}
