package fi.nls.oskari.search.channel;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

public class WFSSearchChannel extends SearchChannel {

    private Logger log = LogFactory.getLogger(this.getClass());
    public static final String ID_PREFIX = "WFSSEARCH_CHANNEL_";
    
    public static final String PARAM_GEOMETRY = "GEOMETRY";
    public static final String PARAM_WFS_SEARCH_CHANNEL_TYPE = "WFS_SEARCH_CHANNEL_TYPE";
    public static final String PARAM_WFS_SEARCH_CHANNEL_TITLE = "WFS_SEARCH_CHANNEL_TITLE";
    public static final String PARAM_WFS_SEARCH_CHANNEL_ISADDRESS = "WFS_SEARCH_CHANNEL_ISADDRESS";
    public static final String GT_GEOM_POINT = "POINT";
    public static final String GT_GEOM_LINESTRING = "LINESTRING";
    public static final String GT_GEOM_POLYGON = "POLYGON";
    public static final String GT_GEOM_MULTIPOINT = "MULTIPOINT";
    public static final String GT_GEOM_MULTILINESTRING = "MULTILINESTRING";
    public static final String GT_GEOM_MULTIPOLYGON = "MULTIPOLYGON";

	private WFSSearchChannelsConfiguration config;

	public WFSSearchChannel(WFSSearchChannelsConfiguration config) {
		this.config = config;
	}

    public JSONObject getUILabels() {
        JSONObject response = new JSONObject();
        Iterator<String> keys = config.getTopic().keys();
        while(keys.hasNext()) {
            String key = keys.next();
            JSONObject locale = JSONHelper.createJSONObject("name", config.getTopic().optString(key));
            JSONHelper.putValue(locale, "desc", config.getDesc().optString(key));
            JSONHelper.putValue(response, key, locale);
        }
        return response;
    }

    public boolean hasPermission(User user) {
        // TODO: check if user roles have VIEW_LAYER permission to wfslayer
        return true;
    }

    public String getId() {
        return ID_PREFIX + config.getId();
    }

