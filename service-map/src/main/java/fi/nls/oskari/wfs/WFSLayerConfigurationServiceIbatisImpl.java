package fi.nls.oskari.wfs;

import java.util.List;

import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;

public class WFSLayerConfigurationServiceIbatisImpl extends BaseIbatisService<WFSLayerConfiguration> implements WFSLayerConfigurationService {

    @Override
    protected String getNameSpace() {
        return "WFSLayerConfiguration";
    }

    public WFSLayerConfiguration findConfiguration(final int id) {
    	WFSLayerConfiguration conf = queryForObject(getNameSpace() + ".findLayer", id);
    	if(conf == null) {
            return null;
        }
    	final List<WFSSLDStyle> styles = findWFSLayerStyles(id);
    	conf.setSLDStyles(styles);
    	return conf;
    }

    public List<WFSSLDStyle> findWFSLayerStyles(final int layerId) {
        List<WFSSLDStyle> styles = queryForList(getNameSpace() + ".findStylesForLayer", layerId);
        return styles;
    }


}
