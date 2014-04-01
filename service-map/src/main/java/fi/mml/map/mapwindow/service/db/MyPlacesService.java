package fi.mml.map.mapwindow.service.db;


import java.util.List;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.db.BaseService;
import org.json.JSONObject;

public interface MyPlacesService extends BaseService<MyPlaceCategory>{

    public static final String RESOURCE_TYPE_MYPLACES = "myplaces";
    public final static String PERMISSION_TYPE_DRAW = "DRAW";

    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList);
    public int updatePublisherName(final long id, final String uuid, final String name);
    public List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search);

    public JSONObject getCategoryAsWmsLayerJSON(final MyPlaceCategory mpLayer,
                                             final String lang, final boolean useDirectURL,
                                              final String uuid, final boolean modifyURLs);

    public boolean canInsert(final User user, final long categoryId);
    public boolean canModifyPlace(final User user, final long placeId);
    public boolean canModifyCategory(final User user, final long categoryId);
    public boolean canModifyCategory(final User user, final String layerId);
    public Resource getResource(final long categoryId);
    public Resource getResource(final String myplacesLayerId);

}
