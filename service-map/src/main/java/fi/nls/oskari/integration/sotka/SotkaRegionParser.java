package fi.nls.oskari.integration.sotka;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.github.kevinsawicki.http.HttpRequest;

import fi.nls.oskari.log.Logger;

/**
 * Parser (JSON) for getting Sotka region ids and codes
 */
public class SotkaRegionParser {
	private static final String URL = "http://www.sotkanet.fi/rest/1.1/regions";
	private static final String ID_FIELD = "id";
	private static final String CODE_FIELD = "code";
	public static final String CATEGORY_FIELD = "category";
	private static final String REGION_CATEGORY = "KUNTA";
	private ObjectMapper mapper;
	
    private final static Logger log = LogFactory.getLogger(SotkaRegionParser.class);

	private Map<String, Integer> regionsByCode;
	private Map<Integer, String> regionsById;
    private Map<Integer, Map<String,Object>> regionsObjectsById;

	/**
	 * Inits parser and maps and triggers parsing
	 */
	public SotkaRegionParser() {
		mapper = new ObjectMapper();
		regionsByCode = new HashMap<String, Integer>();
		regionsById = new HashMap<Integer, String>();
        regionsObjectsById = new HashMap<Integer, Map<String,Object>>();
		
		try {
			getData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Gets code from the HashMap
	 * @param id
     * @return if in map returns the code, otherwise null
	 */
    public String getCode(int id) {
    	if(regionsById.containsKey(id))
    		return regionsById.get(id);
    	return null;
    }

    /**
     * Gets id from the HashMap
     * @param code
     * @return if in map returns the id, otherwise -1
     */
    public int getId(String code) {
    	if(regionsByCode.containsKey(code))
    		return regionsByCode.get(code);
    	return -1;
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
     * @throws IOException
     */
    private void getData() throws IOException{
    	String json = HttpRequest.get(URL).body();
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createJsonParser(json);
		
		Map<String,Object> region = null;
		
		parser.nextToken();
		while(parser.nextToken() == JsonToken.START_OBJECT) {
			region = mapper.readValue(parser, new TypeReference<Map<String,Object>>() { });
            regionsObjectsById.put((Integer) region.get(ID_FIELD), region);
	        if(region.containsKey(CATEGORY_FIELD)) {
	        	if(REGION_CATEGORY.equals(region.get(CATEGORY_FIELD))) {
					regionsByCode.put((String) region.get(CODE_FIELD), (Integer) region.get(ID_FIELD));
					regionsById.put((Integer) region.get(ID_FIELD), (String) region.get(CODE_FIELD));
	        	}
	        }
		}
    }
}
