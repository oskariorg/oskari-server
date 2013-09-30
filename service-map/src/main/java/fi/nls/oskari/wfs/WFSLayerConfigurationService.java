package fi.nls.oskari.wfs;

import fi.nls.oskari.service.db.BaseService;

public interface WFSLayerConfigurationService extends BaseService<WFSLayerConfiguration>{
    
    public WFSLayerConfiguration findConfiguration(int id);

}
