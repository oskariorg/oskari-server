package fi.nls.oskari.control.metadata;

import fi.mml.portti.service.search.SearchCriteria;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for field handler
 */
public class MetadataFieldHandlerTest {

    @Test
    public void testHandleParamEmpty() throws Exception {
        MetadataFieldHandler handler = new MetadataFieldHandler();
        handler.setMetadataField(new MetadataField("type", false));
        SearchCriteria sc = new SearchCriteria();

        handler.handleParam("", sc);
        assertEquals("Param value shouldn't be copied to criteria", null, sc.getParam(handler.getPropertyName()));
    }

    @Test
    public void testHandleParamNull() throws Exception {
        MetadataFieldHandler handler = new MetadataFieldHandler();
        handler.setMetadataField(new MetadataField("type", false));
        SearchCriteria sc = new SearchCriteria();

        handler.handleParam(null, sc);
        assertEquals("Param value shouldn't be copied to criteria", null, sc.getParam(handler.getPropertyName()));
    }

    @Test
    public void testHandleParamUsual() throws Exception {
        MetadataFieldHandler handler = new MetadataFieldHandler();
        handler.setMetadataField(new MetadataField("type", false));
        SearchCriteria sc = new SearchCriteria();

        final String value = "test param value";
        handler.handleParam(value, sc);
        assertEquals("Param value should be copied to criteria", value, sc.getParam(handler.getPropertyName()));
    }

    @Test
    public void testHandleParamMulti() throws Exception {
        MetadataFieldHandler handler = new MetadataFieldHandler();
        handler.setMetadataField(new MetadataField("type", true));
        SearchCriteria sc = new SearchCriteria();

        final String value = "test param value";
        handler.handleParam(value, sc);
        final String[] expected =  new String[] {value};
        final String[] actual = (String[])sc.getParam(handler.getPropertyName());
        assertEquals("Param value should be copied to criteria as array (comma separated)",expected.length, actual.length);
        for(int i = 0; i < expected.length; ++i) {
            assertEquals("Param value should match expected",expected[i], actual[i]);
        }
    }

    @Test
    public void testHandleParamMultiCommaSeparated() throws Exception {
        MetadataFieldHandler handler = new MetadataFieldHandler();
        handler.setMetadataField(new MetadataField("type", true));
        SearchCriteria sc = new SearchCriteria();

        final String value = " test param value, value 2, value3";
        handler.handleParam(value, sc);
        final String[] expected =  value.split("\\s*,\\s*");
        final String[] actual = (String[])sc.getParam(handler.getPropertyName());
        assertEquals("Param value should be copied to criteria as array (comma separated)",expected.length, actual.length);
        for(int i = 0; i < expected.length; ++i) {
            assertEquals("Param value should match expected",expected[i], actual[i]);
        }
    }
}
