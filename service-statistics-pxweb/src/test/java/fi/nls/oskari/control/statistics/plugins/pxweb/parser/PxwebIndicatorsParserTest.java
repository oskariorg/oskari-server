package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.pxweb.PxwebConfig;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests that pxweb config can point directly to a px-file.
 *
 * Note! If the config is modified AFTER users have saved embedded maps/views the saved state won't match the
 *  indicator id anymore -> migration to DB is required
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {IOHelper.class})
public class PxwebIndicatorsParserTest {

    @Before
    public void setup() throws Exception {
        Map<String, String> responses = new HashMap<>();
        responses.put("https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/", "px-folder-response.json");
        responses.put("https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/kuntien_avainluvut_2017_aikasarja.px", "px-table-aikasarja-response.json");
        responses.put("https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/kuntien_avainluvut_2017_viimeisin.px", "px-table-viimeisin-response.json");
        PowerMockito.mockStatic(IOHelper.class);
        for (String url : responses.keySet()) {
            // use Mockito to set up your expectation
            Mockito.when(IOHelper.getURL(url)).thenReturn(ResourceHelper.readStringResource(responses.get(url), PxwebIndicatorsParserTest.class));
            Mockito.when(IOHelper.fixPath(url)).then(Mockito.CALLS_REAL_METHODS);
        }
    }

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

    @Test
    public void testParseConfigWithoutIndicatorKey() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstruct.json");
        List<StatisticalIndicator> indicators = parser.parse(getLayers());

        int expectedCount = 2;
        String expectedName = "Kuntien avainluvut 1987-2016";
        String expectedId = "kuntien_avainluvut_2017_aikasarja.px";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        assertEquals("Should find two dimensions", 2, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'vuosi'", "Vuosi", indicators.get(0).getDataModel().getDimension("vuosi").getName());
        // config.indicatorKey == Tiedot is parsed as dimension as well
        assertEquals("Should find dimension 'Tiedot'", "Tiedot", indicators.get(0).getDataModel().getDimension("Tiedot").getName());
    }
    @Test
    public void testParseWithFolderStructureConfig() throws Exception {
        PxwebIndicatorsParser parser = getParser("config2folderstructWithIndicatorKey.json");
        List<StatisticalIndicator> indicators = parser.parse(getLayers());

        int expectedCount = 2;
        String expectedName = "Kuntien avainluvut 1987-2016";
        String expectedId = "kuntien_avainluvut_2017_aikasarja.px";
        assertEquals("Should find " + expectedCount + " indicators", expectedCount, indicators.size());
        assertEquals("Should find " + expectedName + " as first indicator name", expectedName, indicators.get(0).getName(PropertyUtil.getDefaultLanguage()));
        assertEquals("Should find " + expectedId + " as first indicator id", expectedId, indicators.get(0).getId());
        // config.indicatorKey is parsed as indicators so only vuosi is left as dimension
        assertEquals("Should find one dimension", 1, indicators.get(0).getDataModel().getDimensions().size());
        assertEquals("Should find dimension 'vuosi'", "Vuosi", indicators.get(0).getDataModel().getDimension("vuosi").getName());
    }

    private List<DatasourceLayer> getLayers() {
        DatasourceLayer layer = new DatasourceLayer();
        return Collections.singletonList(layer);
    }

    private PxwebIndicatorsParser getParser(String resource) {
        JSONObject json = ResourceHelper.readJSONResource(resource, this);
        PxwebConfig config = new PxwebConfig(json, 1);
        return new PxwebIndicatorsParser(config);
    }

}