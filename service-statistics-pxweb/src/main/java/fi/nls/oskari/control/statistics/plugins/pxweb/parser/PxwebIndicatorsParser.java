package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.pxweb.PxwebConfig;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.PxFolderItem;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.PxTableItem;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.VariablesItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PxwebIndicatorsParser {
    private final static Logger LOG = LogFactory.getLogger(PxwebIndicatorsParser.class);

    private PxwebConfig config;
    private ObjectMapper mapper = new ObjectMapper();

    public PxwebIndicatorsParser(PxwebConfig config) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.config = config;
    }

    public List<StatisticalIndicator> parse(List<DatasourceLayer> layers) {
        return parse(null, null, layers);
    }

    protected List<StatisticalIndicator> parse(PxFolderItem parent, String path, List<DatasourceLayer> layers) {
        final String url = getUrl(path);
        if(url.endsWith(".px")) {
            return parsePxFile(url, path, layers);
        }

        // if not a px file -> browse through the structure
        List<StatisticalIndicator> indicators = new ArrayList<>();
        List<PxFolderItem> list = readFolderListing(url);
        for(PxFolderItem item : list) {
            if("l".equalsIgnoreCase(item.type)) {
                // recurse to pxweb "folder"
                indicators.addAll(parse(item, getPath(path, item.id), layers));
                continue;
            }
            if(!"t".equalsIgnoreCase(item.type)) {
                // only recognize l and t types
                continue;
            }

            if(config.hasIndicatorKey()) {
                // go to the px-file
                indicators.addAll(parse(item, getPath(path, item.id), layers));
                continue;
            }


            try {
                PxTableItem table = getPxTable(path, item.id);
                StatisticalIndicator ind = new StatisticalIndicator();
                ind.setId(item.id);
                ind.addName(PropertyUtil.getDefaultLanguage(), item.text);
                final StatisticalIndicatorDataModel selectors = getModel(table);
                ind.setDataModel(selectors);
                setupLayers(ind, layers, url);
                indicators.add(ind);
            } catch (IOException e) {
                LOG.error(e, "Error getting indicators from Pxweb datasource:", config.getUrl());
            }
        }

        return indicators;
    }

    protected List<StatisticalIndicator> parsePxFile(String url, String path, List<DatasourceLayer> layers) {
        if(!config.hasIndicatorKey()) {
            LOG.warn("Tried to parse px-file as indicator list but missing indicator key configuration!");
            return Collections.emptyList();
        }
        try {
            PxTableItem table = getPxTable(path);
            List<StatisticalIndicator> indicatorList = readPxTableAsIndicators(table);
            setupLayers(indicatorList, layers, url);
            return indicatorList;
        } catch (IOException e) {
            LOG.error(e, "Error getting indicators from Pxweb datasource:", config.getUrl());
        }
        return Collections.emptyList();
    }

    protected List<PxFolderItem> readFolderListing(String url) {
        try {
            String jsonResponse = loadUrl(url);
            List<PxFolderItem> list =
                    mapper.readValue(jsonResponse, mapper.getTypeFactory().constructCollectionType(List.class, PxFolderItem.class));
            return list;
        } catch (IOException e) {
            LOG.error(e, "Error getting indicators from Pxweb datasource:", config.getUrl());
        }
        return Collections.emptyList();
    }

    private void setupLayers(List<StatisticalIndicator> list, List<DatasourceLayer> layers, String baseUrl) {
        for(StatisticalIndicator ind: list) {
            setupLayers(ind, layers, baseUrl);
        }
    }

    private void setupLayers(StatisticalIndicator ind, List<DatasourceLayer> layers, String baseUrl) {
        for(DatasourceLayer layer : layers) {
            StatisticalIndicatorLayer l = new StatisticalIndicatorLayer(layer.getMaplayerId(), ind.getId());
            l.addParam("baseUrl", baseUrl);
            ind.addLayer(l);
        }
    }

    protected PxTableItem getPxTable(String path) throws IOException {
        String url = getUrl(path);
        if(url.endsWith(".px")) {
            String id = url.substring(url.lastIndexOf('/') + 1);
            return getPxTable(path, id);
        }
        return null;
    }
    protected PxTableItem getPxTable(String path, String tableId) throws IOException {
        String url = getUrl(path);
        if(!url.endsWith(tableId)) {
            if(!url.endsWith("/")) {
                url = url + "/";
            }
            url = url + tableId;
        }
        String json = loadUrl(url);
        PxTableItem table = mapper.readValue(json, PxTableItem.class);
        if(table == null) {
            return null;
        }
        table.setId(tableId);
        table.setPath(path);
        return table;
    }

    protected List<StatisticalIndicator> readPxTableAsIndicators(PxTableItem table) throws IOException {
        List<StatisticalIndicator> list = new ArrayList<>();
        if(table == null) {
            return list;
        }
        // selectors are shared between indicators in pxweb
        final StatisticalIndicatorDataModel selectors = getModel(table);
        // actual indicators list is one of the "variables"
        VariablesItem indicatorList = table.getVariable(config.getIndicatorKey());
        if(indicatorList == null) {
            return list;
        }
        for(IdNamePair item: indicatorList.getLabels()) {
            StatisticalIndicator indicator = new StatisticalIndicator();
            indicator.setId(table.getId() + "::" + item.getKey());
            indicator.addName(PropertyUtil.getDefaultLanguage(), item.getValue());
            indicator.addDescription(PropertyUtil.getDefaultLanguage(), table.getTitle());
            indicator.setDataModel(selectors);
            list.add(indicator);
        }

        return list;
    }

    protected StatisticalIndicatorDataModel getModel(PxTableItem table) {
        // selectors are shared between indicators in pxweb
        final StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        selectors.setTimeVariable(config.getTimeVariableId());
        for (VariablesItem item: table.getSelectors()) {
            if(config.getIgnoredVariables().contains(item.getCode())) {
                continue;
            }
            StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(item.getCode());
            selector.setName(item.getLabel());
            selector.setAllowedValues(item.getLabels());
            selectors.addDimension(selector);
        }
        return selectors;
    }


    private String getUrl(String path) {
        if(path == null) {
            // Example: "http://pxweb.hel.ninja/PXWeb/api/v1/en/hri/hri/"
            return config.getUrl();
        }
        String url = config.getUrl() + "/" + IOHelper.urlEncode(path).replace("+", "%20").replace("%2F", "/");
        return IOHelper.fixPath(url);
    }


    private String getPath(String path, String nextPart) {
        if(path == null) {
            return nextPart;
        }
        String url = path + "/" +  IOHelper.urlEncode(nextPart).replace("+", "%20").replace("%2F", "/");
        return url.replaceAll("//", "/");
    }

    protected String loadUrl(String url) throws IOException {
        return IOHelper.getURL(url);
    }
}
