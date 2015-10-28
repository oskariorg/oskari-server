package fi.nls.oskari.map.userlayer.service;


import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.service.db.BaseService;

public interface UserLayerStyleDbService extends BaseService<UserLayerStyle> {
        
       
      
        public long insertUserLayerStyleRow(final UserLayerStyle userlayerStyle);
     
        

}
