package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaConfig;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser (JSON) for getting Sotka region ids and codes
 */
public class SotkaRegionParser {
	private static final String ID_FIELD = "id";
	private static final String CODE_FIELD = "code";
	public static final String CATEGORY_FIELD = "category";
    
	private ObjectMapper mapper;
    private String url;
    private SotkaConfig config;

    private final static Logger LOG = LogFactory.getLogger(SotkaRegionParser.class);

    private Map<Integer, String> categoriesById;
	private Map<String, Map<String, Integer>> idsByCategoryAndCode;
	private Map<Integer, String> codesById;
    private Map<Integer, Map<String,Object>> regionsObjectsById;

	/**
	 * Inits parser and maps.
	 */
	public SotkaRegionParser(SotkaConfig config) {
		mapper = new ObjectMapper();
		idsByCategoryAndCode = new HashMap<>();
		codesById = new HashMap<>();
        regionsObjectsById = new HashMap<>();
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
    	if(codesById.containsKey(id))
    		return codesById.get(id);
    	return null;
    }

    /**
     * Gets id from the HashMap
     * @param code
     * @return if in map returns the id, otherwise -1
     */
    public int getId(String regionCategory, String code) {
        if(regionCategory == null) {
            for(String region : idsByCategoryAndCode.keySet()) {
                final int id = getId(region, code);
                if(id != -1) {
                    return id;
                }
            }
        }
        if (idsByCategoryAndCode.containsKey(regionCategory)) {
            Map<String, Integer> idsByCode = idsByCategoryAndCode.get(regionCategory);
            if(idsByCode.containsKey(code)) {
                return idsByCode.get(code);
            }
        } else {
            LOG.error("Unknown region category: " + regionCategory + ", known ones: " +
                idsByCategoryAndCode.keySet().toString());
        }
        return -1;
    }

    /**
     * Gets the category for a certain id
     * @param id
     * @return if exists returns the category, for example "KUNTA", otherwise null
     */
    public String getCategoryById(Integer id) {
        if(categoriesById.containsKey(id))
            return categoriesById.get(id);
        return null;
    }

    /**
     * Gets id from the HashMap
     * @param id
     * @return if in map returns the region data else null
     */
    public Map<String,Object> getRegionById(int id) {
        return regionsObjectsById.get(id);
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
        final String cacheKey = "stats:" + config.getId() + ":regions:" + url;
        String json = JedisManager.get(cacheKey);

        if (json == null) {
            json = HttpRequest.get(url).body();
            JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, json);
        }
        try {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(json);

            Map<String,Object> region = null;

            parser.nextToken();
            while(parser.nextToken() == JsonToken.START_OBJECT) {
                region = mapper.readValue(parser, new TypeReference<Map<String,Object>>() { });
                regionsObjectsById.put((Integer) region.get(ID_FIELD), region);
                if(region.containsKey(CATEGORY_FIELD)) {
                    categoriesById.put((Integer) region.get(ID_FIELD), (String) region.get(CATEGORY_FIELD));
                    String category = (String) region.get(CATEGORY_FIELD);
                    if (!idsByCategoryAndCode.containsKey(category)) {
                        idsByCategoryAndCode.put(category, new HashMap<String, Integer>());
                    }
                    idsByCategoryAndCode.get(category).put((String) region.get(CODE_FIELD), (Integer) region.get(ID_FIELD));
                    codesById.put((Integer) region.get(ID_FIELD), (String) region.get(CODE_FIELD));
                }
            }
        } catch (Exception e) {
            // Converting to RuntimeException, because plugins are expected to throw undeclared exceptions.
            throw new APIException("Something went wrong getting regions from SotkaNET.", e);
        }
    }
}
