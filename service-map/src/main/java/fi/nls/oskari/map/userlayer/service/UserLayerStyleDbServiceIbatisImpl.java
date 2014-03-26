package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisStyleDbService;
import fi.nls.oskari.service.db.BaseIbatisService;

public class UserLayerStyleDbServiceIbatisImpl extends
		BaseIbatisService<UserLayerStyle> implements UserLayerStyleDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerStyleDbServiceIbatisImpl.class);

	
    @Override
	protected String getNameSpace() {
		return "UserLayerStyle";
	}

	    /*
	     * The purpose of this method is to allow many SqlMapConfig.xml files in a
	     * single portlet
	     */
   
    protected String getSqlMapLocation() {
	        return "META-INF/SqlMapConfig_UserLayer.xml";
	    }

	  
	    /**
         * insert user_layer_style table row
         * 
         * @param userlayerStyle
         */

        public long insertUserLayerStyleRow(final UserLayerStyle userlayerStyle) {
            

            final Long id = queryForObject(getNameSpace() + ".insertUserLayerStyleRow", userlayerStyle);
            userlayerStyle.setId(id);
            return id;
        }
	   
	 

      


}