    public boolean isDefaultChannel() {
        return config.getIsDefault();
    }

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private JSONObject getData(SearchCriteria searchCriteria) throws Exception {
        JSONArray params = config.getParamsForSearch();
        if(params.length() == 0) {
            // nothing to search for
            return null;
        }
        String searchStr = searchCriteria.getSearchString();
        log.debug("[WFSSEARCH] Search string: " + searchStr);
        String maxFeatures = PropertyUtil.get("search.channel.WFSSEARCH_CHANNEL.maxFeatures", "100");

        JSONArray paramsJSONArray = new JSONArray();

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("service", "WFS");
        urlParams.put("request", "GetFeature");
        urlParams.put("outputformat", "json");
        urlParams.put("version", config.getVersion());
        urlParams.put("typeName", config.getLayerName());
        urlParams.put("srsName", config.getSrs());

        if(maxFeatures != null && !maxFeatures.isEmpty()){
            if(config.getVersion().equals("1.1.0")){
                urlParams.put("maxFeatures", maxFeatures);
            } else{
                urlParams.put("count", maxFeatures);
            }
        }
        StringBuffer filter = new StringBuffer("<Filter>");
        String isKiinteitoTunnus = searchStr.replace("-","");

        if(config.getIsAddress() && !isKiinteitoTunnus.matches("[0-9]+")){
            filter.append("<And>");
            String streetName = searchStr;
            String streetNumber = "";
            // find last word and if it is number then it must be street number?
            String lastWord = searchStr.substring(searchStr.lastIndexOf(" ") + 1);

            if (isStreetNumber(lastWord)) {
                // override streetName without, street number
                streetName = searchStr.substring(0, searchStr.lastIndexOf(" "));
                log.debug("[tre] found streetnumber " + streetNumber);
                streetNumber = lastWord;
            }

            filter.append("<PropertyIsLike wildCard='*' singleChar='>' escape='!' matchCase='false'>" +
                "<PropertyName>"+params.getString(0)+"</PropertyName><Literal>"+ streetName +
                "*</Literal></PropertyIsLike>"
            );

            filter.append("<PropertyIsLike wildCard='*' singleChar='>' escape='!' matchCase='false'>" +
                "<PropertyName>"+params.getString(1)+"</PropertyName><Literal>"+ streetNumber +
                "*</Literal></PropertyIsLike>"
            );

            paramsJSONArray.put(params.getString(0));
            paramsJSONArray.put(params.getString(1));

            filter.append("</And>");
        } else {

            if(params.length()>1){
                filter.append("<Or>");
            }

            for(int j=0;j<params.length();j++){
                String param = params.getString(j);
                filter.append("<PropertyIsLike wildCard='*' singleChar='.' escape='!' matchCase='false'>" +
                        "<PropertyName>"+param+"</PropertyName><Literal>*"+ searchStr +
                        "*</Literal></PropertyIsLike>"
                        );
                paramsJSONArray.put(param);
            }

            if(params.length()>1){
                filter.append("</Or>");
            }

        }

        filter.append("</Filter>");
        urlParams.put("Filter", filter.toString().trim());

        HttpURLConnection connection = getConnection(IOHelper.constructUrl(config.getUrl(), urlParams));
        if(config.requiresAuth()) {
            IOHelper.setupBasicAuth(connection, config.getUsername(), config.getPassword());
        }
        String WFSData = IOHelper.readString(connection);
        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            log.warn("Error response from WFS channel id:", config.getId(), "url:", config.getUrl());
            log.debug("Response:", WFSData);
            return null;
        }
        //log.debug("[WFSSEARCH] WFSData: " + WFSData);
        JSONObject wfsJSON = new JSONObject(WFSData);
        wfsJSON.put(PARAM_WFS_SEARCH_CHANNEL_TYPE, config.getTopic().getString(searchCriteria.getLocale()));
        wfsJSON.put(PARAM_WFS_SEARCH_CHANNEL_TITLE, paramsJSONArray);
        wfsJSON.put(PARAM_WFS_SEARCH_CHANNEL_ISADDRESS, config.getIsAddress());
        return wfsJSON;
    }
    
    /**
     * Returns the true if test contains numbers and/or a/b.
     *
     * @param test Search criteria.
     * @return true if string can be set to street number field in wfs query.
     */
    private boolean isStreetNumber(String test) {
        log.debug("[tre] street number candidate: " + test);
        return test.matches("[0-9-a-b]+");
    }

    /**
     * Returns the channel search results.
     *
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        String queryStr = searchCriteria.getSearchString();
        log.debug("[WFSSEARCH] doSearch queryStr: " + queryStr);     
                
        try {
            final JSONObject resp = getData(searchCriteria);
            if(resp == null) {
                log.info("No response from WFS channel with id", config.getId());
                return searchResultList;
            }

            log.debug("[WFSSEARCH] Response from server: " + resp);

            JSONArray featuresArr = resp.getJSONArray("features");

            for (int i = 0; i < featuresArr.length(); i++) {
                SearchResultItem item = new SearchResultItem();
                JSONObject loopJSONObject = featuresArr.getJSONObject(i);

                item.setType(resp.getString(PARAM_WFS_SEARCH_CHANNEL_TYPE));
                item.setTitle(getTitle(resp, loopJSONObject, resp.getBoolean(PARAM_WFS_SEARCH_CHANNEL_ISADDRESS)));

                if(loopJSONObject.has("geometry")) {
                    JSONObject featuresObj_geometry = loopJSONObject.getJSONObject("geometry");

                    String geomType = featuresObj_geometry.getString("type").toUpperCase();
                    GeometryJSON geom = new GeometryJSON(3);
                    if (geomType.equals(GT_GEOM_POLYGON)) {
                        Polygon polygon = geom.readPolygon(featuresObj_geometry.toString());
                        item.addValue(PARAM_GEOMETRY, WKTHelper.getWKT(polygon));
                        item.setLat(Double.toString(polygon.getCentroid().getCoordinate().y));
                        item.setLon(Double.toString(polygon.getCentroid().getCoordinate().x));
                    } else if (geomType.equals(GT_GEOM_LINESTRING)) {
                        LineString lineGeom = geom.readLine(featuresObj_geometry.toString());
                        item.addValue(PARAM_GEOMETRY, WKTHelper.getWKT(lineGeom));
                        item.setLat(Double.toString(lineGeom.getCentroid().getCoordinate().y));
                        item.setLon(Double.toString(lineGeom.getCentroid().getCoordinate().x));
                    } else if (geomType.equals(GT_GEOM_POINT)) {
                        com.vividsolutions.jts.geom.Point pointGeom = geom.readPoint(featuresObj_geometry.toString());
                        item.addValue(PARAM_GEOMETRY, WKTHelper.getWKT(pointGeom));
                        item.setLat(Double.toString(pointGeom.getCentroid().getCoordinate().y));
                        item.setLon(Double.toString(pointGeom.getCentroid().getCoordinate().x));
                    } else if (geomType.equals(GT_GEOM_MULTIPOLYGON)) {
                        MultiPolygon polygon = geom.readMultiPolygon(featuresObj_geometry.toString());
                        item.addValue(PARAM_GEOMETRY, WKTHelper.getWKT(polygon));
                        item.setLat(Double.toString(polygon.getCentroid().getCoordinate().y));
                        item.setLon(Double.toString(polygon.getCentroid().getCoordinate().x));
                    } else if (geomType.equals(GT_GEOM_MULTILINESTRING)) {
                        MultiLineString lineGeom = geom.readMultiLine(featuresObj_geometry.toString());
                        item.addValue(PARAM_GEOMETRY, WKTHelper.getWKT(lineGeom));
                        item.setLat(Double.toString(lineGeom.getCentroid().getCoordinate().y));
                        item.setLon(Double.toString(lineGeom.getCentroid().getCoordinate().x));
                    } else if (geomType.equals(GT_GEOM_MULTIPOINT)) {
                        MultiPoint pointGeom = geom.readMultiPoint(featuresObj_geometry.toString());
                        item.addValue(PARAM_GEOMETRY, WKTHelper.getWKT(pointGeom));
                        item.setLat(Double.toString(pointGeom.getCentroid().getCoordinate().y));
                        item.setLon(Double.toString(pointGeom.getCentroid().getCoordinate().x));
                    }
                }

                item.setVillage("Tampere");
                item.setDescription("");
                item.setLocationTypeCode("");

                searchResultList.addItem(item);
            }
        
        } catch (Exception e) {
            log.error(e, "[WFSSEARCH] Failed to search locations from register of WFSSearchChannel");
        }
        return searchResultList;
    }
    
    /**
     * Get title
     * @param resp
     * @param loopJSONObject
     * @return title
     */
    private String getTitle(JSONObject resp, JSONObject loopJSONObject, Boolean isAddress){
    	StringBuffer buf = new StringBuffer();
    	JSONArray params;
		try {
			params = resp.getJSONArray(PARAM_WFS_SEARCH_CHANNEL_TITLE);
			JSONObject properties = loopJSONObject.getJSONObject("properties");
	    	
			for(int i=0;i<params.length();i++) {
	    		String param = params.getString(i);
	    		buf.append(properties.getString(param));
	    		if(i<params.length()-1 && !isAddress) {
	    			buf.append(", ");
	    		}else{
	    			buf.append(" ");
	    		}
	    	}
		} catch (JSONException e) {
			log.error(e, "[WFSSEARCH] Failed to get Title");
		}
    	return buf.toString();
    }

    /**
     * Only checks if config.ids match
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WFSSearchChannel)) {
            return false;
        }

        WFSSearchChannel that = (WFSSearchChannel) o;

        if(config == null) {
            return false;
        }

        return config.getId() == that.config.getId();

    }

    @Override
    public int hashCode() {
        return config != null ? config.getId() : 0;
    }
}