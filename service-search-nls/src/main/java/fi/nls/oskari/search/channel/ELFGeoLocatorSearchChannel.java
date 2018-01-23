package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.ELFGeoLocatorParser;
import fi.nls.oskari.search.util.ELFGeoLocatorQueryHelper;
import fi.nls.oskari.search.util.HitCombiner;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * Search channel for ELF Geolocator requests
 */
@Oskari(ELFGeoLocatorSearchChannel.ID)
public class ELFGeoLocatorSearchChannel extends SearchChannel implements SearchAutocomplete {

    public static final String ID = "ELFGEOLOCATOR_CHANNEL";
    public static final String PROPERTY_SERVICE_URL = "search.channel.ELFGEOLOCATOR_CHANNEL.service.url";
    public static final String KEY_LANG_HOLDER = "_LANG_";
    public static final String KEY_LATITUDE_HOLDER = "_LATITUDE_";
    public static final String KEY_LONGITUDE_HOLDER = "_LONGITUDE_";
    public static final String KEY_PLACE_HOLDER = "_PLACE_HOLDER_";
    public static final String KEY_AU_HOLDER = "_AU_HOLDER_";
    public static final String RESPONSE_CLEAN = "<?xml version='1.0' encoding='UTF-8'?>";
    public static final String DEFAULT_REVERSEGEOCODE_TEMPLATE = "?SERVICE=WFS&REQUEST=ReverseGeocode&LAT=_LATITUDE_&LON=_LONGITUDE_&LANGUAGE=_LANG_";
    public static final String DEFAULT_GETFEATUREAU_TEMPLATE = "?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetFeatureInAu&NAME=_PLACE_HOLDER_&AU=_AU_HOLDER_&LANGUAGE=_LANG_";
    public static final String DEFAULT_FUZZY_TEMPLATE = "?SERVICE=WFS&VERSION=2.0.0&REQUEST=FuzzyNameSearch&LANGUAGE=_LANG_&NAME=";
    public static final String DEFAULT_GETFEATURE_TEMPLATE = "?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetFeature&TYPENAMES=SI_LocationInstance&language=_LANG_&FILTER=";
    public static final String JSONKEY_LOCATIONTYPES = "SI_LocationTypes";
    public static final String LOCATIONTYPE_ID_PREFIX = "SI_LocationType.";
    public static final String PARAM_COUNTRY = "country";
    private static final String PROPERTY_SERVICE_SRS = "search.channel.ELFGEOLOCATOR_CHANNEL.service.srs";
    private static final String PROPERTY_SERVICE_REVERSEGEOCODE_TEMPLATE = "search.channel.ELFGEOLOCATOR_CHANNEL.service.reversegeocode.template";
    public static final String REQUEST_REVERSEGEOCODE_TEMPLATE = PropertyUtil.get(PROPERTY_SERVICE_REVERSEGEOCODE_TEMPLATE, DEFAULT_REVERSEGEOCODE_TEMPLATE);
    private static final String PROPERTY_SERVICE_GETFEATUREAU_TEMPLATE = "search.channel.ELFGEOLOCATOR_CHANNEL.service.getfeatureau.template";
    public static final String REQUEST_GETFEATUREAU_TEMPLATE = PropertyUtil.get(PROPERTY_SERVICE_GETFEATUREAU_TEMPLATE, DEFAULT_GETFEATUREAU_TEMPLATE);
    private static final String PROPERTY_SERVICE_FUZZY_TEMPLATE = "search.channel.ELFGEOLOCATOR_CHANNEL.service.fuzzy.template";
    public static final String REQUEST_FUZZY_TEMPLATE = PropertyUtil.get(PROPERTY_SERVICE_FUZZY_TEMPLATE, DEFAULT_FUZZY_TEMPLATE);
    private static final String PROPERTY_SERVICE_GETFEATURE_TEMPLATE = "search.channel.ELFGEOLOCATOR_CHANNEL.service.getfeature.template";
    public static final String REQUEST_GETFEATURE_TEMPLATE = PropertyUtil.get(PROPERTY_SERVICE_GETFEATURE_TEMPLATE, DEFAULT_GETFEATURE_TEMPLATE);
    private static final String PROPERTY_SERVICE_GEOLOCATOR_LOCATIONTYPES = "search.channel.ELFGEOLOCATOR_CHANNEL.service.locationtype.json";
    public static final String LOCATIONTYPE_ATTRIBUTES = PropertyUtil.get(PROPERTY_SERVICE_GEOLOCATOR_LOCATIONTYPES, ID + ".json");
    public static final String PROPERTY_AUTOCOMPLETE_URL = PropertyUtil.getOptional("search.channel.ELFGEOLOCATOR_CHANNEL.autocomplete.url");
    public static final String PROPERTY_AUTOCOMPLETE_USERNAME = PropertyUtil.getOptional("search.channel.ELFGEOLOCATOR_CHANNEL.autocomplete.userName");
    public static final String PROPERTY_AUTOCOMPLETE_PASSWORD = PropertyUtil.getOptional("search.channel.ELFGEOLOCATOR_CHANNEL.autocomplete.password");

