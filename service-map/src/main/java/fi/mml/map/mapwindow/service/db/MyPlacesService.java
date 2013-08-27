package fi.mml.map.mapwindow.service.db;


import java.util.List;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.service.db.BaseService;

public interface MyPlacesService extends BaseService<MyPlaceCategory>{
  
    
    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList);
    public int updatePublisherName(final long id, final String uuid, final String name);
    public List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search);
    

}
