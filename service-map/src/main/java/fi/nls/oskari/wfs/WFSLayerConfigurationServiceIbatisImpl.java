package fi.nls.oskari.wfs;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSParserConfig;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<WFSParserConfig> findWFSParserConfigs(String name) {
        List<WFSParserConfig> configs = queryForList(getNameSpace() + ".findParserConfigs", name);
        return configs;
    }

    public synchronized int insertTemplateModel(final Map<String,String> map) throws ServiceException {
        return  queryForObject(getNameSpace() + ".insertTemplateModel", map);
    }


    public synchronized int insertSLDStyle(final Map<String, Integer> map) throws ServiceException {
        return  queryForObject(getNameSpace() + ".insertSLDStyle", map);
    }

    public void update(final WFSLayerConfiguration layer) {
        try {
            getSqlMapClient().update(getNameSpace() + ".update", layer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        }
    }

    public synchronized List<Integer> insertSLDStyles(final int id, final List<Integer> lnks) {
        SqlMapClient client = null;
        List<Integer> ids = new ArrayList<Integer>();
        try {
            client = getSqlMapClient();
            client.startTransaction();
            // Remove old links for the wfs layer
            // remove wfs layer
            client.delete(getNameSpace() + ".removeLayerSLDStyles", id);
            for (Integer lnk : lnks) {
                Map<String, Integer> styleLink = new HashMap<String, Integer>();
                styleLink.put("id", id);
                styleLink.put("sld_style_id", lnk);
                Object rowId = client.insert(getNameSpace() + ".insertLayerSLDStyles", styleLink);
                ids.add(0);
            }


            client.commitTransaction();
            return ids;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert SLD styles", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }

    public synchronized int insert(final WFSLayerConfiguration layer) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.insert(getNameSpace() + ".insert", layer);
            Integer id = (Integer) client.queryForObject(getNameSpace()
                    + ".maxId");
            client.commitTransaction();
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }


    public void delete(final int id)  {
        long maplayer_id = Long.valueOf(id);
        final SqlMapSession session = openSession();
        try {
            session.startTransaction();
            // remove wfs layer
            session.delete(getNameSpace() + ".delete", maplayer_id);
            session.commitTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting wfs layer with maplayer_id:" + Long.toString(maplayer_id), e);
        } finally {
            endSession(session);
        }
    }
}
