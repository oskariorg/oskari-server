package fi.nls.oskari.search.channel;

import java.net.HttpURLConnection;
import java.util.*;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.SelectItem;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
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
    public static final String GT_GEOM_POINT = "POINT";
    public static final String GT_GEOM_LINESTRING = "LINESTRING";
    public static final String GT_GEOM_POLYGON = "POLYGON";
    public static final String GT_GEOM_MULTIPOINT = "MULTIPOINT";
    public static final String GT_GEOM_MULTILINESTRING = "MULTILINESTRING";
    public static final String GT_GEOM_MULTIPOLYGON = "MULTIPOLYGON";

	private WFSSearchChannelsConfiguration config;
    private static PermissionsService permissionsService;

	public WFSSearchChannel(WFSSearchChannelsConfiguration config) {
        this.config = config;
	}

    PermissionsService getPermissionService() {
        // TODO: use OskariComponentManager when PermissionService supports it
        //return OskariComponentManager.getComponentOfType(PermissionsService.class);
        if(permissionsService == null) {
            permissionsService = new PermissionsServiceIbatisImpl();
        }
        return permissionsService;
    }

    public JSONObject getUILabels() {
        return config.getLocale();
    }

    public boolean hasPermission(User user) {
        // check if user roles have VIEW_LAYER permission to wfslayer
        Cache<Resource> cache = CacheManager.getCache(this.getClass().getName());
        final String cacheKey = config.getUrl() + config.getLayerName();
        Resource resource = cache.get(cacheKey);
        if(resource == null) {
            OskariLayerResource res = new OskariLayerResource(OskariLayer.TYPE_WFS, config.getUrl(), config.getLayerName());
            resource = getPermissionService().findResource(res);
            if(resource == null) {
                return false;
            }
            cache.put(cacheKey, resource);
        }
        return resource.hasPermission(user, Permissions.PERMISSION_TYPE_VIEW_LAYER);
    }

    public WFSChannelHandler getHandler() {
        Map<String, WFSChannelHandler> handlers = OskariComponentManager.getComponentsOfType(WFSChannelHandler.class);
        if(handlers.containsKey(config.getHandler())) {
            return handlers.get(config.getHandler());
        }
        return handlers.get(WFSChannelHandler.ID);
    }

    /**
     * From database oskari_wfs_search_channels-table config-column:
     * {
     *     "defaults" : {
     *         "region" : "",
     *         "desc" : "",
     *         "locationType" : ""
     *     }
     * }
     * @param item
     */
    private void setupDefaults(SearchResultItem item) {
        JSONObject defaults = config.getConfig().optJSONObject("defaults");
        item.setRegion(JSONHelper.getStringFromJSON(defaults, "region", ""));
        item.setDescription(JSONHelper.getStringFromJSON(defaults, "desc", ""));
        item.setLocationTypeCode(JSONHelper.getStringFromJSON(defaults, "locationType", ""));
    }

    public String getId() {
        return ID_PREFIX + config.getId();
    }

    @Override
    public boolean isDefaultChannel() {
        return config.getIsDefault();
    }

    private int maxCount = super.getMaxResults();

    public void init() {
        super.init();
        maxCount = config.getConfig().optInt("maxFeatures", -1);
        if(maxCount == -1) {
            maxCount = PropertyUtil.getOptional("search.channel.WFSSEARCH_CHANNEL.maxFeatures",
                    PropertyUtil.getOptional("search.max.results", maxCount));
        }
    }
    public int getMaxResults() {
        return maxCount;
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
        int maxFeatures = getMaxResults(searchCriteria.getMaxResults());

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("service", "WFS");
        urlParams.put("request", "GetFeature");
        urlParams.put("outputformat", "json");
        urlParams.put("version", config.getVersion());
        urlParams.put("typeName", config.getLayerName());
        urlParams.put("srsName", config.getSrs());

        if(maxFeatures != -1){
            if(config.getVersion().equals("1.1.0")){
                urlParams.put("maxFeatures", Integer.toString(maxFeatures));
            } else{
                urlParams.put("count", Integer.toString(maxFeatures));
            }
        }
        urlParams.put("Filter", getHandler().createFilter(searchCriteria, config));

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
        return new JSONObject(WFSData);
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
                JSONObject featureJSON = featuresArr.getJSONObject(i);

                item.setType(config.getName(searchCriteria.getLocale()));

                setupDefaults(item);
                item.setTitle(getTitle(featureJSON));

                if(featureJSON.has("geometry")) {
                    JSONObject featuresObj_geometry = featureJSON.getJSONObject("geometry");

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
                searchResultList.addItem(item);
            }
        
        } catch (Exception e) {
            log.error(e, "[WFSSEARCH] Failed to search locations from register of WFSSearchChannel");
        }
        return searchResultList;
    }
    
    /**
     * Get title from feature
     * @param featureJSON
     * @return title
     */
    private String getTitle(JSONObject featureJSON) {
    	JSONArray requestedProps = config.getParamsForSearch();
        List<SelectItem> values = new ArrayList<>();

        JSONObject properties = featureJSON.optJSONObject("properties");
        if(properties == null) {
            return null;
        }

        for(int i=0;i<requestedProps.length();i++) {
            String param = requestedProps.optString(i);
            SelectItem item = new SelectItem(param, properties.optString(param));
            values.add(item);
        }
    	return getHandler().getTitle(values);
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