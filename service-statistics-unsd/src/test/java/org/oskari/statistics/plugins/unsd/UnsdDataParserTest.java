package org.oskari.statistics.plugins.unsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.control.statistics.data.IdNamePair;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;

public class UnsdDataParserTest {

    private static String targetResponse = ResourceHelper.readStringResource("TargetList.json",
            UnsdDataParserTest.class);
    private static JSONObject dataResponseUnlimited = JSONHelper
            .createJSONObject(ResourceHelper.readStringResource("IndicatorData.json", UnsdDataParserTest.class));
    private static JSONObject dataResponse2015AreaLimited = JSONHelper
            .createJSONObject(ResourceHelper.readStringResource("IndicatorData2015.json", UnsdDataParserTest.class));

    private static final int EXPECTED_DIMENSIONS_COUNT = 1;
    private static final String EXPECTED_DIMENSION_ID = "Reporting Type";
    private static final List<IdNamePair> EXPECTED_DIMENSION_ALLOWED_VALUES = Arrays
            .asList(new IdNamePair[] { new IdNamePair("N", null), new IdNamePair("G", null) });

    private static final Map<Integer,Integer> EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES = new HashMap<>(); 
    
    static {
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2000, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2001, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2002, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2003, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2004, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2005, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2006, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2007, 2);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2008, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2009, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2010, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2011, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2012, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2013, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2014, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2015, 1);
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.put(2016, 1);
    }

    @Test
    public void testIndicators() throws JSONException {
        List<StatisticalIndicator> indicators = UnsdIndicatorParser.parseIndicators(targetResponse);
        assertFalse("Did not parse any indicator codes from target response.", indicators.isEmpty());
    }

    @Test
    public void testIndicatorSource() throws JSONException {
        Map<String, String> sources = UnsdIndicatorParser.parseSource(dataResponseUnlimited);
        assertNotNull("Indicator doesn't have source.", sources.get("en"));
    }

    @Test
    public void testIndicatorData() throws JSONException {
        Map<String, IndicatorValue> valuesByRegion = UnsdDataParser.parseIndicatorData(dataResponseUnlimited);
        assertFalse("Did not parse any data from response.", valuesByRegion.isEmpty());
    }

    @Test
    public void testIndicatorDataPages() throws JSONException {
        Boolean lastPage = UnsdDataParser.isLastPage(dataResponse2015AreaLimited);
        assertNotNull("Could not parse page info", lastPage);
        assertTrue("The response wasn't the last page.", lastPage);

        lastPage = UnsdDataParser.isLastPage(dataResponseUnlimited);
        assertNotNull("Could not parse page info", lastPage);
        assertFalse("The response was the last page.", lastPage);
    }

    @Test
    public void testIndicatorDimensions() {
        StatisticalIndicatorDataModel m = UnsdIndicatorParser.parseDimensions(dataResponseUnlimited);
        assertNotNull(m);
        assertNotNull(m.getDimensions());
        assertEquals(EXPECTED_DIMENSIONS_COUNT, m.getDimensions().size());
        StatisticalIndicatorDataDimension d = m.getDimensions().get(0);
        assertEquals(EXPECTED_DIMENSION_ID, d.getId());
        List<IdNamePair> dimensionsAllowedValues = d.getAllowedValues();
        assertNotNull(dimensionsAllowedValues);
        assertEquals(EXPECTED_DIMENSION_ALLOWED_VALUES.size(), dimensionsAllowedValues.size());
        EXPECTED_DIMENSION_ALLOWED_VALUES.forEach(v -> assertTrue(
                String.format("Expected key %s not found from dimensions parsed allowed values", v.getKey()),
                dimensionsAllowedValues.contains(v)));
    }

    @Test
    public void testIndicatorTimePeriodDimensionValues() {
        Map<Integer, Integer> countOfAreaCodesForYear = new HashMap<>();
        UnsdDataParser.parseTimePeriod(countOfAreaCodesForYear, dataResponseUnlimited);
        assertNotNull(countOfAreaCodesForYear);
        assertEquals(EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.size(), countOfAreaCodesForYear.keySet().size());
        EXPECTED_TIMEPERIOD_DIMENSION_ALLOWED_VALUES.entrySet().forEach(e -> {
            assertTrue(String.format("Expected key %s not found", e.getKey()),
                    countOfAreaCodesForYear.containsKey(e.getKey()));
            assertEquals(String.format("For year %s", e.getKey()),
                    e.getValue(), countOfAreaCodesForYear.get(e.getKey()));
        });
    }
}
