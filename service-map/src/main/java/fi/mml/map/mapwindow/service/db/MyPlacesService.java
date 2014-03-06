package fi.mml.map.mapwindow.service.db;


import java.util.List;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.db.BaseService;
import org.json.JSONObject;

public interface MyPlacesService extends BaseService<MyPlaceCategory>{

    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList);
    public int updatePublisherName(final long id, final String uuid, final String name);
    public List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search);

    public JSONObject getCategoryAsWmsLayerJSON(final MyPlaceCategory mpLayer,
                                             final String lang, final boolean useDirectURL,
                                              final String uuid, final boolean modifyURLs);
    

}
