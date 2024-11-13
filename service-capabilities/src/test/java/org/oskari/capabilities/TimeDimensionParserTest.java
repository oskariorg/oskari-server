package org.oskari.capabilities;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TimeDimensionParserTest {

    @Test
    public void testWMSTIntervalParsingHours() {
        String timeDimension = "2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3H";

        List<String> timeList = TimeDimensionParser.parseTimeDimensionAsStrings(timeDimension);
        assertEquals("timeList should have x times", 81, timeList.size());

        assertEquals("timeList should have expected first time", "2024-11-13T00:00:00Z", timeList.get(0));

        assertEquals("timeList should have expected 10th time", "2024-11-14T03:00:00Z", timeList.get(9));
        assertEquals("timeList should have expected last time", "2024-11-23T00:00:00Z", timeList.get(80));
    }

    @Test
    public void testIntervalDateErrors() {
        assertEquals("Should return empty list for null", 0, TimeDimensionParser.parseTimeDimensionAsStrings(null).size());
        assertEquals("Should return empty list for empty str", 0, TimeDimensionParser.parseTimeDimensionAsStrings("").size());
        assertEquals("Should return empty list for str with less /", 0, TimeDimensionParser.parseTimeDimensionAsStrings("/").size());
        assertEquals("Should return empty list for str with more /", 0, TimeDimensionParser.parseTimeDimensionAsStrings("/////").size());
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2v024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3H");
            fail("Should have raised exception for start date");
        } catch (IllegalArgumentException e) {
            assertEquals("Should throw exception", "Unable to parse start date from: 2v024-11-13T00:00:00Z", e.getMessage());
        }

        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13/2024-11-23T00:00:00Z/PT3H");
            fail("Should have raised exception for start date");
        } catch (IllegalArgumentException e) {
            assertEquals("Should throw exception", "Unable to parse start date from: 2024-11-13", e.getMessage());
        }
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T000:00:00Z/PT3H");
            fail("Should have raised exception for end date");
        } catch (IllegalArgumentException e) {
            assertEquals("Should throw exception", "Unable to parse end date from: 2024-11-23T000:00:00Z", e.getMessage());
        }
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("//PT3H");
            fail("Should have raised exception for start date");
        } catch (IllegalArgumentException e) {
            assertEquals("Should throw exception", "Unable to parse start date from: ", e.getMessage());
        }
    }

    @Test
    public void testIntervalErrors() {
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/PT3xH");
            fail("Should have raised exception for interval");
        } catch (IllegalArgumentException e) {
            assertEquals("Should throw exception", "Unsupported interval format: PT3xH", e.getMessage());
        }
        try {
            TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/P20S");
            fail("Should have raised exception for interval");
        } catch (IllegalArgumentException e) {
            assertEquals("Should throw exception", "Unsupported interval format: P20S", e.getMessage());
        }

        assertEquals("Should return single entry when interval is bigger than start/end difference", 1, TimeDimensionParser.parseTimeDimensionAsStrings("2024-11-13T00:00:00Z/2024-11-23T00:00:00Z/P20Y").size());

    }
}
