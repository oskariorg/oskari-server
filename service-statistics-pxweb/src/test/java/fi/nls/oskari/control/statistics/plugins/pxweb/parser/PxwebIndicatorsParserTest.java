package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.statistics.data.IdNamePair;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.pxweb.PxwebConfig;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.MetadataItem;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.PxTableItem;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Tests that pxweb config can point directly to a px-file.
 * <p>
 * Note! If the config is modified AFTER users have saved embedded maps/views the saved state won't match the
 * indicator id anymore -> migration to DB is required
 */
public class PxwebIndicatorsParserTest {

    private static final Map<String, String> responses = new HashMap<>();

    static {
        // tk
        responses.put("https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/", "px-folder-response.json");
        responses.put("https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/kuntien_avainluvut_2017_aikasarja.px", "px-table-aikasarja-response.json");
        responses.put("https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/kuntien_avainluvut_2017_viimeisin.px", "px-table-viimeisin-response.json");
        //hki
        //responses.put("http://api.aluesarjat.fi/PXWeb/api/v1/fi/Helsingin%20seudun%20tilastot/P%C3%A4%C3%A4kaupunkiseutu%20alueittain", "");
    }

    /**
     * Tests parsing when the configured url points directly to a px-file
     */
    @Test
    public void testParseWithPXfileConfig() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2pxfile.json");
        List<StatisticalIndicator> indicators = parser.parse(getLayers());
        int expectedCount = 31;
        String expectedName = "Taajama-aste, %";
        String expectedId = "kuntien_avainluvut_2017_aikasarja.px::M408";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        // config.indicatorKey is parsed as indicators so only vuosi is left as dimension
        assertEquals("Should find one dimension", 1, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'vuosi'", "Vuosi", indicators.get(0).getDataModel().getDimension("vuosi").getName());
    }

    /**
     * Tests parsing when the configured url points to a folder structure (NOT to a px-file) AND indicator key is NOT configured.
     * PX-file refs are treated as indicators.
     */
    @Test
    @Ignore("Assumes network connectivity")
    public void testParseConfigWithoutIndicatorKey() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstruct_tk.json");
        PropertyUtil.addProperty("oskari.locales", "en, fi, sv", true);
        List<StatisticalIndicator> indicators = parser.parse(getLayers());

        int expectedCount = 2;
        String expectedName = "Municipal key figures 1987-2018";
        String expectedId = "kuntien_avainluvut_2019_aikasarja.px";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        assertEquals("Should find two dimensions", 2, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'Vuosi'", "Year", indicators.get(0).getDataModel().getDimension("Vuosi").getName());
        // config.indicatorKey == Tiedot is parsed as dimension as well
        assertEquals("Should find dimension 'Information'", "Information", indicators.get(0).getDataModel().getDimension("Tiedot").getName());
        PropertyUtil.clearProperties();
    }

    /**
     * Tests parsing when the configured url points to a folder structure (NOT to a px-file) AND indicator key is NOT configured.
     * PX-file refs are treated as indicators.
     */
    @Test
    @Ignore("Assumes network connectivity")
    public void testParseConfigWithIndicatorKey() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstruct_tk_indicator_key.json");
        PropertyUtil.addProperty("oskari.locales", "en, fi, sv", true);
        List<StatisticalIndicator> indicators = parser.parse(getLayers());

        int expectedCount = 32;
        String expectedName = "Degree of urbanisation, %";
        String expectedId = "kuntien_avainluvut_2019_aikasarja.px::M408";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        assertEquals("Should find two dimensions", 1, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'vuosi'", "Vuosi", indicators.get(0).getDataModel().getDimension("vuosi").getName());
        PropertyUtil.clearProperties();
    }

