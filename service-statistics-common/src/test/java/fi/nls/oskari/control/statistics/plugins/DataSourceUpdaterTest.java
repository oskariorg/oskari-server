package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 13.1.2017.
 */
public class DataSourceUpdaterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Test
    public void testReadWorkQueue()
            throws Exception {

        StatisticalIndicator indicator = MAPPER.readValue(getClass().getResourceAsStream("indicator_full.json"), StatisticalIndicator.class);
        assertEquals("Indicator id parsed correctly", "3056", indicator.getId());
    }
    @Test
    public void writeToList()
            throws Exception {

        final String fullIndicator = IOHelper.readString(getClass().getResourceAsStream("indicator_full.json"));
        StatisticalIndicator indicator = MAPPER.readValue(fullIndicator, StatisticalIndicator.class);

        final ObjectMapper listMapper = new ObjectMapper();
        listMapper.addMixIn(StatisticalIndicator.class, JacksonIndicatorListMixin.class);

        final String expectedListItem = IOHelper.readString(getClass().getResourceAsStream("indicator_listitem.json"));
        String result = listMapper.writeValueAsString(indicator);
        assertTrue("List serialization should match", JSONHelper.isEqual(JSONHelper.createJSONObject(result), JSONHelper.createJSONObject(expectedListItem)));

        String full = MAPPER.writeValueAsString(indicator);
        assertTrue("Metadata serialization should match", JSONHelper.isEqual(JSONHelper.createJSONObject(full), JSONHelper.createJSONObject(fullIndicator)));
    }

    @Test
    public void layerJSONTest()
            throws Exception {
        StatisticalIndicatorLayer layer = new StatisticalIndicatorLayer(1, "indicatorId");
        final String key = "testing";
        final String value = "param value";
        layer.addParam(key, value);
        String json = MAPPER.writeValueAsString(layer);
        final String expected = "{\"oskariLayerId\":1,\"indicatorId\":\"indicatorId\",\"params\":{\"testing\":\"param value\"},\"indicatorValueType\":\"FLOAT\"}";
        assertTrue("Layer serialization should match", JSONHelper.isEqual(JSONHelper.createJSONObject(expected), JSONHelper.createJSONObject(json)));

        StatisticalIndicatorLayer layer_fromJson = MAPPER.readValue(json, StatisticalIndicatorLayer.class);
        assertEquals("Param should have been deserialized correctly", value, layer_fromJson.getParam(key));
    }

    @Test
    public void testDimensionHintsWithSerialization()
            throws Exception {

        final String fullIndicator = IOHelper.readString(getClass().getResourceAsStream("indicator_full.json"));
        StatisticalIndicator indicator = MAPPER.readValue(fullIndicator, StatisticalIndicator.class);

        StatisticalIndicatorDataDimension gender = indicator.getDataModel().getDimension("sex");
        assertEquals("Initial value should be male", "male", gender.getAllowedValues().get(0).getKey());
        gender.useDefaultValue("total");
        assertEquals("Default value should be total", "total", gender.getAllowedValues().get(0).getKey());

        StatisticalIndicatorDataDimension year = indicator.getDataModel().getDimension("year");
        assertEquals("Initial value should be 1996", "1996", year.getAllowedValues().get(0).getKey());
        year.sort(true);
        assertEquals("Sorted value should be 2015", "2015", year.getAllowedValues().get(0).getKey());

        String json = MAPPER.writeValueAsString(indicator);
        StatisticalIndicator deserialized = MAPPER.readValue(json, StatisticalIndicator.class);

        assertEquals("Deserialized value should be total", "total", gender.getAllowedValues().get(0).getKey());
        assertEquals("Deserialized value should be 2015", "2015", year.getAllowedValues().get(0).getKey());
    }
}