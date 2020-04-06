package fi.nls.oskari.analysis;

import org.junit.Test;

import static org.junit.Assert.*;

public class AnalysisParserTest {

    @Test
    public void getUserContentAnalysisInputId() {

        assertNull("Non-prefixed layer should not have user content id", AnalysisParser.getUserContentAnalysisInputId("1"));
        assertEquals("Myplaces-prefixed layer should have user content id", "1", AnalysisParser.getUserContentAnalysisInputId("myplaces_1"));
        assertEquals("Userlayer-prefixed layer should have user content id", "5235", AnalysisParser.getUserContentAnalysisInputId("userlayer_5235"));
        assertEquals("Analysis-prefixed layer should have user content id", "347", AnalysisParser.getUserContentAnalysisInputId("analysis_347"));
        assertEquals("Analyzing an analysis layer should have user content id", "68568", AnalysisParser.getUserContentAnalysisInputId("analysis_347_68568"));
    }
}