    /**
     * Tests parsing when the configured url points to a DEEP folder structure (NOT to a px-file) AND indicator key is NOT configured.
     * PX-file refs are treated as indicators.
     * <p>
     * TODO: Should indicator id be prefixed with the path it was found in (relative to root url configuration)?
     */
    @Test
    @Ignore("Assumes network connectivity")
    public void testParseConfigWithoutIndicatorKeyHKI() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstruct_hki.json");
        List<StatisticalIndicator> indicators = parser.parse(getLayers());
        int expectedCount = 16;
        String expectedName = "Helsingin asuntokuntien tulot asuntokunnan elinvaiheen ja alueen mukaan 2014-";
        String expectedId = "Asuntokuntien tulot/A01AS_HKI_Asuntokuntien_tulot_elinvaihe.px";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        assertEquals("Should find three dimensions", 3, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'vuosi'", "Vuosi", indicators.get(0).getDataModel().getDimension("vuosi").getName());
        // config.indicatorKey == Tiedot is parsed as dimension as well
        assertEquals("Should find dimension 'Elinvaihe'", "Elinvaihe", indicators.get(0).getDataModel().getDimension("Elinvaihe").getName());
        assertEquals("Should find dimension 'Tieto'", "Tieto", indicators.get(0).getDataModel().getDimension("Tieto").getName());
    }

    /**
     * Tests parsing when the configured url points to a folder structure (NOT to a px-file) AND indicator key is configured.
     * PX-file refs are processed like when configured url would point to a px-file AND all the indicators from all
     * the px-files in the whole folder structure is gathered as a single indicator list.
     */
    @Test
    public void testParseWithFolderStructureConfig() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstructWithIndicatorKey.json");
        List<StatisticalIndicator> indicators = parser.parse(getLayers());

        int expectedCount = 62;
        String expectedName = "Taajama-aste, %";
        String expectedId = "kuntien_avainluvut_2017_aikasarja.px::M408";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        // config.indicatorKey is parsed as indicators so only vuosi is left as dimension
        assertEquals("Should find one dimension", 1, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'vuosi'", "Vuosi", indicators.get(0).getDataModel().getDimension("vuosi").getName());
    }

    @Test
    public void testHKIModel() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstruct_hki.json");
        String json = ResourceHelper.readStringResource("px-table-hki.json", PxwebIndicatorsParserTest.class);
        PxTableItem table = new ObjectMapper().readValue(json, PxTableItem.class);
        StatisticalIndicatorDataModel model = parser.getModel(table);

