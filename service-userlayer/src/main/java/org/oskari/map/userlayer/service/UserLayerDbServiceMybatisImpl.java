package org.oskari.map.userlayer.service;


import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONArrayMybatisTypeHandler;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Oskari
public class UserLayerDbServiceMybatisImpl extends UserLayerDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerDbServiceMybatisImpl.class);
    private static final String USERLAYER_MYBATIS_BATCH_SIZE = "userlayer.mybatis.batch.size";
    private static final String NATIVE_SRS = "oskari.native.srs";
    final int batchSize = PropertyUtil.getOptional(USERLAYER_MYBATIS_BATCH_SIZE, 1000);
    private final int srid;
    private final Cache<UserLayer> cache;
    private SqlSessionFactory factory = null;

    private static final WKTReader wktReader = new WKTReader();
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
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(UserLayer.class);
        configuration.getTypeAliasRegistry().registerAlias(UserLayerData.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(JSONArrayMybatisTypeHandler.class);
        configuration.addMapper(UserLayerMapper.class);
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
    public JSONObject getFeatures(int layerId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            log.debug("getFeatures by bbox: ", bbox);

            final UserLayerMapper mapper = getMapper(session);
            String nativeSrsName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
            String targetSrsName = crs.getIdentifiers()
                .stream()
                .filter(identifier -> identifier.getCodeSpace().equals("EPSG"))
                .map(identifier -> identifier.getCodeSpace() + ":" + identifier.getCode())
                .findFirst()
                .orElse(null);

            int nativeSrid = getSRID(nativeSrsName);
            List<UserLayerData> features = mapper.findAllByBBOX(layerId, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), nativeSrid);

            JSONObject featureCollection = this.toGeoJSONFeatureCollection(features, targetSrsName != null ? targetSrsName : nativeSrsName);
            return featureCollection;
        } catch (Exception e) {
            log.warn(e, "Exception when trying to get features by bounding box ", bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
            throw new ServiceException(e.getMessage());
        }
    }


    private JSONObject toGeoJSONFeatureCollection(List<UserLayerData> features, String targetSRSName) throws ServiceException {
        if (features == null || features.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
            json.put("crs", createCRSObject(targetSRSName));

            JSONArray jsonFeatures = new JSONArray(features.stream().map(feature -> this.toGeoJSONFeature(feature, targetSRSName)).collect(Collectors.toList()));
            json.put(GeoJSON.FEATURES, jsonFeatures);

        } catch(JSONException ex) {
            log.warn("Failed to create GeoJSON FeatureCollection");
            throw new ServiceException("Failed to create GeoJSON FeatureCollection");
        }
        return json;
    }

     private JSONObject toGeoJSONFeature(UserLayerData feature, String targetSRSName) {
        JSONObject jsonFeature = new JSONObject();
        JSONObject properties = new JSONObject();
        try {
            jsonFeature.put("id", feature.getId());
            jsonFeature.put("geometry_name", GeoJSON.GEOMETRY);
            jsonFeature.put(GeoJSON.TYPE, GeoJSON.FEATURE);

            String sourceSRSName = "EPSG:" + feature.getDatabaseSRID();
            Geometry transformed = wktToGeometry(feature.getWkt(), sourceSRSName, targetSRSName);
            JSONObject geoJsonGeometry = geojsonWriter.writeGeometry(transformed);
            jsonFeature.put(GeoJSON.GEOMETRY, geoJsonGeometry);

            properties.put("id", feature.getId());
            properties.put("user_layer_id", feature.getUser_layer_id());
            properties.put("uuid", feature.getUuid());
            properties.put("feature_id", feature.getFeature_id());
            properties.put("property_json", feature.getProperty_json());
            properties.put("created", feature.getCreated());
            properties.put("updated", feature.getUpdated());
            jsonFeature.put("properties", properties);

        } catch(JSONException ex) {
            log.warn("Failed to convert UserLayerData to GeoJSONFeature");
        }

        return jsonFeature;
     }

    /** //////
     * TODO: move to some helper class - common with myplaces
     * //////*/
     private JSONObject createCRSObject(String srsName) {
        JSONObject crs = new JSONObject();
        try {
            crs.put("type", "name");
            JSONObject crsProperties = new JSONObject();
            crsProperties.put("name", srsName);
            crs.put(GeoJSON.PROPERTIES, crsProperties);

        } catch(JSONException e) {
            log.warn("Failed to create crs object.");
            return null;
        }

        return crs;
     }

     private int getSRID(String srsName) {
        String srid = srsName.substring(srsName.indexOf(':') + 1);
        return Integer.parseInt(srid);
    }
    private Geometry transformGeometry(Geometry geometry, String sourceSRSName, String targetSRSName) {
        try {
            CoordinateReferenceSystem targetCRS, sourceCRS;
            MathTransform transform;

            try {
                targetCRS = CRS.decode(targetSRSName);
                sourceCRS = CRS.decode(sourceSRSName);
                transform = CRS.findMathTransform(sourceCRS, targetCRS);
            } catch (Exception e) {
                throw new UserLayerException("Failed to get geometry transform from " + sourceSRSName + " to " + targetSRSName);
            }
            Geometry transformed = JTS.transform(geometry, transform);
            transformed.setSRID(getSRID(targetSRSName));
            return transformed;

        } catch(Exception e) {
            log.warn(e, "Exception transforming geometry");
        }
        return null;
    }
     private Geometry wktToGeometry(String wkt, String sourceSRSName, String targetSRSName) {
        try {
            if (sourceSRSName.equals(targetSRSName)) {
                return wktReader.read(wkt);
            }

            Geometry geometry = wktReader.read(wkt);
            Geometry transformed = transformGeometry(geometry, sourceSRSName, targetSRSName);
            return transformed;
        } catch(ParseException e) {
            log.warn("Failed to parse geometry from wkt " + wkt);
            return null;
        }
     }
    /* ////////////// */
}
