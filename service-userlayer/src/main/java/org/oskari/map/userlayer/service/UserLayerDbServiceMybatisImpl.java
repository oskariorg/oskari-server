package org.oskari.map.userlayer.service;


import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.List;

import static fi.nls.oskari.map.geometry.ProjectionHelper.getSRID;
import static fi.nls.oskari.map.geometry.WKTHelper.parseWKT;

@Oskari
public class UserLayerDbServiceMybatisImpl extends UserLayerDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerDbServiceMybatisImpl.class);
    private static final String USERLAYER_MYBATIS_BATCH_SIZE = "userlayer.mybatis.batch.size";
    private static final String NATIVE_SRS = "oskari.native.srs";
    final int batchSize = PropertyUtil.getOptional(USERLAYER_MYBATIS_BATCH_SIZE, 1000);
    private final int srid;
    private final Cache<UserLayer> cache;
    private SqlSessionFactory factory = null;

    private String GEOM_ATTRIBUTE = "geometry";
    private static final GeoJSONWriter geojsonWriter = new GeoJSONWriter();

    public UserLayerDbServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("userlayer"));

        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            log.error("Couldn't get datasource for userlayer");
        }
        cache = CacheManager.getCache(getClass().getName());
        String epsg = PropertyUtil.get(NATIVE_SRS, "EPSG:4326");
        srid = Integer.parseInt(epsg.substring(epsg.indexOf(':') + 1));
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, UserLayer.class, UserLayerData.class);
        MyBatisHelper.addMappers(configuration, UserLayerMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public int insertUserLayerAndData(final UserLayer userLayer, final List<UserLayerData> userLayerDataList) throws UserLayerException {
        validateUserLayer(userLayer);
        try (SqlSession session = factory.openSession(ExecutorType.BATCH)) {
            int count = 0;
            final UserLayerMapper mapper = getMapper(session);
            mapper.insertUserLayer(userLayer);
            session.flushStatements();
            long userLayerId = userLayer.getId();
            final UserLayer inserted = mapper.findUserLayer(userLayerId);
            userLayer.setCreated(inserted.getCreated());
            log.debug("got layer id", userLayerId);
            for (UserLayerData userLayerData : userLayerDataList) {
                mapper.insertUserLayerData(userLayerData, userLayerId, srid);
                count++;
                // Flushes batch statements and clears local session cache
                if (count % batchSize == 0) {
                    session.flushStatements();
                    session.clearCache();
                }
            }
            session.flushStatements();
            if (count == 0) throw new UserLayerException("UserLayer doesn't contain features", UserLayerException.ErrorType.NO_FEATURES);
            log.debug("stored:", count, "rows");
            session.commit();
            cache(userLayer);
            return count;
        } catch (Exception e) {
            log.error(e, "Rolling back, failed to insert userlayer with id:", +userLayer.getId());
            if(e instanceof UserLayerException){
                throw e; // no features
            }
            throw new UserLayerException("Failed to store features to database", UserLayerException.ErrorType.STORE);
        }
    }

    /**
     * update UserLayer table row field mapping
     *
     * @param userLayer
     */
    public int updateUserLayer(final UserLayer userLayer) throws UserLayerException {
        validateUserLayer(userLayer);
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            int result = mapper.updateUserLayer(userLayer);
            session.commit();
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update userLayer", userLayer);
            throw new UserLayerException("Failed to update userlayer", UserLayerException.ErrorType.STORE);
        } finally {
            cache.remove(Long.toString(userLayer.getId()));
        }
    }

    private UserLayer getFromCache(long id) {
        return cache.get(Long.toString(id));
    }

    private UserLayer cache(UserLayer layer) {
        if (layer != null) {
            cache.put(Long.toString(layer.getId()), layer);
        }
        return layer;
    }
    private void validateUserLayer (UserLayer layer) throws UserLayerException {
        if (layer.getNames().isEmpty()) {
            throw new UserLayerException("Couldn't find name for userlayer", UserLayerException.ErrorType.NO_NAME);
        }
    }
    /**
     * Get UserLayer row  by id
     *
     * @param id
     * @return userLayer object
     */
    public UserLayer getUserLayerById(long id) {
        UserLayer layer = getFromCache(id);
        if (layer != null) {
            return layer;
        }
        try (SqlSession session = factory.openSession()) {
            layer = getMapper(session).findUserLayer(id);
            return cache(layer);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with id:", id);
            return null;
        }
    }


    /**
     * Get UserLayer rows of one user by uuid
     *
     * @param uuid user uuid
     * @return List of userLayer objects
     */
    public List<UserLayer> getUserLayerByUuid(String uuid) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findUserLayerByUuid(uuid);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with uuid:", uuid);
            return null;
        }
    }

    public void deleteUserLayerById(final long id) throws ServiceException {
        final UserLayer userLayer = getUserLayerById(id);
        deleteUserLayer(userLayer);
        cache.remove(Long.toString(id));
    }

    public void deleteUserLayer(final UserLayer userLayer) throws ServiceException {
        if (userLayer == null) {
            throw new ServiceException("Tried to delete userLayer with <null> param");
        }
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            mapper.deleteUserLayerDataByLayerId(userLayer.getId());
            mapper.deleteUserLayer(userLayer.getId());
            session.commit();
            cache.remove(Long.toString(userLayer.getId()));
        } catch (Exception e) {
            log.error(e, "Error deleting userLayer with id:", userLayer.getId());
            throw new ServiceException("Error deleting userLayer with id:" + userLayer.getId(), e);
        }
    }

    public void deleteUserLayersByUuid(String uuid) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            List<UserLayer> userLayers = mapper.findUserLayerByUuid(uuid);
            if (userLayers.isEmpty()) {
                log.info("Couldn't find any userlayer with uuid:", uuid);
                return;
            }
            for (UserLayer layer : userLayers) {
                deleteUserLayer(layer);
            }
            session.commit();
        } catch (Exception e) {
            log.error(e, "Error deleting user related userlayer content with uuid:", uuid);
            throw new ServiceException("Error deleting user related userlayer content with uuid:" + uuid, e);
        }
    }

    /**
     * Updates a userLayer publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            int result = mapper.updatePublisherName(id, uuid, name);
            session.commit();
            // update data in cache
            UserLayer layer = getFromCache(id);
            if (layer != null && result > 0) {
                cache.remove(Long.toString(id));
            }
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update publisher name:", name, "id:", id, "uuid", uuid);
            return 0;
        }
    }

    /**
     * update UserLayerData table row field mapping
     *
     * @param userLayerData
     */
    public int updateUserLayerData(final UserLayerData userLayerData) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).updateUserLayerData(userLayerData);
        } catch (Exception e) {
            log.error(e, "Failed to update userlayerdata col mapping", userLayerData);
            return 0;
        }
    }
	
	public String getUserLayerExtent(long id) {
	    try (SqlSession session = factory.openSession()) {
             return getMapper(session).getUserLayerBbox(id);
         } catch (Exception e) {
             log.error(e, "Failed to get userlayer bbox with id:", id);
             return "";
         }
     }

	private UserLayerMapper getMapper(SqlSession session) {
	    return session.getMapper(UserLayerMapper.class);
	}

    @Override
    public SimpleFeatureCollection getFeatures(int layerId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            log.debug("getFeatures by bbox: ", bbox);

            final UserLayerMapper mapper = getMapper(session);
            String nativeSrsName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
            int nativeSrid = getSRID(nativeSrsName);
            List<UserLayerData> features = mapper.findAllByLooseBBOX(layerId, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), nativeSrid);
            return this.toSimpleFeatureCollection(features);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to get features by bounding box ", bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
            throw new ServiceException(e.getMessage());
        }
    }

    private SimpleFeatureCollection toSimpleFeatureCollection(List<UserLayerData> features) throws ServiceException {
        try {
            if (features == null || features.isEmpty()) {
                return new EmptyFeatureCollection(null);
            }

            DefaultFeatureCollection collection = new DefaultFeatureCollection();
            for (UserLayerData feature: features) {
                if (feature.getWkt() != null) {
                    collection.add(toSimpleFeature(feature));
                }
            }

            return collection;
        } catch (Exception e) {
            log.warn(e, "Failed to create SimpleFeatureCollection");
            throw new ServiceException("Failed to create SimpleFeatureCollection");
        }
    }

    private SimpleFeature toSimpleFeature(UserLayerData feature) {
        Geometry geom = parseWKT(feature.getWkt());
        SimpleFeatureTypeBuilder featureTypeBuilder = getFeatureTypeBuilder(geom);
        SimpleFeatureType simpleFeatureType = featureTypeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(simpleFeatureType);
        featureBuilder.set(GEOM_ATTRIBUTE, geom);
        featureBuilder.set("geometry_name", GEOM_ATTRIBUTE);
        featureBuilder.set("id", feature.getId());
        featureBuilder.set("user_layer_id", feature.getUser_layer_id());
        featureBuilder.set("uuid", feature.getUuid());
        featureBuilder.set("feature_id", feature.getFeature_id());
        featureBuilder.set("property_json", feature.getProperty_json());
        featureBuilder.set("created", feature.getCreated());
        featureBuilder.set("updated", feature.getUpdated());

        return featureBuilder.buildFeature(Long.valueOf(feature.getId()).toString());
    }

    private SimpleFeatureTypeBuilder getFeatureTypeBuilder(Geometry geometry) {
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("temp");
        featureTypeBuilder.setNamespaceURI("http://oskari.org");
        featureTypeBuilder.setDefaultGeometry(GEOM_ATTRIBUTE);

        if (geometry != null) {
            setFeatureTypeBuilderGeometry(featureTypeBuilder, geometry);
        }

        featureTypeBuilder.add("geometry_name", String.class);
        featureTypeBuilder.add("id", Long.class);
        featureTypeBuilder.add("user_layer_id", String.class);
        featureTypeBuilder.add("uuid", String.class);
        featureTypeBuilder.add("feature_id", String.class);
        featureTypeBuilder.add("property_json", String.class);
        featureTypeBuilder.add("created", OffsetDateTime.class);
        featureTypeBuilder.add("updated", OffsetDateTime.class);

        return featureTypeBuilder;

    }
    private void setFeatureTypeBuilderGeometry(SimpleFeatureTypeBuilder featureTypeBuilder, Geometry geometry) {
        if (geometry != null) {
            featureTypeBuilder.setDefaultGeometry(GEOM_ATTRIBUTE);
            switch (geometry.getGeometryType()) {
                case GeoJSON.POINT:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, Point.class);
                    break;
                case GeoJSON.LINESTRING:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, LineString.class);
                    break;
                case GeoJSON.POLYGON:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, Polygon.class);
                    break;
                case GeoJSON.MULTI_POINT:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, MultiPoint.class);
                    break;
                case GeoJSON.MULTI_LINESTRING:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, MultiLineString.class);
                    break;
                case GeoJSON.MULTI_POLYGON:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, MultiPolygon.class);
                    break;
                case GeoJSON.GEOMETRY_COLLECTION:
                    featureTypeBuilder.add(GEOM_ATTRIBUTE, GeometryCollection.class);
                    break;
            }
        }


    }
}
