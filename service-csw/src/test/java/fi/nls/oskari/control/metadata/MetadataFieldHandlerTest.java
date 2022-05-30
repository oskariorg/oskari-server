package fi.nls.oskari.control.metadata;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MetadataFieldHandlerTest {

    @Test
    public void parseTags() {
        List<String> values = MetadataFieldHandler.parseTags(getClass().getResourceAsStream("MetadataFieldParser-input.xml"));
        assertEquals("Expect 14 values", 14, values.size());
        assertEquals("Expect specific item", "YKR-taajama", values.get(6));
    }
}