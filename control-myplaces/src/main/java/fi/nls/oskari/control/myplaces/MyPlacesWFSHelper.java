package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.EmptyFeatureCollection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.oskari.geojson.GeoJSONReader;
import org.oskari.myplaces.service.mybatis.MyPlacesFeaturesServiceMybatisImpl;
import org.oskari.service.user.UserLayerService;

import java.util.ArrayList;
import java.util.List;

@Oskari
public class MyPlacesWFSHelper extends UserLayerService {

    private static final String PREFIX_MYPLACES = OskariLayer.TYPE_MYPLACES + "_";

    private MyPlacesService service;

    private MyPlacesFeaturesService featureService = new MyPlacesFeaturesServiceMybatisImpl();

    private OskariLayer getBaseLayer() {
        return MyPlacesService.getBaseLayer();
    }

    @Override
    public OskariLayer getOskariLayer(String layerId) {
        return getBaseLayer();
    }

    @Override
    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_MYPLACES);
    }

    private int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(PREFIX_MYPLACES.length()));
    }

    @Override
    public boolean hasViewPermission(String id, User user) {
        MyPlaceCategory layer = getLayer(parseId(id));
        if (layer == null) {
            return false;
        }
        return layer.isOwnedBy(user.getUuid()) || layer.isPublished();
    }

    protected MyPlaceCategory getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        }
        return service.findCategory(id);
    }

    public static List<MyPlace> parseMyPlaces(String input, boolean shouldSetId)
            throws JSONException {
        JSONObject featureCollection = new JSONObject(input);
        // Expect custom key featureCollection.srsName to contain srid in pattern of 'EPSG:srid'
        // if that doesn't exist or if we fail to parse the srid part out of it use 0 (unknown)
        String srsName = JSONHelper.optString(featureCollection, "srsName");
        int srid = getSrid(srsName, 0);
        JSONArray features = featureCollection.getJSONArray("features");
        final int n = features.length();
        List<MyPlace> myPlaces = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            JSONObject feature = features.getJSONObject(i);
            myPlaces.add(parseMyPlace(feature, shouldSetId, srid));
        }
        return myPlaces;
    }

    private static int getSrid(String srsName, int defaultValue) {
        if (srsName != null) {
            int i = srsName.lastIndexOf(':');
            if (i > 0) {
                srsName = srsName.substring(i + 1);
            }
            try {
                return Integer.parseInt(srsName);
            } catch (NumberFormatException ignroe) {}
        }
        return defaultValue;
    }

    private static MyPlace parseMyPlace(JSONObject feature, boolean shouldSetId, int srid)
            throws JSONException {
        MyPlace myPlace = new MyPlace();

        if (shouldSetId) {
            myPlace.setId(feature.getLong("id"));
        }
        myPlace.setCategoryId(feature.getLong("category_id"));

        JSONObject geomJSON = feature.getJSONObject("geometry");
        Geometry geom = GeoJSONReader.toGeometry(geomJSON);
        geom.setSRID(srid);
        myPlace.setGeometry(geom);

        JSONObject properties = feature.getJSONObject("properties");
        myPlace.setName(JSONHelper.getString(properties, "name"));

        // Optional fields
        myPlace.setAttentionText(JSONHelper.optString(properties, "attention_text"));
        myPlace.setDesc(JSONHelper.optString(properties, "place_desc"));
        myPlace.setLink(JSONHelper.optString(properties, "link"));
        myPlace.setImageUrl(JSONHelper.optString(properties, "image_url"));

        return myPlace;
    }

    @Override
    public SimpleFeatureCollection getFeatures(String layerId, Envelope bbox) throws ServiceException{
        int categoryId = parseId(layerId);
        SimpleFeatureCollection featureCollection = featureService.getFeatures(categoryId, bbox);
        return featureCollection != null ? featureCollection : new EmptyFeatureCollection(null);
    }

    @Override
    public WFSLayerOptions getWFSLayerOptions(String layerId) {
        int id = parseId(layerId);
        WFSLayerOptions wfsOpts = getLayer(id).getWFSLayerOptions();
        OskariLayer baseLayer = getBaseLayer();
        JSONObject baseOptions = baseLayer == null ? new JSONObject() : baseLayer.getOptions();
        wfsOpts.injectBaseLayerOptions(baseOptions);
        return wfsOpts;
    }

}
