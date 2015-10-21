package fi.nls.oskari.control.statistics.plugins.sotka.parser;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser (JSON) for getting Sotka region ids and codes
 */
public class SotkaRegionParser {
    // FIXME: Use the property sotka.baseurl
	private static final String URL = "http://www.sotkanet.fi/rest/1.1/regions";
	private static final String ID_FIELD = "id";
	private static final String CODE_FIELD = "code";
	public static final String CATEGORY_FIELD = "category";
	private ObjectMapper mapper;

    private final static Logger log = LogFactory.getLogger(SotkaRegionParser.class);

    private Map<String, String> categoriesByCode;
	private Map<String, Integer> idsByCode;
	private Map<Integer, String> codesById;
    private Map<Integer, Map<String,Object>> regionsObjectsById;

	/**
	 * Inits parser and maps.
	 */
	public SotkaRegionParser() {
		mapper = new ObjectMapper();
		idsByCode = new HashMap<>();
		codesById = new HashMap<>();
        regionsObjectsById = new HashMap<>();
        categoriesByCode = new HashMap<>();
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
    public int getId(String code) {
    	if(idsByCode.containsKey(code))
    		return idsByCode.get(code);
    	return -1;
    }

    /**
     * Gets the category for a certain code
     * @param code
     * @return if exists returns the category, for example "KUNTA", otherwise null
     */
    public String getCategory(String code) {
        if(categoriesByCode.containsKey(code))
            return categoriesByCode.get(code);
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
     */
    public void getData() {
        try {
            String json = HttpRequest.get(URL).body();
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(json);

            Map<String,Object> region = null;

            parser.nextToken();
            while(parser.nextToken() == JsonToken.START_OBJECT) {
                region = mapper.readValue(parser, new TypeReference<Map<String,Object>>() { });
                regionsObjectsById.put((Integer) region.get(ID_FIELD), region);
                if(region.containsKey(CATEGORY_FIELD)) {
                    categoriesByCode.put((String) region.get(CODE_FIELD), (String) region.get(CATEGORY_FIELD));
                    idsByCode.put((String) region.get(CODE_FIELD), (Integer) region.get(ID_FIELD));
                    codesById.put((Integer) region.get(ID_FIELD), (String) region.get(CODE_FIELD));
                }
            }
        } catch (Throwable e) {
            // Converting to RuntimeException, because plugins are expected to throw undeclared exceptions.
            throw new APIException("Something went wrong getting regions from SotkaNET.", e);
        }
    }
}
