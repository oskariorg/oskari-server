package fi.nls.oskari.wfs;

import java.util.List;

import fi.nls.oskari.service.db.BaseIbatisService;

public class WFSLayerConfigurationServiceIbatisImpl extends BaseIbatisService<WFSLayerConfiguration> implements WFSLayerConfigurationService {

    @Override
    protected String getNameSpace() {
        return "WFSLayerConfiguration";
    }

    public WFSLayerConfiguration findConfiguration(int id) {
    	WFSLayerConfiguration conf = (WFSLayerConfiguration) queryForObject(getNameSpace() + ".findLayer", id);
    	if(conf == null)
    		return null;
    	List<WFSSLDStyle> styles = queryForList(getNameSpace() + ".findStyles", id);
    	conf.setSLDStyles(styles);
    	return conf;
    }
    
}