    // Parameters
    public static final String PARAM_NORMAL = "normal";
    public static final String PARAM_REGION = "region";
    public static final String PARAM_FILTER = "filter";
    public static final String PARAM_FUZZY = "fuzzy";
    
    private static final String LIKE_LITERAL_HOLDER = "_like-literal_";
    private static Map<String, Double> elfScalesForType = new HashMap<>();
    private static Map<String, Integer> elfLocationPriority = new HashMap<>();
    private static JSONObject elfLocationTypes = null;
    private static JSONObject elfNameLanguages = null;
    private final String locationType = "ELFGEOLOCATOR_CHANNEL.json";
    public String serviceURL = null;
    public ELFGeoLocatorParser elfParser = null;
    private Logger log = LogFactory.getLogger(this.getClass());

    /* --- For unit testing: ----------------------------------------------- */
    private HttpURLConnection conn;
    private String likeQueryXMLtemplate = null;

    public ELFGeoLocatorSearchChannel() {
    }

    public ELFGeoLocatorSearchChannel(HttpURLConnection conn) {
        this.conn = conn;
    }

    @Override
    public HttpURLConnection getConnection(final String url) {
        if (conn != null) {
            return conn;
        }

        return super.getConnection(url);
    }

    /* --------------------------------------------------------------------- */

    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        if(serviceURL == null) {
            throw new RuntimeException("ELFGeolocator didn't initialize - provide url with property: " + PROPERTY_SERVICE_URL);
        }
        log.debug("ServiceURL set to " + serviceURL);

        readLocationTypes();

        // read available languages
        try (InputStream languageStream = this.getClass().getResourceAsStream("namelanguage.json");
             InputStreamReader reader = new InputStreamReader(languageStream)) {
            elfNameLanguages = JSONHelper.createJSONObject4Tokener(new JSONTokener(reader));
        } catch (IOException e) {
            log.warn("Couldn't load language selection from 'namelanguage.json':", e.getMessage());
        }

        elfParser = new ELFGeoLocatorParser(PropertyUtil.getOptional(PROPERTY_SERVICE_SRS), this);

