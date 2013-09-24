package fi.mml.map.mapwindow.service.db;

import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

public class MyPlacesServiceIbatisImpl extends BaseIbatisService<MyPlaceCategory>
        implements MyPlacesService {

    private final static Logger log = LogFactory.getLogger(
            MyPlacesServiceIbatisImpl.class);

    @Override
    protected String getNameSpace() {
        return "MyPlace";
    }

    private SqlMapClient client = null;

    /**
     * Returns SQLmap
     * 
     * @return
     */
    @Override
    protected SqlMapClient getSqlMapClient() {
        if (client != null) {
            return client;
        }

        Reader reader = null;
        try {
            String sqlMapLocation = getSqlMapLocation();
            reader = Resources.getResourceAsReader(sqlMapLocation);
            client = SqlMapClientBuilder.buildSqlMapClient(reader);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SQL client", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return "META-INF/SqlMapConfig_MyPlace.xml";
    }

    /**
     * Updates a MyPlace publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {

        final Map<String, Object> data = new HashMap<String,Object>();
        data.put("publisher_name", name);
        data.put("uuid", uuid);
        data.put("id", id);
        try {
            return getSqlMapClient().update(
                    getNameSpace() + ".updatePublisherName", data);
        } catch (SQLException e) {
            log.error(e, "Failed to update publisher name", data);
        }
        return 0;
    }

   
    @Override
    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList) {
        return queryForList(getNameSpace() + ".findByIds", idList);
    }
    
    @Override
    public List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("searchKey", search + ":*");
        return queryForList(getNameSpace() + ".freeFind", data);
    }

}
