package org.oskari.map.myfeatures.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.util.PropertyUtil;

@Oskari
public class MyFeaturesServiceMybatisImpl extends MyFeaturesService {

    private static final Logger LOG = LogFactory.getLogger(MyFeaturesServiceMybatisImpl.class);

    private static final String INSERT_BATCH_SIZE = "myfeatures.mybatis.batch.size";
    private static final String NATIVE_SRS = "oskari.native.srs";
    private static final String FALLBACK_NATIVE_SRS = "EPSG:3857";

    private final CoordinateReferenceSystem nativeCRS;
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
        nativeCRS = createNativeCRS();
        batchSize = PropertyUtil.getOptional(INSERT_BATCH_SIZE, 1000);
    }

    private static SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, MyFeaturesLayer.class, MyFeaturesFeature.class);
        MyBatisHelper.addMappers(configuration, MyFeaturesMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public CoordinateReferenceSystem getNativeCRS() {
        return nativeCRS;
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
            MyFeaturesMapper mapper = getMapper(session);

            OffsetDateTime now = mapper.now();
            layer.setCreated(now);
            layer.setUpdated(now);

            mapper.insertLayer(layer);

            session.commit();
        }
    }

    @Override
    public void updateLayer(MyFeaturesLayer layer) {
        if (layer.getId() == null) {
            throw new IllegalArgumentException("Layer must have id when updating");
        }
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            OffsetDateTime now = mapper.now();
            layer.setUpdated(now);

            mapper.updateLayer(layer);

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
    public MyFeaturesFeature getFeature(UUID layerId, long featureId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeatureById(featureId);
        }
    }

    @Override
    public void createFeature(UUID layerId, MyFeaturesFeature feature) {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            OffsetDateTime now = mapper.now();
            feature.setCreated(now);
            feature.setUpdated(now);

            mapper.insertFeature(layerId, feature);
            mapper.refreshLayerMetadata(layerId);

            session.commit();
        }
    }

    @Override
    public void updateFeature(UUID layerId, MyFeaturesFeature feature) {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            OffsetDateTime now = mapper.now();
            feature.setUpdated(now);

            mapper.updateFeature(feature);
            mapper.refreshLayerMetadata(layerId);

            session.commit();
        }
    }

    @Override
    public void deleteFeature(UUID layerId, long featureId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.deleteFeature(featureId);
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
            OffsetDateTime now = mapper.now();
            int batchCount = 0;
            for (MyFeaturesFeature feature : features) {
                feature.setCreated(now);
                feature.setUpdated(now);
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
            OffsetDateTime now = mapper.now();
            mapper.swapAxisOrder(layerId, now);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public void deleteFeaturesByLayerId(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.deleteFeaturesByLayerId(layerId);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    private static CoordinateReferenceSystem createNativeCRS() {
        try {
            String epsg = PropertyUtil.get(NATIVE_SRS, FALLBACK_NATIVE_SRS);
            return CRS.decode(epsg, true);
        } catch (Exception e) {
            LOG.error(e, "Failed to create nativeCRS!");
            return null;
        }
    }

    private MyFeaturesMapper getMapper(SqlSession session) {
	    return session.getMapper(MyFeaturesMapper.class);
	}

}
