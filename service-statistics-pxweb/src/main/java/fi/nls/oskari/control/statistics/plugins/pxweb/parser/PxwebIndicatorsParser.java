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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        return parse(null, layers);
    }

    protected List<StatisticalIndicator> parse(String path, List<DatasourceLayer> layers) {
        final String url = getUrl(path);

        Collection<String> languages = getLanguages();
        List<StatisticalIndicator> indicatorList;
        if(url.endsWith(".px")) {
            // No id for indicator, assume the service has a separate indicator key config.
            indicatorList = parsePxFileToMultipleIndicators(path, languages);
        } else {
            indicatorList = parseStructuredService(path, languages);
        }
        setupLayers(indicatorList, layers, url);
        return indicatorList;
    }

    protected List<StatisticalIndicator> parseStructuredService(String path, Collection<String> languages) {
        final String url = getUrl(path);
        List<StatisticalIndicator> indicators = new ArrayList<>();
        List<PxFolderItem> list = readFolderListing(url);
        for(PxFolderItem item : list) {
            if("l".equalsIgnoreCase(item.type)) {
                // recurse to pxweb "folder"
                indicators.addAll(parseStructuredService(getPath(path, item.id), languages));
                continue;
            }
            if(!"t".equalsIgnoreCase(item.type)) {
                // only recognize l and t types
                continue;
            }
            if(config.hasIndicatorKey()) {
                // go to the px-file
                indicators.addAll(parsePxFileToMultipleIndicators(getPath(path, item.id), languages));
                continue;
            }
            HashMap<String, StatisticalIndicator> indicatorMap = new HashMap<>();
            languages.forEach(lang -> {
                try {
                    PxTableItem table = getPxTable(path, lang, item.id);
                    String indicatorId = createIndicatorId(table);
                    StatisticalIndicator ind = indicatorMap.get(indicatorId);
                    if (ind == null) {
                        ind = new StatisticalIndicator();
                        ind.setId(indicatorId);
                        indicatorMap.put(indicatorId, ind);
                        indicators.add(ind);
                    }
                    ind.addName(lang, item.text);
                    ind.setDataModel(getModel(table));
                } catch (IOException e) {
                    LOG.error(e, "Error getting indicators from Pxweb datasource:", config.getUrl());
                }
            });
        }
        return indicators;
    }

    protected List<StatisticalIndicator> parsePxFileToMultipleIndicators(String path, Collection<String> languages) {

        if(!config.hasIndicatorKey()) {
            LOG.warn("Tried to parse px-file as indicator list but missing indicator key configuration!");
            return Collections.emptyList();
        }
        List<StatisticalIndicator> indicatorList = new ArrayList<>();
        HashMap<String, StatisticalIndicator> indicatorMap = new HashMap<>();

        languages.forEach(lang -> {
            try {
                PxTableItem table = getPxTable(path, lang);
                List<StatisticalIndicator> indicators = readPxTableAsIndicators(table, lang);
                indicators.stream().forEach(cur -> {
                    StatisticalIndicator indicator = indicatorMap.get(cur.getId());
                    if (indicator == null) {
                        indicatorMap.put(cur.getId(), cur);
                        indicatorList.add(cur);
                        return;
                    }
                    indicator.addName(lang, cur.getName(lang));
                    indicator.addDescription(lang, cur.getDescription(lang));
                });
            } catch (IOException e) {
                LOG.error(e, "Error getting indicators from Pxweb datasource:", config.getUrl());
            }
        });
        return indicatorList;
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
        return getPxTable(path, null);
    }
    protected PxTableItem getPxTable(String path, String lang) throws IOException {
        String url = getUrl(path, lang);
        if(url.endsWith(".px")) {
            String id = url.substring(url.lastIndexOf('/') + 1);
            return getPxTable(path, lang, id);
        }
        return null;
    }
    protected PxTableItem getPxTable(String path, String lang, String tableId) throws IOException {
        String url = getUrl(path, lang);
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

    protected List<StatisticalIndicator> readPxTableAsIndicators(PxTableItem table, String lang) throws IOException {
        List<StatisticalIndicator> list = new ArrayList<>();
        if(table == null) {
            return list;
        }
        if (lang == null) {
            lang = PropertyUtil.getDefaultLanguage();
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
            indicator.setId(createIndicatorId(table, item.getKey()));
            indicator.addName(lang, item.getValue());
            indicator.addDescription(lang, table.getTitle());
            indicator.setDataModel(selectors);
            list.add(indicator);
        }

        return list;
    }

    private String createIndicatorId(PxTableItem table) {
        if (table.getPath() != null) {
            if (table.getPath().endsWith(".px")) {
                return table.getPath();
            }
            return table.getPath() + "/" + table.getId();
        }
        return table.getId();
    }

    private String createIndicatorId(PxTableItem table, String item) {
        return createIndicatorId(table) + PxwebConfig.ID_SEPARATOR + item;
    }

    protected StatisticalIndicatorDataModel getModel(PxTableItem table) {
        // selectors are shared between indicators in pxweb
        final StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        selectors.setTimeVariable(config.getTimeVariableId());
        for (VariablesItem item: table.getSelectors()) {
            if(item.getCode().equals(config.getRegionKey())) {
                selectors.setHasRegionInfo(true);
            }
            if(config.getIgnoredVariables().contains(item.getCode())) {
                continue;
            }
            StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(item.getCode());
            selector.setName(item.getLabel());
            selector.setAllowedValues(item.getLabels());
            selectors.addDimension(selector);
            if (item.isTimeVariable()) {
                // override the time variable config for datasource if we get the information from the API
                selectors.setTimeVariable(selector.getId());
            }
        }
        return selectors;
    }


    private String getUrl(String path) {
        return getUrl(path, null);
    }
    private String getUrl(String path, String language) {
        String lang = language != null ? language : PropertyUtil.getDefaultLanguage();
        String url = config.getUrl().replace("{language}", lang);
        if(path == null) {
            // Example: "http://pxweb.hel.ninja/PXWeb/api/v1/en/hri/hri/"
            return url;
        }
        url += "/" + IOHelper.urlEncode(path).replace("+", "%20").replace("%2F", "/");
        return IOHelper.fixPath(url);
    }

    private String getPath(String path, String nextPart) {
        if(path == null) {
            return nextPart;
        }
        return path + "/" + nextPart;
    }

    protected String loadUrl(String url) throws IOException {
        // make sure there's no spaces
        return IOHelper.getURL(url.replaceAll(" ", "%20"));
    }

    private Collection<String> getLanguages() {
        HashSet<String> languages = new HashSet<>();
        languages.add(PropertyUtil.getDefaultLanguage());
        if (serviceSupportsMultipleLanguages()) {
            languages.addAll(Arrays.asList(PropertyUtil.getSupportedLanguages()));
        }
        return languages;
    }

    private boolean serviceSupportsMultipleLanguages() {
        return config.getUrl().contains("{language}");
    }
}
