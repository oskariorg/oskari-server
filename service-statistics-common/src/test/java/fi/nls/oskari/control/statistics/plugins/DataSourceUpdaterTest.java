package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
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

        StatisticalIndicator indicator = MAPPER.readValue(getClass().getResourceAsStream("indicator.json"), StatisticalIndicator.class);
        assertEquals("Indicator id parsed correctly", "3056", indicator.getId());
    }
}