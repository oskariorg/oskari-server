package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by SMAKINEN on 13.1.2017.
 */
public class DataSourceUpdaterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Test
    public void testReadWorkQueue()
            throws Exception {

        StatisticalIndicator indicator = MAPPER.readValue(getClass().getResourceAsStream("indicator_full.json"), StatisticalIndicator.class);
        Assertions.assertEquals("3056", indicator.getId(), "Indicator id parsed correctly");
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
        Assertions.assertTrue(JSONHelper.isEqual(JSONHelper.createJSONObject(result), JSONHelper.createJSONObject(expectedListItem)), "List serialization should match");

        String full = MAPPER.writeValueAsString(indicator);
        Assertions.assertTrue(JSONHelper.isEqual(JSONHelper.createJSONObject(full), JSONHelper.createJSONObject(fullIndicator)), "Metadata serialization should match");
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
        Assertions.assertTrue(JSONHelper.isEqual(JSONHelper.createJSONObject(expected), JSONHelper.createJSONObject(json)), "Layer serialization should match");

        StatisticalIndicatorLayer layer_fromJson = MAPPER.readValue(json, StatisticalIndicatorLayer.class);
        Assertions.assertEquals(value, layer_fromJson.getParam(key), "Param should have been deserialized correctly");
    }

    @Test
    public void testDimensionHintsWithSerialization()
            throws Exception {

        final String fullIndicator = IOHelper.readString(getClass().getResourceAsStream("indicator_full.json"));
        StatisticalIndicator indicator = MAPPER.readValue(fullIndicator, StatisticalIndicator.class);

        StatisticalIndicatorDataDimension gender = indicator.getDataModel().getDimension("sex");
        Assertions.assertEquals("male", gender.getAllowedValues().get(0).getKey(), "Initial value should be male");
        gender.useDefaultValue("total");
        Assertions.assertEquals("total", gender.getAllowedValues().get(0).getKey(), "Default value should be total");

        StatisticalIndicatorDataDimension year = indicator.getDataModel().getDimension("year");
        Assertions.assertEquals("1996", year.getAllowedValues().get(0).getKey(), "Initial value should be 1996");
        year.sort(true);
        Assertions.assertEquals("2015", year.getAllowedValues().get(0).getKey(), "Sorted value should be 2015");

        String json = MAPPER.writeValueAsString(indicator);
        StatisticalIndicator deserialized = MAPPER.readValue(json, StatisticalIndicator.class);

        Assertions.assertEquals("total", gender.getAllowedValues().get(0).getKey(), "Deserialized value should be total");
        Assertions.assertEquals("2015", year.getAllowedValues().get(0).getKey(), "Deserialized value should be 2015");
    }
}