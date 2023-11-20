package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaConfig;
import fi.nls.oskari.control.statistics.util.CacheKeys;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parser (JSON) for getting Sotka region ids and codes
 */
public class SotkaRegionParser {
	private static final String ID_FIELD = "id";
	private static final String CODE_FIELD = "code";
    private static final String CATEGORY_FIELD = "category";
    
	private ObjectMapper mapper;
    private String url;
    private SotkaConfig config;

    private Map<Integer, String> categoriesById;
	private Map<Integer, String> codesById;
    private static final TypeReference<Map<String, Object>> TYPE_REF_REGION = new TypeReference<Map<String, Object>>() { };

    private Logger log = LogFactory.getLogger(SotkaRegionParser.class);

	/**
	 * Inits parser and maps.
	 */
	public SotkaRegionParser(SotkaConfig config) {
		mapper = new ObjectMapper();
		codesById = new HashMap<>();
        categoriesById = new HashMap<>();
        this.config = config;
        url = config.getUrl() + "/1.1/regions";
	}

	/**
     * Gets code from the HashMap
	 * @param id
     * @return if in map returns the code, otherwise null
	 */
    public String getCode(int id) {
        return codesById.get(id);
    }

    /**
     * Checks if the sotkanet internal id has the category that we are interested in
     * @param id sotkanet internal id
     * @param regionType regionType from statslayer config
     * @return
     */
    public boolean isSotkanetInternalIdInRegionSet(Integer id, String regionType) {
        if (id == null || regionType == null) {
            return false;
        }
        String sotkanetCategory = categoriesById.get(id);
        return regionType.equalsIgnoreCase(sotkanetCategory);

    }

    /**
     * Makes HTTP get request and parses the responses JSON into HashMaps.
     * {
         "id": 833,
         "code": "1",
         "category": "ALUEHALLINTOVIRASTO",
         "title": {
         "fi": "Etelä-Suomen AVIn alue",
         "en": "Area for Southern Finland AVI",
         "sv": "Området för Södra Finlands RFV"
         },
         "memberOf": [],
         "uri": "http://www.yso.fi/onto/kunnat/ahv1"
         }
     */
    public void getData() {
        final String cacheKey = CacheKeys.buildCacheKey(config.getId(), "regions", url);
        String json = JedisManager.get(cacheKey);

        if (json == null) {
            try {
                HttpURLConnection con = IOHelper.getConnection(url);
                IOHelper.addIdentifierHeaders(con);
                int code = con.getResponseCode();
                log.info(System.getProperties()
                        .keySet()
                        .stream()
                        .map(key -> "\r\n  " + key + "=" + System.getProperty((String)key) +  "\r\n")
                        .collect(Collectors.toList()));
                if (code != 200) {
                    String error = IOHelper.readString(con.getErrorStream());
                    log.error("Error response from sotkanet:", error);
                }
                json = IOHelper.readString(con);
            } catch (IOException e) {
                throw new APIException("Couldn't read response from SotkaNET: " + url, e);
            }
            JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, json);
        }
        try {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(json);

            parser.nextToken();
            while(parser.nextToken() == JsonToken.START_OBJECT) {
                Map<String,Object> region = mapper.readValue(parser, TYPE_REF_REGION);
                if (!region.containsKey(CATEGORY_FIELD)) {
                    //  oskari statslayer is presented as category in sotkanet like "KUNTA"
                    // each region has 3 things we are interested in:
                    // - category that is used to links regionset layers
                    // - sotkanet internal id
                    // - code that is an id that is recognizable by the regionset layer as a region id (like municipality id)

                    // if don't know the category, we don't know the regionset for this region -> ignore it
                    continue;
                }
                Integer sotkaRegionId = (Integer) region.get(ID_FIELD);
                String regionType = (String) region.get(CATEGORY_FIELD);
                // this is how we can get the regions set for sotkanet internal region id
                // when we know the internal id and we need to get the region set reference
                categoriesById.put(sotkaRegionId, regionType);
                String regionCode = (String) region.get(CODE_FIELD);
                // This is the one we do need -> when requesting data we get the sotkanet internal region id
                // and we need to get the feature id that the layer uses for that region
                codesById.put(sotkaRegionId, regionCode);
            }
        } catch (Exception e) {
            // Converting to RuntimeException, because plugins are expected to throw undeclared exceptions.
            throw new APIException("Something went wrong getting regions from SotkaNET.", e);
        }
    }
}
