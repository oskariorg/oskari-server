package org.oskari.capabilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TimeDimensionParserTest {

    @Test
    public void testWMSTIntervalParsingHours() {
        String timeDimension = "2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3H";

        List<String> timeList = TimeDimensionParser.parseTimeDimensionAsStrings(timeDimension);
        Assertions.assertEquals(81, timeList.size(), "timeList should have x times");
        Assertions.assertEquals("2024-11-13T00:00:00Z", timeList.get(0), "timeList should have expected first time");
        Assertions.assertEquals("2024-11-14T03:00:00Z", timeList.get(9), "timeList should have expected 10th time");
        Assertions.assertEquals("2024-11-23T00:00:00Z", timeList.get(80), "timeList should have expected last time");
    }
    @Test
    public void testWMSTIntervalParsingMinutes() {
        String timeDimension = "2024-11-20T00:00:00Z/2024-11-23T00:00:00Z/PT10M";

        List<String> timeList = TimeDimensionParser.parseTimeDimensionAsStrings(timeDimension);
        Assertions.assertEquals(433, timeList.size(), "timeList should have x times");
        Assertions.assertEquals("2024-11-20T00:00:00Z", timeList.get(0), "timeList should have expected first time");
        Assertions.assertEquals("2024-11-20T01:30:00Z", timeList.get(9), "timeList should have expected 10th time");
        Assertions.assertEquals("2024-11-23T00:00:00Z", timeList.get(432), "timeList should have expected last time");

    }

    @Test
    public void testIntervalDateErrors() {
        Assertions.assertEquals(0, TimeDimensionParser.parseTimeDimensionAsStrings(null).size(), "Should return empty list for null");
        Assertions.assertEquals(0, TimeDimensionParser.parseTimeDimensionAsStrings("").size(), "Should return empty list for empty str");
        Assertions.assertEquals(0, TimeDimensionParser.parseTimeDimensionAsStrings("/").size(), "Should return empty list for str with less /");
        Assertions.assertEquals(0, TimeDimensionParser.parseTimeDimensionAsStrings("/////").size(), "Should return empty list for str with more /");
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2v024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3H");
            Assertions.fail("Should have raised exception for start date");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Unable to parse start date from: 2v024-11-13T00:00:00Z", e.getMessage(), "Should throw exception");
        }

        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13/2024-11-23T00:00:00Z/PT3H");
            Assertions.fail("Should have raised exception for start date");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Unable to parse start date from: 2024-11-13", e.getMessage(), "Should throw exception");
        }
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T000:00:00Z/PT3H");
            Assertions.fail("Should have raised exception for end date");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Unable to parse end date from: 2024-11-23T000:00:00Z", e.getMessage(), "Should throw exception");
        }
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("//PT3H");
            Assertions.fail("Should have raised exception for start date");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Unable to parse start date from: ", e.getMessage(), "Should throw exception");
        }
    }

    @Test
    public void testIntervalErrors() {
        TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT10M");

        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3xH");
            Assertions.fail("Should have raised exception for interval");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Unsupported interval format: PT3xH", e.getMessage(), "Should throw exception");
        }
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/P20S");
            Assertions.fail("Should have raised exception for interval");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Unsupported interval format: P20S", e.getMessage(), "Should throw exception");
        }

        Assertions.assertEquals(1, TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/P20Y").size(), "Should return single entry when interval is bigger than start/end difference");

    }
}
