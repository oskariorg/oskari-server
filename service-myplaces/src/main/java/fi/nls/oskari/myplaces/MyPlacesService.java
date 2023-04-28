package fi.nls.oskari.myplaces;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterMYPLACES;
import fi.nls.oskari.map.style.VectorStyleService;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
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
    public static MyPlaceCategory createDefaultCategory() {
        MyPlaceCategory category = new MyPlaceCategory();
        category.setName("");
        category.setDefault(true);
        category.setLocale(new JSONObject());
        VectorStyleService vss = OskariComponentManager.getComponentOfType(VectorStyleService.class);
        category.getWFSLayerOptions().setDefaultFeatureStyle(vss.getDefaultFeatureStyle());
        return category;
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
        return parseLayerToJSON(mpLayer, srs, PropertyUtil.getDefaultLanguage());
    }
    public static JSONObject parseLayerToJSON (final MyPlaceCategory mpLayer, final String srs, final String lang) {
        return FORMATTER.getJSON(getBaseLayer(), mpLayer, srs, lang);
    }


}
