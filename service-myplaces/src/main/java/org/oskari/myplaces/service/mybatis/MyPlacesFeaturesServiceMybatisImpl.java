package org.oskari.myplaces.service.mybatis;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.myplaces.MyPlaceMapper;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.geotools.geometry.jts.JTS;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Float.NaN;

public class MyPlacesFeaturesServiceMybatisImpl implements MyPlacesFeaturesService {
    private static final Logger LOG = LogFactory.getLogger(
            MyPlacesFeaturesServiceMybatisImpl.class);

    private static final WKTReader wktReader = new WKTReader();
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
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(MyPlaceCategory.class);
        configuration.getTypeAliasRegistry().registerAlias(MyPlace.class);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(MyPlaceMapper.class);

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
     private Geometry wktToGeometry(String wkt, String sourceSRSName, String targetSRSName) {
        try {
            Geometry geometry = wktReader.read(wkt);
            Geometry transformed = transformGeometry(geometry, sourceSRSName, targetSRSName);
            return transformed;
        } catch(ParseException e) {
            LOG.warn("Failed to parse geometry from wkt " + wkt);
            return null;
        }
     }

    private int getSRID(String srsName) {
        String srid = srsName.substring(srsName.indexOf(':') + 1);
        return Integer.parseInt(srid);
    }

     private JSONObject toGeoJSONFeatureCollection(List<MyPlace> places, String targetSRSName) throws ServiceException {
        if (places == null || places.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);

            List<Geometry> geometries = places
                .stream()
                .map(place -> wktToGeometry(place.getWkt(), "EPSG:" + place.getDatabaseSRID(), targetSRSName))
                .collect(Collectors.toList());
            json.put(GeoJSON.BBOX, createBBOX(geometries));
            json.put("crs", createCRSObject(targetSRSName));

            JSONArray features = new JSONArray(places.stream().map(place -> this.toGeoJSONFeature(place, targetSRSName)).collect(Collectors.toList()));
            json.put(GeoJSON.FEATURES, features);

        } catch(JSONException ex) {
            LOG.warn("Failed to create GeoJSON FeatureCollection");
            throw new ServiceException("Failed to create GeoJSON FeatureCollection");
        }
        return json;
     }

     private JSONObject createCRSObject(String srsName) {
        JSONObject crs = new JSONObject();
        try {
            crs.put("type", "name");
            JSONObject crsProperties = new JSONObject();
            crsProperties.put("name", srsName);
            crs.put(GeoJSON.PROPERTIES, crsProperties);

        } catch(JSONException e) {
            LOG.warn("Failed to create crs object.");
            return null;
        }

        return crs;
     }

     private JSONArray createBBOX(List<Geometry> geometries) {
        JSONArray bbox = null;
        try {
            double minX = NaN, minY = NaN, maxX = NaN, maxY = NaN;
            for (Geometry g : geometries) {
                if (g.getEnvelopeInternal().getMinX() < minX || Double.isNaN(minX)) {
                    minX = g.getEnvelopeInternal().getMinX();
                }
                if (g.getEnvelopeInternal().getMaxX() > maxX || Double.isNaN(maxX)) {
                    maxX = g.getEnvelopeInternal().getMaxX();
                }
                if (g.getEnvelopeInternal().getMinY() < minY || Double.isNaN(minY)) {
                    minY = g.getEnvelopeInternal().getMinY();
                }
                if (g.getEnvelopeInternal().getMaxY() > maxY || Double.isNaN(maxY)) {
                    maxY = g.getEnvelopeInternal().getMaxY();
                }
            }
            bbox = new JSONArray(new double[]{
                minX,
                minY,
                maxX,
                maxY
            });
        } catch(JSONException e) {
            LOG.warn("Failed to calculate bbox");
        }

        return bbox;
     }
     private JSONObject toGeoJSONFeature(MyPlace place, String targetSRSName) {
        JSONObject feature = new JSONObject();
        JSONObject properties = new JSONObject();
        try {
            feature.put("id", place.getId());
            feature.put("geometry_name", GeoJSON.GEOMETRY);
            feature.put(GeoJSON.TYPE, GeoJSON.FEATURE);
            String sourceSRSName = "EPSG:" + place.getDatabaseSRID();
            Geometry transformed = wktToGeometry(place.getWkt(), sourceSRSName, targetSRSName);
            JSONObject geoJsonGeometry = geojsonWriter.writeGeometry(transformed);
            feature.put(GeoJSON.GEOMETRY, geoJsonGeometry);
            JSONArray featureBbox = createBBOX(Collections.singletonList(transformed));
            feature.put(GeoJSON.BBOX, featureBbox);

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
