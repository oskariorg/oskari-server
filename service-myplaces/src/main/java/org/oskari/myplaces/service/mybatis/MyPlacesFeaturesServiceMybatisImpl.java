package org.oskari.myplaces.service.mybatis;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.myplaces.MyPlaceMapper;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static fi.nls.oskari.map.geometry.ProjectionHelper.getSRID;
import static fi.nls.oskari.map.geometry.WKTHelper.parseWKT;

public class MyPlacesFeaturesServiceMybatisImpl implements MyPlacesFeaturesService {
    private static final Logger LOG = LogFactory.getLogger(
            MyPlacesFeaturesServiceMybatisImpl.class);

    private static final GeoJSONWriter geojsonWriter = new GeoJSONWriter();
    private SqlSessionFactory factory = null;

    public MyPlacesFeaturesServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("myplaces"));
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            LOG.error("Couldn't get datasource for myplaces");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, MyPlace.class, MyPlaceCategory.class);
        MyBatisHelper.addMappers(configuration, MyPlaceMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public JSONObject getFeaturesByCategoryId(long categoryId, String crs) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            LOG.debug("getFeatures by category id: ", categoryId, crs);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            List<MyPlace> places = mapper.findAllByCategoryId(categoryId);
            JSONObject featureCollection = this.toGeoJSONFeatureCollection(places, crs);
            return featureCollection;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to get MyPlaces ");
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public JSONObject getFeaturesByUserId(String uuid, String crs) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            LOG.debug("getFeatures by user id: ", uuid, crs);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            List<MyPlace> places = mapper.findAllByUuId(uuid);
            JSONObject featureCollection = this.toGeoJSONFeatureCollection(places, crs);
            return featureCollection;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to get myplaces by uuid ", uuid);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public JSONObject getFeaturesByMyPlaceId(long[] ids, String crs) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            LOG.debug("getFeatures by my place ids: ", ids, crs);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            List<MyPlace> places = mapper.findAllByPlaceIdIn(ids);
            JSONObject featureCollection = this.toGeoJSONFeatureCollection(places, crs);
            return featureCollection;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying get features by place ids ", ids);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public JSONObject getFeatures(int categoryId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs)  throws ServiceException{
        try (SqlSession session = factory.openSession()) {
            LOG.debug("getFeatures by bbox: ", bbox);

            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            String nativeSrsName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
            String targetSrsName = crs.getIdentifiers()
                .stream()
                .filter(identifier -> identifier.getCodeSpace().equals("EPSG"))
                .map(identifier -> identifier.getCodeSpace() + ":" + identifier.getCode())
                .findFirst()
                .orElse(null);

            int nativeSrid = getSRID(nativeSrsName);
            List<MyPlace> places = mapper.findAllByBBOX(categoryId, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), nativeSrid);

            JSONObject featureCollection = this.toGeoJSONFeatureCollection(places, targetSrsName != null ? targetSrsName : nativeSrsName);
            return featureCollection;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to get features by bounding box ", bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public long[] insert(List<MyPlace> places) throws ServiceException {
        String targetSRSName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
        try (SqlSession session = factory.openSession()) {
            LOG.debug("Adding new places: ", places);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            for (MyPlace place : places) {
                String sourceSRSName = "EPSG:" + place.getGeometry().getSRID();
                Geometry transformed = this.transformGeometry(place.getGeometry(), sourceSRSName, targetSRSName);
                place.setGeometry(transformed);

                mapper.addMyPlace(place);
                LOG.info("inserted myplace: ", place.getId());
            }
            session.commit();
            return places.stream().mapToLong(MyPlace::getId).toArray();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add MyPlaces: ");
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public int update(List<MyPlace> places) throws ServiceException {
        String targetSRSName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
        try (SqlSession session = factory.openSession()) {
            LOG.debug("Adding new places: ", places);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            for (MyPlace place : places) {
                String sourceSRSName = "EPSG:" + place.getGeometry().getSRID();
                Geometry transformed = this.transformGeometry(place.getGeometry(), sourceSRSName, targetSRSName);
                place.setGeometry(transformed);

                mapper.updateMyPlace(place);
                LOG.info("updated myplace: ", place.getId());
            }
            session.commit();
            return places.size();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add MyPlaces ");
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public int delete(long[] ids) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            LOG.debug("Deleting from myPlaces: ", ids);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            for (long id : ids) {
                mapper.deleteMyPlace(id);
                LOG.info("deleted myplace: ", id);
            }
            session.commit();
            return ids.length;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add MyPlaces ");
            throw new ServiceException(e.getMessage());
        }
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
                throw new ActionParamsException("Invalid " + ActionConstants.PARAM_SRS);
            }
            Geometry transformed = JTS.transform(geometry, transform);
            transformed.setSRID(getSRID(targetSRSName));
            return transformed;

        } catch(Exception e) {
            LOG.warn(e, "Exception transforming geometry");
        }
        return null;
    }

     private JSONObject toGeoJSONFeatureCollection(List<MyPlace> places, String targetSRSName) throws ServiceException {
        if (places == null || places.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
            json.put("crs", geojsonWriter.writeCRSObject(targetSRSName));

            JSONArray features = new JSONArray(places.stream().map(place -> this.toGeoJSONFeature(place, targetSRSName)).collect(Collectors.toList()));
            json.put(GeoJSON.FEATURES, features);

        } catch(JSONException ex) {
            LOG.warn("Failed to create GeoJSON FeatureCollection");
            throw new ServiceException("Failed to create GeoJSON FeatureCollection");
        }
        return json;
     }

     private JSONObject toGeoJSONFeature(MyPlace place, String targetSRSName) {
        JSONObject feature = new JSONObject();
        JSONObject properties = new JSONObject();
        try {
            feature.put("id", place.getId());
            feature.put("geometry_name", GeoJSON.GEOMETRY);
            feature.put(GeoJSON.TYPE, GeoJSON.FEATURE);

            String sourceSRSName = "EPSG:" + place.getDatabaseSRID();
            Geometry transformed = WKTHelper.transform(parseWKT(place.getWkt()), sourceSRSName, targetSRSName);
            JSONObject geoJsonGeometry = geojsonWriter.writeGeometry(transformed);
            feature.put(GeoJSON.GEOMETRY, geoJsonGeometry);

            properties.put("attention_text", place.getAttentionText());
            properties.put("category_id", place.getCategoryId());
            properties.put("created", place.getCreated());
            properties.put("image_url", place.getImageUrl());
            properties.put("link", place.getLink());
            properties.put("name", place.getName());
            properties.put("place_desc", place.getDesc());
            properties.put("updated", place.getUpdated());
            feature.put("properties", properties);

        } catch(JSONException ex) {
            LOG.warn("Failed to convert MyPlace to GeoJSONFeature");
        }

        return feature;
     }
}
