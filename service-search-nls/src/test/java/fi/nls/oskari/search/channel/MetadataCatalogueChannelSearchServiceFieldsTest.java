package fi.nls.oskari.search.channel;

import fi.nls.oskari.control.metadata.MetadataField;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataCatalogueChannelSearchServiceFieldsTest {

    @Before
    public void setup() {
        PropertyUtil.clearProperties();
    }
    @AfterClass
    public static void teardown() {
        PropertyUtil.clearProperties();
    }
    @Test
    public void testEmptyFields() throws Exception {
        PropertyUtil.loadProperties("/fi/nls/oskari/search/channel/MetadataCatalogueFieldsDifferentService.properties");
        MetadataCatalogueChannelSearchService.resetProperties();
        List<MetadataField> fields = MetadataCatalogueChannelSearchService.getFields();
        assertTrue("Fields should be empty", fields.isEmpty());
        assertEquals("Server url should match properties", PropertyUtil.get("search.channel.METADATA_CATALOGUE_CHANNEL.metadata.catalogue.server"), MetadataCatalogueChannelSearchService.getServerURL());
    }

    @Test
    public void testPaikkatietoikkunaFields() throws Exception {
        PropertyUtil.loadProperties("/fi/nls/oskari/search/channel/MetadataCataloguePaikkatietoikkunaFields.properties");
        MetadataCatalogueChannelSearchService.resetProperties();
        List<MetadataField> fields = MetadataCatalogueChannelSearchService.getFields();
        final String[] propFields = PropertyUtil.getCommaSeparatedList("search.channel.METADATA_CATALOGUE_CHANNEL.fields");
        assertEquals("Number of fields in properties field should match SearchService fields", propFields.length, fields.size());
    }

}