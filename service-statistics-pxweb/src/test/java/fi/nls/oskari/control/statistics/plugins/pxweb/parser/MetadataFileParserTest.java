package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.plugins.pxweb.json.MetadataItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class MetadataFileParserTest {

    @Test
    public void parseMetadataMissingFile() {
        Map<String, MetadataItem> metadata = MetadataFileParser.parseMetadataFile("/dummy.testing-file");
        Assertions.assertNull(metadata, "Returns null when file not found");
    }
    @Test
    public void parseMetadataBrokenFile() {
        Map<String, MetadataItem> metadata = MetadataFileParser.parseMetadataFile("/indicator-metadata-broken.json");
        Assertions.assertNull(metadata, "Returns null when file is not parseable");
    }
    @Test
    public void parseMetadataFile() {
        Map<String, MetadataItem> metadata = MetadataFileParser.parseMetadataFile("/indicator-metadata.json");
        Assertions.assertEquals(30, metadata.size(), "Found metadata for 30 indicators");
        Assertions.assertEquals("1987", metadata.get("M404").timerange.start);
        Assertions.assertEquals("2005", metadata.get("M303").timerange.start);
    }
}