        try {
            likeQueryXMLtemplate = IOHelper.readString(getClass().getResourceAsStream("geolocator-wildcard.xml"));
        } catch (Exception e) {
            log.debug("Reading geolocator like search XML template failed", e);
        }
    }

    private void readLocationTypes() {
        if (this.elfScalesForType.size() > 0) {
            return;
        }

        try (InputStream inp2 = getLocationTypeStream();
             InputStreamReader reader = new InputStreamReader(inp2)) {
            JSONTokener tokenizer = new JSONTokener(reader);
            this.elfLocationTypes = JSONHelper.createJSONObject4Tokener(tokenizer);
            if (elfLocationTypes == null) {
                return;
            }
            JSONArray types = JSONHelper.getJSONArray(elfLocationTypes, JSONKEY_LOCATIONTYPES);
            if (types == null) {
                return;
            }
            // Set Map scale values
            for (int i = 0; i < types.length(); i++) {

                JSONObject jsobj = types.getJSONObject(i);
                String type_range = JSONHelper.getStringFromJSON(jsobj, "gml_id", null);
                Double scale = jsobj.optDouble("scale");
                int priority = jsobj.optInt("priority");
                if (type_range == null || scale == null) {
                    continue;
                }
                String[] sranges = type_range.split("-");
                int i1 = Integer.parseInt(sranges[0]);
                int i2 = sranges.length > 1 ? Integer.parseInt(sranges[1]) : i1;
                for (int k = i1; k <= i2; k++) {
                    String key = LOCATIONTYPE_ID_PREFIX + String.valueOf(k);
                    this.elfScalesForType.put(key, scale);
                    this.elfLocationPriority.put(key, Integer.valueOf(priority));
                }
            }
        } catch (Exception e) {
            log.debug("Initializing Elf geolocator search configs failed", e);
        }
    }

    private InputStream getLocationTypeStream() {
        InputStream inp2 = this.getClass().getResourceAsStream(locationType);
        if (inp2 != null) {
            return inp2;
        }
        // Try to get user defined setup file
        try {
            return new FileInputStream(LOCATIONTYPE_ATTRIBUTES);
        } catch (Exception e) {
            log.info("No setup found for location type based scaling in geolocator seach", e);
        }
        throw new RuntimeException("No location types data");
    }

    public Capabilities getCapabilities() {
        return Capabilities.BOTH;
    }

    public ChannelSearchResult reverseGeocode(SearchCriteria searchCriteria) {

        StringBuffer buf = new StringBuffer(serviceURL);
        // reverse geocoding
        // Transform lon,lat
        String[] lonlat = elfParser.transformLonLat("" + searchCriteria.getLon(), "" + searchCriteria.getLat(), searchCriteria.getSRS());
        if (lonlat == null) {
            log.warn("Invalid lon/lat coordinates", searchCriteria.getLon(), searchCriteria.getLat());
            return null;
        }
        String request = REQUEST_REVERSEGEOCODE_TEMPLATE.replace(KEY_LATITUDE_HOLDER, lonlat[1]);
        Locale locale = new Locale(searchCriteria.getLocale());
        String lang3 = locale.getISO3Language();
        request = request.replace(KEY_LONGITUDE_HOLDER, lonlat[0]);
        request = request.replace(KEY_LANG_HOLDER, lang3);
        buf.append(request);
        String data;
        try {
            data = IOHelper.readString(getConnection(buf.toString()));
            // Clean xml version for geotools parser for faster parse
            data = data.replace(RESPONSE_CLEAN, "");
        } catch (Exception e) {
            log.warn("Unable to fetch data " + buf.toString());
            log.error(e);
            return new ChannelSearchResult();
        }

        boolean exonym = true;
        // New definitions - no mode setups any more in UI - exonym is always true
        ChannelSearchResult result = elfParser.parse(data, searchCriteria.getSRS(), locale, exonym);

        // Add used search method
        result.setSearchMethod(findSearchMethod(searchCriteria));
        return result;
    }

    protected boolean hasWildcard(String query) {
        return query != null && (query.contains("*") || query.contains("#"));
    }

    protected String getWildcardQuery(SearchCriteria searchCriteria) {
        String postData = likeQueryXMLtemplate;
        return postData.replace(LIKE_LITERAL_HOLDER, searchCriteria.getSearchString());
    }

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    public String getData(SearchCriteria searchCriteria)
            throws Exception {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }

        // wildcard search
        String searchString = searchCriteria.getSearchString();
        if (hasWildcard(searchString)) {
            log.debug("Wildcard search: ", searchString);

            String postData = getWildcardQuery(searchCriteria);

            StringBuffer buf = new StringBuffer(serviceURL);

            log.debug("Server request: " + buf.toString());
            
            HttpURLConnection conn = getConnection(buf.toString());
            IOHelper.writeToConnection(conn, postData);
            String response = IOHelper.readString(conn);

            log.debug("Server response: " + response);
            return response;
        }

        // Language
        Locale locale = new Locale(searchCriteria.getLocale());
        String lang3 = locale.getISO3Language();

        StringBuffer buf = new StringBuffer(serviceURL);
        if ("true".equals(searchCriteria.getParamAsString(PARAM_FILTER))) {
            log.debug("Exact search (AU)");
            
            // Exact search limited to AU region - case sensitive - no fuzzy support
            String request = REQUEST_GETFEATUREAU_TEMPLATE.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));
            request = request.replace(KEY_AU_HOLDER, URLEncoder.encode(searchCriteria.getParam(PARAM_REGION).toString(), "UTF-8"));
            request = request.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request);
        } else if ("true".equals(searchCriteria.getParamAsString(PARAM_FUZZY))) {
            log.debug("Fuzzy search");
            
            // Fuzzy search
            buf.append(REQUEST_FUZZY_TEMPLATE.replace(KEY_LANG_HOLDER, lang3));
            buf.append(URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));

            // Location type
            final String locationType = "locationtype";
            if (hasParam(searchCriteria, locationType)) {
                buf.append("&LOCATIONTYPE" + "=" + searchCriteria.getParam(locationType));
            }
            
            // Name language
            final String nameLanguage = "namelanguage";
            if (hasParam(searchCriteria, nameLanguage)) {
                buf.append("&NAMELANGUAGE" + "=" + searchCriteria.getParam(nameLanguage));
            }
            
            // Nearest
            final String nearest = "nearest";
            if (hasParam(searchCriteria, nearest)) {
                buf.append("&NEAREST=" + searchCriteria.getParam(nearest));
                buf.append("&LON=" + searchCriteria.getParam("lon"));
                buf.append("&LAT=" + searchCriteria.getParam("lat"));
                
                // API supports EPSG:4258, EPSG:3034, EPSG:3035 ja EPSG:3857 coordinates
                buf.append("&SRSNAME=" + searchCriteria.getSRS());
            }
        } else {
            log.debug("Exact search");
            
            // Exact search - case sensitive
            String filter = getFilter(searchCriteria);
            String request = REQUEST_GETFEATURE_TEMPLATE.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request);
            buf.append(filter);
        }
        
        log.debug("Server request: " + buf.toString());
        return IOHelper.readString(getConnection(buf.toString()));
    }

    protected String getFilter(SearchCriteria searchCriteria) {
        String country = searchCriteria.getParamAsString(PARAM_COUNTRY);

        try {
            final String adminFilter = new ELFGeoLocatorQueryHelper().getFilter(searchCriteria.getSearchString(), country);
            return URLEncoder.encode(adminFilter, IOHelper.CHARSET_UTF8);
        } catch (ServiceRuntimeException | UnsupportedEncodingException e) {
            log.error(e, "Couldn't create filter for country. Using default filter");
        }
        // will result in empty filter
        return "";
    }

    /**
     * Check if criteria has named extra parameter and it's not empty
     *
     * @param sc
     * @param param
     * @return
     */
    public boolean hasParam(SearchCriteria sc, final String param) {
        final Object obj = sc.getParam(param);
        return obj != null && !obj.toString().isEmpty();
    }

    public JSONObject getElfLocationTypes() {
        return this.elfLocationTypes;
    }

    public JSONObject getElfNameLanguages() {
        return this.elfNameLanguages;
    }

    /**
     * Returns the channel search results.
     *
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        try {
            String fuzzy = (String) searchCriteria.getParam(PARAM_FUZZY);
            boolean fuzzyDone = false;
            if (fuzzy != null && fuzzy.equals("true")) {
                searchCriteria.addParam(PARAM_NORMAL, "false");
                searchCriteria.addParam(PARAM_FUZZY, "true");
                fuzzyDone = true;
            }
            
            String data = getData(searchCriteria);
            Locale locale = new Locale(searchCriteria.getLocale());
            
            // Clean xml version for geotools parser for faster parse
            data = data.replace(RESPONSE_CLEAN, "");
            boolean exonym = true;
            // New definitions - no mode setups any more in UI - exonym is always true
            ChannelSearchResult result = elfParser.parse(data, searchCriteria.getSRS(), locale, exonym);

            // Execute fuzzy search, if no result in exact search (and fuzzy search has not been done already)
            if (result.getSearchResultItems().size() == 0 && findSearchMethod(searchCriteria).equals(PARAM_NORMAL) && !fuzzyDone) {
                // Try fuzzy search, if empty
                searchCriteria.addParam(PARAM_NORMAL, "false");
                searchCriteria.addParam(PARAM_FUZZY, "true");
                result = doSearch(searchCriteria);
            }
            // Add used search method
            result.setSearchMethod(findSearchMethod(searchCriteria));
            return result;

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of ELF GeoLocator");
            return new ChannelSearchResult();
        }
    }

    private String findSearchMethod(SearchCriteria sc) {
        String method = "unknown";
        if ("true".equals(sc.getParamAsString(PARAM_FILTER))) {
            // Exact search limited to AU region - case sensitive - no fuzzy support
            method = PARAM_FILTER;

        } else if ("true".equals(sc.getParamAsString(PARAM_FUZZY))) {
            // Fuzzy search
            method = PARAM_FUZZY;
        } else {
            // Exact search - case sensitive
            method = PARAM_NORMAL;
        }
        return method;
    }

    public Map<String, Double> getElfScalesForType() {
        return elfScalesForType;
    }

    public Map<String, Integer> getElfLocationPriority() {
        return elfLocationPriority;
    }

    /**
     * Skip scale setting in ELF
     * Scale is not based on type value
     *
     * @param item
     */
    @Override
    public void calculateCommonFields(final SearchResultItem item) {
        double scale = item.getZoomScale();
        super.calculateCommonFields(item);
        if (scale != -1) {
            item.setZoomScale(scale);
        }
    }

    public List<String> doSearchAutocomplete(String searchString) {
        if(PROPERTY_AUTOCOMPLETE_URL == null || PROPERTY_AUTOCOMPLETE_URL.isEmpty()) {
            return Collections.emptyList();
        }
        JSONObject jsonObject;
        try {
            log.info("Creating autocomplete search url with url:", PROPERTY_AUTOCOMPLETE_URL);
            HttpURLConnection conn = IOHelper.getConnection(PROPERTY_AUTOCOMPLETE_URL,
                    PROPERTY_AUTOCOMPLETE_USERNAME, PROPERTY_AUTOCOMPLETE_PASSWORD);
            IOHelper.writeToConnection(conn, getElasticQuery(searchString));
            String result = IOHelper.readString(conn);
            jsonObject = new JSONObject(result);
        }
        catch (Exception ex) {
            log.error("Couldn't open or read from connection for search channel!");
            throw new RuntimeException("Couldn't open or read from connection!", ex);
        }

        HitCombiner combiner = new HitCombiner();
        try {
            JSONArray fuzzyHits = jsonObject.getJSONArray("fuzzy_search").getJSONObject(0).getJSONArray("options");
            for (int i = 0; i < fuzzyHits.length(); i++) {
                combiner.addHit(fuzzyHits.getJSONObject(i), false);
            }

            JSONArray normalHits = jsonObject.getJSONArray("normal_search").getJSONObject(0).getJSONArray("options");
            for (int i = 0; i < normalHits.length(); i++) {
                JSONObject hit = normalHits.getJSONObject(i);
                boolean isExact = searchString.trim().equalsIgnoreCase(hit.getString("text"));
                combiner.addHit(hit, isExact);
            }
        }
        catch (JSONException ex) {
            log.error("Unexpected autocomplete service JSON response structure!", ex.getMessage());
        }

        return combiner.getSortedHits();
    }


    protected String getElasticQuery(String query) {
        // {"normal_search":{"text":"[user input]","completion":{"field":"name_suggest","size":20}},"fuzzy_search":{"text":"[user input]","completion":{"field":"name_suggest","size":20,"fuzzy":{"fuzziness":5}}}}
        JSONObject elasticQueryTemplate = JSONHelper.createJSONObject("{\"normal_search\":{\"completion\":{\"field\":\"name_suggest\",\"size\":20}},\"fuzzy_search\":{\"completion\":{\"field\":\"name_suggest\",\"size\":20,\"fuzzy\":{\"fuzziness\":5}}}}");
        try {
            // set the actual search query
            elasticQueryTemplate.optJSONObject("normal_search").put("text", query);
            elasticQueryTemplate.optJSONObject("fuzzy_search").put("text", query);
        } catch(Exception ignored) {}
        return elasticQueryTemplate.toString();
    }

}
