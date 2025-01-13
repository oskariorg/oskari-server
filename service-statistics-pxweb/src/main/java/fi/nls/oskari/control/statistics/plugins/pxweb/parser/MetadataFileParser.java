package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.MetadataItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataFileParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private static final Logger LOG = LogFactory.getLogger(MetadataFileParser.class);
    private static final TypeReference METADATA_TYPE_REF = new TypeReference<List<MetadataItem>>(){};

    public static Map<String, MetadataItem> parseMetadataFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        List<MetadataItem> list = null;
        try (InputStream in = MetadataFileParser.class.getResourceAsStream(filePath)) {
            if (in != null) {
                list = (List<MetadataItem>) MAPPER.readValue(in, METADATA_TYPE_REF);
            }
        } catch (MismatchedInputException e) {
            LOG.error(e, "Unexpected format for indicator metadata in file:", filePath);
        } catch (JacksonException e) {
            LOG.error(e, "Error parsing indicator metadata from file:", filePath);
        } catch (IOException e) {
            LOG.warn(e, "Error reading file:", filePath);
        }
        if (list == null) {
            return null;
        }
        Map<String, MetadataItem> result = new HashMap<>();
        list.forEach(item -> {
            if (item != null && item.code != null) {
                result.put(item.code, item);
            }
        });
        return result;
    }
}
