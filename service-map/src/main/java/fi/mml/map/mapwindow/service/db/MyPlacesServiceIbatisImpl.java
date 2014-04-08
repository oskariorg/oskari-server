package fi.mml.map.mapwindow.service.db;

import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wms.WMSCapabilities;
import org.json.JSONObject;

public class MyPlacesServiceIbatisImpl extends BaseIbatisService<MyPlaceCategory>
        implements MyPlacesService {

    private final static Logger log = LogFactory.getLogger(
            MyPlacesServiceIbatisImpl.class);

    private static String MYPLACES_WMS_NAME = PropertyUtil.get("myplaces.xmlns.prefix", "ows")+":my_places_categories";
    private static String MYPLACES_CLIENT_WMS_URL = PropertyUtil.get("myplaces.client.wmsurl");
    private static String MYPLACES_ACTUAL_WMS_URL = PropertyUtil.get("myplaces.wms.url");

    private static final String MYPLACES_LAYERID_PREFIX = "myplaces_";

    private final static LayerJSONFormatterWMS JSON_FORMATTER = new LayerJSONFormatterWMS();
    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    @Override
    protected String getNameSpace() {
        return "MyPlace";
    }

    private SqlMapClient client = null;

    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return "META-INF/SqlMapConfig_MyPlace.xml";
    }
    /**
     * Returns SQLmap
     * 
     * @return
     */
    @Override
    protected SqlMapClient getSqlMapClient() {
        if (client != null) {
            return client;
        }

        Reader reader = null;
        try {
            String sqlMapLocation = getSqlMapLocation();
            reader = Resources.getResourceAsReader(sqlMapLocation);
            client = SqlMapClientBuilder.buildSqlMapClient(reader);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SQL client", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Check if user can insert a place in category
     * @param user
     * @return
     */
    public boolean canInsert(final User user, final long categoryId) {
        final Resource resource = getResource(categoryId);
        final boolean isDrawLayer = resource.hasPermission(user, PERMISSION_TYPE_DRAW);
        // returns true if users own layer or if published as a draw layer
        return isDrawLayer || canModifyCategory(user,categoryId);
    }

    public Resource getResource(final long categoryId) {
        return getResource(MYPLACES_LAYERID_PREFIX + categoryId);
    }

    public Resource getResource(final String myplacesLayerId) {
        final Resource resource = new Resource();
        resource.setType(RESOURCE_TYPE_MYPLACES);
        resource.setMapping(myplacesLayerId);
        final Resource dbRes = permissionsService.findResource(resource);
        if(dbRes == null) {
            return resource;
        }
        return dbRes;
    }

    /**
     * Check if user can update/delete place
     * @param user
     * @return
     */
    public boolean canModifyPlace(final User user, final long placeId) {

        try {
            MyPlace place = (MyPlace) getSqlMapClient().queryForObject(getNameSpace() + ".findPlace", placeId);
            if(place == null) {
                return false;
            }
            return place.isOwnedBy(user.getUuid());
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load place with id:", placeId);
        }
        return false;
    }

    public boolean canModifyCategory(final User user, final String layerId) {
        log.debug("canModifyCategory - layer:", layerId, "- User:", user);
        if(layerId == null || user.isGuest()) {
            return false;
        }
        if(!layerId.startsWith(MYPLACES_LAYERID_PREFIX)) {
            return false;
        }
        final long categoryId = ConversionHelper.getLong(layerId.substring(MYPLACES_LAYERID_PREFIX.length()), -1);
        if(categoryId == -1) {
            return false;
        }
        return canModifyCategory(user, categoryId);
    }

    /**
     * Check if user can update/delete category
     * @param user
     * @return
     */
    public boolean canModifyCategory(final User user, final long categoryId) {
        log.debug("canModifyCategory - categoryId:", categoryId, "- User:", user);
        MyPlaceCategory cat = queryForObject(getNameSpace() + ".find", (int) categoryId);
        if(cat == null) {
            return false;
        }
        return cat.isOwnedBy(user.getUuid());
    }

    /**
     * Updates a MyPlace publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {

        final Map<String, Object> data = new HashMap<String,Object>();
        data.put("publisher_name", name);
        data.put("uuid", uuid);
        data.put("id", id);
        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updatePublisherName", data);
        } catch (SQLException e) {
            log.error(e, "Failed to update publisher name", data);
        }
        return 0;
    }

   
    @Override
    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList) {
        return queryForList(getNameSpace() + ".findByIds", idList);
    }
    
    @Override
    public List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("searchKey", search + ":*");
        return queryForList(getNameSpace() + ".freeFind", data);
    }

    public JSONObject getCategoryAsWmsLayerJSON(final MyPlaceCategory mpLayer,
                                              final String lang, final boolean useDirectURL,
                                              final String uuid, final boolean modifyURLs) {

        final OskariLayer layer = new OskariLayer();
        layer.setExternalId(MYPLACES_LAYERID_PREFIX + mpLayer.getId());
        layer.setName(MYPLACES_WMS_NAME);
        layer.setType(OskariLayer.TYPE_WMS);
        layer.setName(lang, mpLayer.getCategory_name());
        layer.setTitle(lang, mpLayer.getPublisher_name());
        layer.setOpacity(50);
        layer.setOptions(JSONHelper.createJSONObject("singleTile", true));

        // if useDirectURL -> geoserver URL
        if(useDirectURL) {
            layer.setUrl(MYPLACES_ACTUAL_WMS_URL +
                    "(uuid='" + uuid + "'+OR+publisher_name+IS+NOT+NULL)+AND+category_id=" + mpLayer.getId());
        }
        else {
            layer.setUrl(MYPLACES_CLIENT_WMS_URL + mpLayer.getId() + "&");
        }

        final WMSCapabilities capabilities = new WMSCapabilities();
        // gfi
        capabilities.setQueryable(true);
        // capabilities.setFormats([]); // possibly needed

        JSONObject myPlaceLayer = JSON_FORMATTER.getJSON(layer, lang, modifyURLs, capabilities);
        // flag with metaType for frontend
        JSONHelper.putValue(myPlaceLayer, "metaType", "published");
        return myPlaceLayer;
    }
}
