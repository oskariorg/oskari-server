package org.oskari.spatineo.monitor.maplayer;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Data access to basic information of MapLayers
 */
public class MapLayerDao {

    private final SqlSessionFactory factory;

    public MapLayerDao(final SqlSessionFactory factory) {
        this.factory = factory;
    }

    public List<MapLayer> findWMSMapLayers() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MapLayerMapper.class).selectWMSLayers();
        }
    }

    public List<MapLayer> findWFSMapLayers() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MapLayerMapper.class).selectWFSLayers();
        }
    }

}
