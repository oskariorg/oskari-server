package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicator;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
public class SotkaIndicatorsParserTest {
    private static String testResponse = ResourceHelper.readStringResource("SotkaIndicators.json",
            SotkaIndicatorsParserTest.class);
    @Test
    public void testParseIndicators() throws Exception {
        SotkaIndicatorsParser parser = new SotkaIndicatorsParser();
        List<SotkaIndicator> parsedObject = parser.parse(testResponse);
        // TODO: Fix assertion.
        // assertEquals("[]", parsedObject.toString());
    }
}
