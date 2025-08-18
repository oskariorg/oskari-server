package org.oskari.map.myfeatures.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.util.PropertyUtil;

public class MyFeaturesServiceMybatisImpl extends MyFeaturesService {

    private static final Logger LOG = LogFactory.getLogger(MyFeaturesServiceMybatisImpl.class);

    private static final String INSERT_BATCH_SIZE = "myfeatures.mybatis.batch.size";
    private static final String NATIVE_SRS = "oskari.native.srs";
    private static final String FALLBACK_NATIVE_SRS = "EPSG:4326";

    private final int storageSrid;
    private final int batchSize;
    private final SqlSessionFactory factory;

    public MyFeaturesServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource(DatasourceHelper.getInstance().getOskariDataSourceName("myfeatures")));
    }

    public MyFeaturesServiceMybatisImpl(DataSource ds) {
        if (ds != null) {
            factory = initializeMyBatis(ds);
        } else {
            LOG.error("Couldn't get datasource for myfeatures");
            factory = null;
        }
        storageSrid = getStorageSrid();
        batchSize = PropertyUtil.getOptional(INSERT_BATCH_SIZE, 1000);
    }

    private static SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, MyFeaturesLayer.class, MyFeaturesFeature.class);
        MyBatisHelper.addMappers(configuration, MyFeaturesMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static int getStorageSrid() {
        String epsg = PropertyUtil.get(NATIVE_SRS, FALLBACK_NATIVE_SRS);
        return Integer.parseInt(epsg.substring(epsg.lastIndexOf(':') + 1));
    }

    @Override
    public MyFeaturesLayer getLayer(UUID layerId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findLayer(layerId);
        }
    }

    @Override
    public void createLayer(MyFeaturesLayer layer) {
        if (layer.getId() == null) {
            layer.setId(UUID.randomUUID());
        }        
        try (SqlSession session = factory.openSession()) {
            getMapper(session).insertLayer(layer);
            session.commit();
        }
    }

    @Override
    public void updateLayer(MyFeaturesLayer layer) {
        if (layer.getId() == null) {
            throw new IllegalArgumentException("Layer must have id when updating");
        }
        try (SqlSession session = factory.openSession()) {
            getMapper(session).updateLayer(layer);
            session.commit();
        }
    }

    @Override
    public void deleteLayer(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            getMapper(session).deleteLayer(layerId);
            session.commit();
        }
    }

    @Override
    public MyFeaturesFeature getFeature(UUID layerId, String fid) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeatureById(layerId, fid);
        }
    }

    @Override
    public void createFeature(UUID layerId, MyFeaturesFeature feature) {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.insertFeature(layerId, feature);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public void updateFeature(UUID layerId, MyFeaturesFeature feature) {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.updateFeature(layerId, feature);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public void deleteFeature(UUID layerId, String featureId) {
        layerId = Objects.requireNonNull(layerId);
        featureId = Objects.requireNonNull(featureId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.deleteFeature(layerId, featureId);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public List<MyFeaturesFeature> getFeatures(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeatures(layerId);
        }
    }
    
    @Override
    public List<MyFeaturesFeature> getFeaturesByBbox(UUID layerId, double minX, double minY, double maxX, double maxY) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeaturesByBbox(layerId, minX, minY, maxX, maxY);
        }
    }

    @Override
    public void createFeatures(UUID layerId, List<MyFeaturesFeature> features) {
        layerId = Objects.requireNonNull(layerId);
        if (features.isEmpty()) {
            return;
        }
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            int batchCount = 0;
            for (MyFeaturesFeature feature : features) {
                mapper.insertFeature(layerId, feature);
                batchCount++;
                // Flushes batch statements and clears local session cache
                if (batchCount == batchSize) {
                    session.flushStatements();
                    session.clearCache();
                    batchCount = 0;
                }
            }
            if (batchCount > 0) {
                session.flushStatements();
            }
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public List<MyFeaturesLayer> getLayersByOwnerUuid(String ownerId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findLayersByOwnerUuid(ownerId);
        }
    }

    @Override
    public void deleteLayersByOwnerUuid(String ownerUuid) {
        try (SqlSession session = factory.openSession()) {
            getMapper(session).deleteLayersByOwnerUuid(ownerUuid);
            session.commit();
        }
    }

    @Override
    public void swapAxisOrder(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.swapAxisOrder(layerId);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    private MyFeaturesMapper getMapper(SqlSession session) {
	    return session.getMapper(MyFeaturesMapper.class);
	}

}
