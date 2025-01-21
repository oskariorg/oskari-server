package fi.nls.oskari.control.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MetadataFieldHandlerTest {

    @Test
    public void parseTags() {
        List<String> values = MetadataFieldHandler.parseTags(getClass().getResourceAsStream("MetadataFieldParser-input.xml"));
        Assertions.assertEquals(14, values.size(), "Expect 14 values");
        Assertions.assertEquals("YKR-taajama", values.get(6), "Expect specific item");
    }
}