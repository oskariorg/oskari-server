package fi.nls.oskari.myplaces.service;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.myplaces.MyPlaceMapper;
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
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import javax.sql.DataSource;
import java.util.List;

public class MyPlacesFeaturesServiceMybatisImpl implements MyPlacesFeaturesService {
    private static final Logger LOG = LogFactory.getLogger(
            MyPlacesFeaturesServiceMybatisImpl.class);

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
        return null;
    }

    @Override
    public JSONObject getFeaturesByUserId(String uuid, String crs) throws ServiceException {
        return null;
    }

    @Override
    public JSONObject getFeaturesByMyPlaceId(long[] ids, String crs) throws ServiceException {
        return null;
    }

    @Override
    public long[] insert(List<MyPlace> places) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            LOG.debug("Adding new places: ", places);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            for (MyPlace place : places) {
                Geometry transformed = this.doGeometryTransform(place.getGeometry());
                place.setGeometry(transformed);

                mapper.addMyPlace(place);
                LOG.info("inserted myplace: ", place.getId());
            }
            session.commit();
            return places.stream().mapToLong(MyPlace::getId).toArray();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add MyPlaces: ");
        }

        return null;
    }


    @Override
    public int update(List<MyPlace> places) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            LOG.debug("Adding new places: ", places);
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            for (MyPlace place : places) {
                Geometry transformed = this.doGeometryTransform(place.getGeometry());
                place.setGeometry(transformed);

                mapper.updateMyPlace(place);
                LOG.info("inserted myplace: ", place.getId());
            }
            session.commit();
            return places.size();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add MyPlaces: ");
        }

        return -1;
    }

    @Override
    public int delete(long[] ids) throws ServiceException {
        return 0;
    }

    private Geometry doGeometryTransform(Geometry geometry) throws ServiceException {
        try {
            String targetSRSName = PropertyUtil.get("oskari.native.srs", "EPSG:3857");
            String sourceSRSName = "EPSG:" + geometry.getSRID();
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

    private int getSRID(String srsName) {
        String srid = srsName.substring(srsName.indexOf(':') + 1);
        return Integer.parseInt(srid);
    }


}
