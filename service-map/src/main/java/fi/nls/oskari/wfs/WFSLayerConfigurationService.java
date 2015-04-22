package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;
import java.util.Map;

public interface WFSLayerConfigurationService extends BaseService<WFSLayerConfiguration>{
    
    public WFSLayerConfiguration findConfiguration(int id);
    public List<WFSSLDStyle> findWFSLayerStyles(final int layerId);
    public int insertTemplateModel(Map<String,String> map ) throws ServiceException;
}
