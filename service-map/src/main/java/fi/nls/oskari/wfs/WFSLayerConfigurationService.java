package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface WFSLayerConfigurationService extends BaseService<WFSLayerConfiguration>{
    
    public WFSLayerConfiguration findConfiguration(int id);
    public List<WFSSLDStyle> findWFSLayerStyles(final int layerId);
    public int updateSchemaInfo(final long id, final String schema, final String status);
}
