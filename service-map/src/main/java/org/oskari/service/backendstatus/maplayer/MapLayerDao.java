package org.oskari.service.backendstatus.maplayer;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * Data access to basic information of MapLayers
 */
public class MapLayerDao {

    private static final Class<MapLayerMapper> MAPPER = MapLayerMapper.class;

    private final SqlSessionFactory factory;

    public MapLayerDao() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public MapLayerDao(final DataSource ds) {
        this.factory = MyBatisHelper.initMyBatis(ds, MAPPER);
    }

    public List<MapLayer> findWMSMapLayers() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).selectWMSLayers();
        }
    }

    public List<MapLayer> findWFSMapLayers() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).selectWFSLayers();
        }
    }

}
