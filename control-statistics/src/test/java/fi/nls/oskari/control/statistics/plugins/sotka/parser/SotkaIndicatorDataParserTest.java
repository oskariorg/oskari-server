package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
public class SotkaIndicatorDataParserTest {
    private static String testResponse = ResourceHelper.readStringResource("SotkaIndicatorData.csv");

}