        // Alue is ignored as it's the region key
        assertEquals("Should have 6 params", 6, model.getDimensions().size());
    }


    @Test
    @Ignore("Assumes network connectivity")
    public void testHKIModelLive() throws Exception {
        JSONObject json = ResourceHelper.readJSONResource("config2folderstruct_hki_live.json", this);
        PxwebConfig config = new PxwebConfig(json, 1);

        PxwebIndicatorsParser parser = new PxwebIndicatorsParser(config);
        List<DatasourceLayer> layers = new ArrayList<>();
        List<StatisticalIndicator> indicators = parser.parse(layers);
        // This is just for debugging the live service and how it's parsed
        System.out.println(indicators.size());
    }


    @Test
    @Ignore("Assumes network connectivity")
    public void testLUKEModelLive() throws Exception {
        // PropertyUtil.addProperty("oskari.trustAllCerts", "true", true);
        JSONObject json = ResourceHelper.readJSONResource("config2folderstruct_luke.json", this);
        PxwebConfig config = new PxwebConfig(json, 1);

        PxwebIndicatorsParser parser = new PxwebIndicatorsParser(config);
        List<DatasourceLayer> layers = new ArrayList<>();
        List<StatisticalIndicator> indicators = parser.parse(layers);
        // This is just for debugging the live service and how it's parsed
        System.out.println(indicators.size());
    }


    private List<DatasourceLayer> getLayers() {
        DatasourceLayer layer = new DatasourceLayer();
        return Collections.singletonList(layer);
    }

    private PxwebIndicatorsParser getParser(String resource) {
        JSONObject json = ResourceHelper.readJSONResource(resource, this);
        PxwebConfig config = new PxwebConfig(json, 1);

        //PxwebIndicatorsParser parser = mock(PxwebIndicatorsParser.class, Mockito.CALLS_REAL_METHODS);
        PxwebIndicatorsParser parser = spy(new PxwebIndicatorsParser(config));
        try {
            for (String url : responses.keySet()) {
                // use Mockito to set up your expectation
                doReturn(ResourceHelper.readStringResource(responses.get(url), PxwebIndicatorsParserTest.class)).when(parser).loadUrl(url);
                //Mockito.when(parser.loadUrl(url)).thenReturn();
            }
            return parser;
        } catch (IOException ignored) {
        }
        return null;
    }

    @Test
    public void testFilteringTimeValuesWithIndicatorMetadata() {
        PxwebIndicatorsParser parser = getParser("config2folderstructWithIndicatorKey.json");
        StatisticalIndicatorDataDimension timeVar = new StatisticalIndicatorDataDimension("time");
        assertNull("No time variable + null meta == null as list", parser.filterAvailableTimes(null, null));
        assertEquals("No allowed values + null meta should result in empty array", 0, parser.filterAvailableTimes(timeVar, null).size());

        MetadataItem meta = new MetadataItem();
        assertNull("No time variable == null as list", parser.filterAvailableTimes(null, meta));
        assertEquals("No allowed values + empty meta should result in empty array", 0, parser.filterAvailableTimes(timeVar, new MetadataItem()).size());

        // NOTE! Timerange filtering assumes times are in order
        for (int  year = 1990; year < 2022; ++year) {
            timeVar.addAllowedValue(Integer.toString(year));
        }
        assertEquals("Check that we have current amount of values in allowed values",32, timeVar.getAllowedValues().size());
        assertEquals("Null meta shouldn't filter anything out", timeVar.getAllowedValues().size(), parser.filterAvailableTimes(timeVar, null).size());
        assertEquals("Meta without time range shouldn't filter anything out", timeVar.getAllowedValues().size(), parser.filterAvailableTimes(timeVar, meta).size());

        meta.timerange = new MetadataItem.Timerange();
        assertEquals("Meta without start/end time shouldn't filter anything out", timeVar.getAllowedValues().size(), parser.filterAvailableTimes(timeVar, meta).size());

        // with start time only
        meta.timerange.start = "2000";
        List<IdNamePair> values = parser.filterAvailableTimes(timeVar, meta);
        assertEquals("Meta with start time should filter out years", 22, values.size());
        assertEquals("First allowed value with meta start time should be 2000", "2000", values.get(0).getKey());
        assertEquals("Last allowed value with meta end time should be 2021", "2021", values.get(values.size() - 1).getKey());
        assertFalse("All years below 2000 should have been removed",
                values.stream().mapToInt(item -> Integer.parseInt(item.getKey())).anyMatch(year -> year < 2000));

        // with start + end time
        meta.timerange.end = "2010";
        values = parser.filterAvailableTimes(timeVar, meta);
        assertEquals("Meta with start + end time should filter out years", 11, values.size());
        assertEquals("First allowed value with meta start time should be 2000", "2000", values.get(0).getKey());
        assertEquals("Last allowed value with meta end time should be 2010", "2010", values.get(values.size() - 1).getKey());
        assertFalse("All years below 2000 should have been removed",
                values.stream().mapToInt(item -> Integer.parseInt(item.getKey())).anyMatch(year -> year < 2000));
        assertFalse("All years after 2010 should have been removed",
                values.stream().mapToInt(item -> Integer.parseInt(item.getKey())).anyMatch(year -> year > 2010));

        // with end time only
        meta.timerange.start = null;
        values = parser.filterAvailableTimes(timeVar, meta);
        assertEquals("Meta with end time should filter out years", 21, values.size());
        assertEquals("First allowed value with meta start time should be 1990", "1990", values.get(0).getKey());
        assertEquals("Last allowed value with meta end time should be 2010", "2010", values.get(values.size() - 1).getKey());
        assertFalse("All years after 2010 should have been removed",
                values.stream().mapToInt(item -> Integer.parseInt(item.getKey())).anyMatch(year -> year > 2010));

    }
}