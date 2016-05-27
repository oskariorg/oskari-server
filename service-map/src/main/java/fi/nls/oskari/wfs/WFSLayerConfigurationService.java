package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSParserConfig;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface WFSLayerConfigurationService extends BaseService<WFSLayerConfiguration>{
    
    public WFSLayerConfiguration findConfiguration(int id);
    public List<WFSSLDStyle> findWFSLayerStyles(final int layerId);
    public List<WFSParserConfig> findWFSParserConfigs(String name);
    public int insertTemplateModel(Map<String,String> map ) throws ServiceException;
    public List<Integer> insertSLDStyles(final int id, final List<Integer> lnks) throws ServiceException;
    public int insertSLDStyle(Map<String,Integer> map ) throws ServiceException;
}
