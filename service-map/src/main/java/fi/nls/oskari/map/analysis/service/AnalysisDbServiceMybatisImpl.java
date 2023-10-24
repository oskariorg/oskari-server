package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.nls.oskari.map.geometry.ProjectionHelper.getSRID;
import static fi.nls.oskari.map.geometry.WKTHelper.parseWKT;

@Oskari
public class AnalysisDbServiceMybatisImpl extends AnalysisDbService {

    protected static final String DATASOURCE_ANALYSIS = "analysis";

    private static final Logger log = LogFactory.getLogger(AnalysisDbServiceMybatisImpl.class);
    private final Cache<Analysis> cache;

    private SqlSessionFactory factory = null;

    private static final GeoJSONWriter geojsonWriter = new GeoJSONWriter();

    public AnalysisDbServiceMybatisImpl() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName(DATASOURCE_ANALYSIS));
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            log.error("Couldn't get datasource for analysisservice");
        }
        cache = CacheManager.getCache(getClass().getName());
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(Analysis.class);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(AnalysisMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }


    private Analysis getFromCache(long id) {
        return cache.get(Long.toString(id));
    }

    private Analysis cache(Analysis layer) {
        if (layer != null) {
            cache.put(Long.toString(layer.getId()), layer);
        }
        return layer;
    }

    /**
     * insert Analysis table row
     *
     * @param analysis
     */

    public long insertAnalysisRow(final Analysis analysis) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Insert analyse row:", analysis);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.insertAnalysisRow(analysis);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add analysis: ", analysis);
        } finally {
            session.close();
        }
        log.debug("Got analyse id:", analysis.getId());
        return analysis.getId();
    }

    /**
     * update Analysis table row field mapping
     *
     * @param analysis
     */
    public long updateAnalysisCols(final Analysis analysis) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating analysis columns:", analysis);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.updateAnalysisCols(analysis);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update analysis columns mapping: ", analysis);
        } finally {
            session.close();
        }
        return analysis.getId();
    }

    /**
     * Get Analysis row  by id
     *
     * @param id
     * @return analysis object
     */
    public Analysis getAnalysisById(long id) {
        Analysis layer = getFromCache(id);
        if (layer != null) {
            return layer;
        }
        try (SqlSession session = factory.openSession()) {
            log.debug("Finding analysis by id:", id);
            AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            Analysis analysis =  mapper.getAnalysisById(id);
            if (analysis == null) {
                log.debug("Could not find analysis by id:", id);
                return null;
            }
            log.debug("Found analysis: ", analysis);
            return cache(analysis);
        }
    }

    public List<Analysis> getAnalysisById(List<Long> idList) {
        if(idList == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Analysis> analysisList = null;
        try {
            log.debug("Finding analysis matching: ", idList);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            analysisList =  mapper.getAnalysisByIdList(idList);
            if(analysisList == null) {
                analysisList = Collections.emptyList();
            }
            log.debug("Found analysis: ", analysisList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying get analysis by id: ", idList);
        } finally {
            session.close();
        }
        return analysisList;
    }

    /**
     * Get Analysis rows of one user by uuid
     *
     * @param uid user uuid
     * @return List of analysis objects
     */
    public List<Analysis> getAnalysisByUid(String uid) {
        if(uid == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Analysis> analysisList = null;
        try {
            log.debug("Finding analysis matching uid: ", uid);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            analysisList =  mapper.getAnalysisByUid(uid);
            if(analysisList == null) {
                analysisList = Collections.emptyList();
            }
            log.debug("Found analysis: ", analysisList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying get analysis by Uid: ", uid);
        } finally {
            session.close();
        }
        return analysisList;
    }

    @Override
    public void deleteAnalysisByUid(final String uid) throws ServiceException {
        final List<Analysis> userLayers = getAnalysisByUid(uid);
        for (Analysis userLayer: userLayers) {
            deleteAnalysis(userLayer);
        }
    }
    /**
     * Get Analysis data  by uuid and analysis id
     *
     * @param id analysis id
     * @param uuid user uuid
     * @param select_items select body string in select statement
     * @return List of analysis data rows
     */
    public List<HashMap<String,Object>> getAnalysisDataByIdUid(long id, String uuid, String select_items) {
        try (SqlSession session = factory.openSession()) {
            log.debug("Finding analysis data for id:", id, " uid:", uuid);
            AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);
            params.put("uuid", uuid);
            params.put("select_items", select_items);
            List<HashMap<String,Object>> analysisdataList = mapper.getAnalysisDataByIdUid(params);
            if (analysisdataList == null) {
                return Collections.emptyList();
            }
            log.debug("Found analysis data: ", analysisdataList);
            return analysisdataList;
        }
    }

    public void deleteAnalysisById(final long id) throws ServiceException {
        deleteAnalysis(getAnalysisById(id));
    }


    public void deleteAnalysis(final Analysis analysis) throws ServiceException {
        if(analysis == null) {
            throw new ServiceException("Tried to delete analysis with <null> param");
        }
        final SqlSession session = factory.openSession();
        try {
            log.debug("Deleting analysis: ", analysis);
            //TODO final Resource res = permissionsService.getResource(AnalysisLayer.TYPE, "analysis+" + analysis.getId());
            //permissionsService.deleteResource(res);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            mapper.deleteAnalysisById(analysis.getId());
            mapper.deleteAnalysisDataById(analysis.getId());
            session.commit();
            cache.remove(Long.toString(analysis.getId()));
        } catch (Exception e) {
            session.rollback();
            log.warn(e, "Exception when trying delete analysis by id: ", analysis);
        } finally {
            session.close();
        }
    }

    public void mergeAnalysis(final Analysis analysis, final List<Long> ids) throws ServiceException {
        if(analysis == null) {
            throw new ServiceException("Tried to merge analysis with <null> param");
        }
        final SqlSession session = factory.openSession();
        if (ids.size() > 1) {
            try {
                log.debug("Merging analysis: ", analysis);
                //TODO final Resource res = permissionsService.getResource(AnalysisLayer.TYPE, "analysis+" + analysis.getId());
                //permissionsService.deleteResource(res);
                final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
                for (long id : ids) {
                    analysis.setOld_id(id);
                    mapper.mergeAnalysisData(analysis);
                }
                for (long id : ids) {
                    Analysis analysis_old = mapper.getAnalysisById(id);
                    mapper.deleteAnalysisById(id);
                    cache.remove(Long.toString(analysis_old.getId()));
                }
                session.commit();
            } catch (Exception e) {
                log.warn(e, "Error merging analysis data with ids: ", ids);
            } finally {
                session.close();
            }
        }
    }

    /**
     * Updates a analysis publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public long updatePublisherName(final long id, final String uuid, final String name) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating publisher name with id: ", id);
            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            final Map<String, Object> params = new HashMap<>();
            params.put("publisher_name", name);
            params.put("uuid", uuid);
            params.put("id", id);
            mapper.updatePublisherName(params);
            session.commit();
            // update data in cache
            Analysis layer = getFromCache(id);
            if (layer != null) {
                layer.setPublisher_name(name);
            }
        } catch (Exception e) {
            log.warn(e, "Failed to update publisher name");
        } finally {
            session.close();
        }
        return id;
    }

    @Override
    public JSONObject getFeatures(int layerId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            log.debug("getFeatures by bbox: ", bbox);

            final AnalysisMapper mapper = session.getMapper(AnalysisMapper.class);
            String nativeSrsName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
            String targetSrsName = crs.getIdentifiers()
                .stream()
                .filter(identifier -> identifier.getCodeSpace().equals("EPSG"))
                .map(identifier -> identifier.getCodeSpace() + ":" + identifier.getCode())
                .findFirst()
                .orElse(null);

            int nativeSrid = getSRID(nativeSrsName);
            List<AnalysisData> features = mapper.findAllByBBOX(layerId, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), nativeSrid);

            JSONObject featureCollection = this.toGeoJSONFeatureCollection(features, targetSrsName != null ? targetSrsName : nativeSrsName);
            return featureCollection;
        } catch (Exception e) {
            log.warn(e, "Exception when trying to get features by bounding box ", bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
            throw new ServiceException(e.getMessage());
        }
    }

    private JSONObject toGeoJSONFeatureCollection(List<AnalysisData> features, String targetSRSName) throws ServiceException {
        if (features == null || features.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
            json.put("crs", geojsonWriter.writeCRSObject(targetSRSName));

            JSONArray jsonFeatures = new JSONArray(features.stream().map(feature -> this.toGeoJSONFeature(feature, targetSRSName)).collect(Collectors.toList()));
            json.put(GeoJSON.FEATURES, jsonFeatures);

        } catch(JSONException ex) {
            log.warn("Failed to create GeoJSON FeatureCollection");
            throw new ServiceException("Failed to create GeoJSON FeatureCollection");
        }
        return json;
    }

     private JSONObject toGeoJSONFeature(AnalysisData feature, String targetSRSName) {
        JSONObject jsonFeature = new JSONObject();
        JSONObject properties = new JSONObject();
        try {
            jsonFeature.put("id", feature.getId());
            jsonFeature.put("geometry_name", GeoJSON.GEOMETRY);
            jsonFeature.put(GeoJSON.TYPE, GeoJSON.FEATURE);

            String sourceSRSName = "EPSG:" + feature.getDatabaseSRID();
            Geometry transformed = WKTHelper.transform(parseWKT(feature.getWkt()), sourceSRSName, targetSRSName);
            JSONObject geoJsonGeometry = geojsonWriter.writeGeometry(transformed);
            jsonFeature.put(GeoJSON.GEOMETRY, geoJsonGeometry);

            properties.put("id", feature.getId());
            properties.put("analysis_id", feature.getAnalysisId());
            properties.put("uuid", feature.getUuid());
            properties.put("t1", feature.getT1());
            properties.put("t2", feature.getT2());
            properties.put("t3", feature.getT3());
            properties.put("t4", feature.getT4());
            properties.put("t5", feature.getT5());
            properties.put("t6", feature.getT6());
            properties.put("t7", feature.getT7());
            properties.put("t8", feature.getT8());

            properties.put("n1", feature.getN1());
            properties.put("n2", feature.getN2());
            properties.put("n3", feature.getN3());
            properties.put("n4", feature.getN4());
            properties.put("n5", feature.getN5());
            properties.put("n6", feature.getN6());
            properties.put("n7", feature.getN7());
            properties.put("n8", feature.getN8());

            properties.put("d1", feature.getD1());
            properties.put("d2", feature.getD2());
            properties.put("d3", feature.getD3());
            properties.put("d4", feature.getD4());

            properties.put("created", feature.getCreated());
            properties.put("updated", feature.getUpdated());
            jsonFeature.put("properties", properties);

        } catch(JSONException ex) {
            log.warn("Failed to convert UserLayerData to GeoJSONFeature");
        }

        return jsonFeature;
     }

}
