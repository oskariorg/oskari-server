package fi.nls.oskari.myplaces;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataStyle;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterMYPLACES;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wms.WMSCapabilities;
import org.json.JSONObject;
import org.oskari.permissions.model.Resource;

import java.util.List;

public abstract class MyPlacesService extends OskariComponent {

    public static final String PERMISSION_TYPE_DRAW = "DRAW";
    public static final String MYPLACES_LAYERID_PREFIX = "myplaces_";

    private String MYPLACES_CLIENT_WMS_URL = PropertyUtil.getOptional("myplaces.client.wmsurl");
    private static final String MYPLACES_BASELAYER_ID = "myplaces.baselayer.id";
    private static final int BASE_WFS_LAYER_ID = PropertyUtil.getOptional(MYPLACES_BASELAYER_ID, -1);
    private static final OskariLayerService mapLayerService = new OskariLayerServiceMybatisImpl();

    private static String MYPLACES_WMS_NAME = PropertyUtil.get("myplaces.xmlns.prefix", "ows") + ":my_places_categories";
    private static String MYPLACES_ACTUAL_WMS_URL = PropertyUtil.get("myplaces.wms.url");
    private static final LayerJSONFormatterWMS JSON_FORMATTER_WMS = new LayerJSONFormatterWMS();
    private static final LayerJSONFormatterWFS JSON_FORMATTER_WFS = new LayerJSONFormatterWFS();
    private static final LayerJSONFormatterMYPLACES FORMATTER = new LayerJSONFormatterMYPLACES();
    private static final Logger LOGGER = LogFactory.getLogger(MyPlacesService.class);

    public abstract List<MyPlaceCategory> getCategories();

    public abstract MyPlaceCategory findCategory(long id);

    public abstract List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList);

    public abstract int updatePublisherName(final long id, final String uuid, final String name);


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
    /**
     * Returns the base WFS-layer for myplaces
     */
    public static OskariLayer getBaseLayer() {
        if (BASE_WFS_LAYER_ID == -1) {
            LOGGER.error("Myplaces baseId not defined. Please define", MYPLACES_BASELAYER_ID,
                    "property with value pointing to the baselayer in database.");
            return null;
        }
        return mapLayerService.find(BASE_WFS_LAYER_ID);
    }

    public String getClientWMSUrl() {
        return MYPLACES_CLIENT_WMS_URL;
    }
    
    // FIXME: remove hard-coded name from server side
    // This is a quick fix for common supported languages
    // frontend does this, but embedded maps don't have the same code as layers are shown with WMS
    private String getLayerUIName(String lang) {
        if (lang.equalsIgnoreCase("fi")) {
            return "Oma karttataso";
        } else if (lang.equalsIgnoreCase("sv")) {
            return "Mitt kartlager";
        }
        return "My map layer";
    }
    public static JSONObject parseLayerToJSON (final MyPlaceCategory mpLayer, final String srs) {
        return FORMATTER.getJSON(getBaseLayer(), mpLayer, srs);
    }

    public JSONObject getCategoryAsWmsLayerJSON(final MyPlaceCategory mpLayer,
                                                final String lang, final boolean useDirectURL,
                                                final String uuid, final boolean modifyURLs) {

        final OskariLayer layer = new OskariLayer();
        layer.setName(MYPLACES_WMS_NAME);
        layer.setType(OskariLayer.TYPE_WMS);
        String name = mpLayer.getCategory_name();
        if (name == null || name.isEmpty())  {
            name = getLayerUIName(lang);
        }
        layer.setName(lang, name);

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

        JSONObject myPlaceLayer = JSON_FORMATTER_WMS.getJSON(layer, lang, modifyURLs, null, capabilities);
        // flag with metaType for frontend
        JSONHelper.putValue(myPlaceLayer, "metaType", "published");
        JSONHelper.putValue(myPlaceLayer, "id", MYPLACES_LAYERID_PREFIX + mpLayer.getId());
        return myPlaceLayer;
    }
    public JSONObject getCategoryAsWfsLayerJSON(final MyPlaceCategory mpLayer, final String lang) {

        final OskariLayer layer = new OskariLayer();
        layer.setName(MYPLACES_WMS_NAME);
        layer.setType(OskariLayer.TYPE_WFS);
        String name = mpLayer.getCategory_name();
        if (name == null || name.isEmpty())  {
            name = getLayerUIName(lang);
        }
        layer.setName(lang, name);
        layer.setVersion("1.1.0");
        layer.setTitle(lang, mpLayer.getPublisher_name());
        layer.setOpacity(50);
        WFSLayerOptions wfsOpts = mpLayer.getWFSLayerOptions();
        OskariLayer base = getBaseLayer();
        wfsOpts.injectBaseLayerOptions(base.getOptions());
        layer.setOptions(wfsOpts.getOptions());

        JSONObject myPlaceLayer = JSON_FORMATTER_WFS.getJSON(layer, lang, false, null);
        // flag with metaType for frontend
        JSONHelper.putValue(myPlaceLayer, "metaType", "published");
        JSONHelper.putValue(myPlaceLayer, "id", MYPLACES_LAYERID_PREFIX + mpLayer.getId());
        return myPlaceLayer;
    }
}
