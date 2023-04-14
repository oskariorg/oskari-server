package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.plugins.pxweb.json.MetadataItem;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class MetadataFileParserTest {

    @Test
    public void parseMetadataMissingFile() {
        Map<String, MetadataItem> metadata = MetadataFileParser.parseMetadataFile("/dummy.testing-file");
        assertNull("Returns null when file not found", metadata);
    }
    @Test
    public void parseMetadataBrokenFile() {
        Map<String, MetadataItem> metadata = MetadataFileParser.parseMetadataFile("/indicator-metadata-broken.json");
        assertNull("Returns null when file is not parseable", metadata);
    }
    @Test
    public void parseMetadataFile() {
        Map<String, MetadataItem> metadata = MetadataFileParser.parseMetadataFile("/indicator-metadata.json");
        assertEquals("Found metadata for 30 indicators", 30, metadata.size());
        assertEquals("1987", metadata.get("M404").timerange.start);
        assertEquals("2005", metadata.get("M303").timerange.start);
    }
}