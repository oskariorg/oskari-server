package fi.nls.oskari.myplaces;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wms.WMSCapabilities;
import org.json.JSONObject;

import java.util.List;

public abstract class MyPlacesService extends OskariComponent {

    public static final String RESOURCE_TYPE_MYPLACES = "myplaces";
    public static final String PERMISSION_TYPE_DRAW = "DRAW";
    public static final String MYPLACES_LAYERID_PREFIX = "myplaces_";

    private String MYPLACES_CLIENT_WMS_URL = PropertyUtil.getOptional("myplaces.client.wmsurl");

    private static String MYPLACES_WMS_NAME = PropertyUtil.get("myplaces.xmlns.prefix", "ows") + ":my_places_categories";
    private static String MYPLACES_ACTUAL_WMS_URL = PropertyUtil.get("myplaces.wms.url");
    private static final LayerJSONFormatterWMS JSON_FORMATTER = new LayerJSONFormatterWMS();

    public abstract List<MyPlaceCategory> getCategories();

    public abstract MyPlaceCategory findCategory(long id);

    public abstract List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList);

    public abstract int updatePublisherName(final long id, final String uuid, final String name);

    public abstract List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search);

    public abstract boolean canInsert(final User user, final long categoryId);

    public abstract boolean canModifyPlace(final User user, final long placeId);

    public abstract boolean canModifyCategory(final User user, final long categoryId);

    public abstract boolean canModifyCategory(final User user, final String layerId);

    public abstract Resource getResource(final long categoryId);

    public abstract Resource getResource(final String myplacesLayerId);

    public abstract void deleteByUid(final String uid);

    public MyPlacesService() {
        // default 'myplaces.client.wmsurl' to ajax url for tiles if not configured
        if (MYPLACES_CLIENT_WMS_URL == null) {
            // action_route name points to fi.nls.oskari.control.myplaces.MyPlacesTileHandler
            MYPLACES_CLIENT_WMS_URL = PropertyUtil.get("oskari.ajax.url.prefix") + "action_route=MyPlacesTile&myCat=";
        }
    }

    public String getClientWMSUrl() {
        return MYPLACES_CLIENT_WMS_URL;
    }

    public JSONObject getCategoryAsWmsLayerJSON(final MyPlaceCategory mpLayer,
                                                final String lang, final boolean useDirectURL,
                                                final String uuid, final boolean modifyURLs) {

        final OskariLayer layer = new OskariLayer();
        layer.setExternalId(MYPLACES_LAYERID_PREFIX + mpLayer.getId());
        layer.setName(MYPLACES_WMS_NAME);
        layer.setType(OskariLayer.TYPE_WMS);
        layer.setName(lang, mpLayer.getCategory_name());

        /*
Version 1.1.0 works better as it has fixed coordinate order, the OL3 default 1.3.0 causes problems with some setups like:
ERROR org.geoserver.ows -
java.lang.RuntimeException: Unable to encode filter [[ geometry bbox POLYGON ((4913648.8700826 4969613.8817587, 4913648.8700826 4970013.1540413, 4914182.3089174 4970013.1540413, 4914182.3089174 4969613.8817587, 4913648.8700826 4969613.88
17587)) ] AND [[ category_id = 186 ] AND [ uuid = 8e1cd426-6d91-26-23 ]]]
        at org.geoserver.wfs.GetFeature.encodeQueryAsKvp(GetFeature.java:892)
        at org.geoserver.wfs.GetFeature.buildKvpFromRequest(GetFeature.java:814)
         */
        layer.setVersion("1.1.0");
        layer.setTitle(lang, mpLayer.getPublisher_name());
        layer.setOpacity(50);
        JSONObject options = JSONHelper.createJSONObject("singleTile", true);
        JSONHelper.putValue(options, "transitionEffect", JSONObject.NULL);
        layer.setOptions(options);

        // if useDirectURL -> geoserver URL
        if (useDirectURL) {
            layer.setUrl(MYPLACES_ACTUAL_WMS_URL +
                    "(uuid='" + uuid + "'+OR+publisher_name+IS+NOT+NULL)+AND+category_id=" + mpLayer.getId());
        } else {
            layer.setUrl(MYPLACES_CLIENT_WMS_URL + mpLayer.getId() + "&");
        }

        final WMSCapabilities capabilities = new WMSCapabilities();
        // enable gfi
        capabilities.setQueryable(true);

        JSONObject myPlaceLayer = JSON_FORMATTER.getJSON(layer, lang, modifyURLs, null, capabilities);
        // flag with metaType for frontend
        JSONHelper.putValue(myPlaceLayer, "metaType", "published");
        return myPlaceLayer;
    }


}
