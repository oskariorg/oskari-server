package fi.nls.oskari.wfs;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;

public class WFSLayerConfigurationServiceIbatisImpl extends BaseIbatisService<WFSLayerConfiguration> implements WFSLayerConfigurationService {

    private final static Logger log = LogFactory.getLogger(WFSLayerConfigurationServiceIbatisImpl.class);

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

    /**
     * Updates schema information
     *
     * @param id
     * @param schema
     * @param status
     */
    public int updateSchemaInfo(final long id, final String schema, final String status) {
        final Map<String, Object> data = new HashMap<String,Object>();
        data.put("id", id);
        data.put("schema", schema);
        data.put("status", status);

        try {
            if(status.equals("ok")) {
                return getSqlMapClient().update(
                        getNameSpace() + ".updateSchemaInfo", data);
            } else {
                return getSqlMapClient().update(
                        getNameSpace() + ".updateFailSchemaInfo", data);
            }
        } catch (SQLException e) {
            log.error(e, "Failed to update", data);
        }
        return 0;
    }
}
