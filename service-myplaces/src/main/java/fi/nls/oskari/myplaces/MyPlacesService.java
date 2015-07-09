package fi.nls.oskari.myplaces;

import java.util.List;

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

public abstract class MyPlacesService extends OskariComponent {

    public static final String RESOURCE_TYPE_MYPLACES = "myplaces";
    public final static String PERMISSION_TYPE_DRAW = "DRAW";
    public static final String MYPLACES_LAYERID_PREFIX = "myplaces_";

    static String MYPLACES_CLIENT_WMS_URL = PropertyUtil.getOptional("myplaces.client.wmsurl");

    private static String MYPLACES_WMS_NAME = PropertyUtil.get("myplaces.xmlns.prefix", "ows")+":my_places_categories";
    private static String MYPLACES_ACTUAL_WMS_URL = PropertyUtil.get("myplaces.wms.url");
    private final static LayerJSONFormatterWMS JSON_FORMATTER = new LayerJSONFormatterWMS();

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
        layer.setTitle(lang, mpLayer.getPublisher_name());
        layer.setOpacity(50);
        JSONObject options = JSONHelper.createJSONObject("singleTile", true);
        JSONHelper.putValue(options, "transitionEffect", JSONObject.NULL);
        layer.setOptions(options);

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
