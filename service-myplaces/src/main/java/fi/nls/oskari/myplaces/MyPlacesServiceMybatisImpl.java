package fi.nls.oskari.myplaces;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.ConversionHelper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Oskari
public class MyPlacesServiceMybatisImpl extends MyPlacesService {

    private static final Logger LOG = LogFactory.getLogger(
            MyPlacesServiceMybatisImpl.class);

    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private SqlSessionFactory factory = null;

    public MyPlacesServiceMybatisImpl() {

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
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(MyPlaceMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }


    /**
     * Check if user can insert a place in category
     * @param user
     * @return
     */
    public boolean canInsert(final User user, final long categoryId) {
        final Resource resource = getResource(categoryId);
        final boolean isDrawLayer = resource.hasPermission(user, PERMISSION_TYPE_DRAW);
        // returns true if users own layer or if published as a draw layer
        return isDrawLayer || canModifyCategory(user,categoryId);
    }

    public Resource getResource(final long categoryId) {
        return getResource(MYPLACES_LAYERID_PREFIX + categoryId);
    }

    public Resource getResource(final String myplacesLayerId) {
        final Resource resource = new Resource();
        resource.setType(RESOURCE_TYPE_MYPLACES);
        resource.setMapping(myplacesLayerId);
        final Resource dbRes = permissionsService.findResource(resource);
        if(dbRes == null) {
            return resource;
        }
        return dbRes;
    }

    /**
     * Check if user can update/delete place
     * @param user
     * @return
     */
    public boolean canModifyPlace(final User user, final long placeId) {

        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            MyPlace place =  mapper.findPlace(placeId);
            if(place == null) {
                return false;
            }
            return place.isOwnedBy(user.getUuid());

        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to load place with id:", placeId);
        } finally {
            session.close();
        }
        return false;
    }

    public boolean canModifyCategory(final User user, final String layerId) {
        LOG.debug("canModifyCategory - layer:", layerId, "- User:", user);
        if(layerId == null || user.isGuest()) {
            return false;
        }
        if(!layerId.startsWith(MYPLACES_LAYERID_PREFIX)) {
            return false;
        }
        final long categoryId = ConversionHelper.getLong(layerId.substring(MYPLACES_LAYERID_PREFIX.length()), -1);
        if(categoryId == -1) {
            return false;
        }
        return canModifyCategory(user, categoryId);
    }

    /**
     * Check if user can update/delete category
     * @param user
     * @return
     */
    public boolean canModifyCategory(final User user, final long categoryId) {
        LOG.debug("canModifyCategory - categoryId:", categoryId, "- User:", user);
        try {
            MyPlaceCategory cat =  findCategory(categoryId);
            if(cat == null) {
                return false;
            }
            return cat.isOwnedBy(user.getUuid());

        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to load category with id:", categoryId);
        }
        return false;
    }
    public List<MyPlaceCategory> getCategories() {

        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            return mapper.findAll();
        } catch (Exception e) {
            LOG.error(e, "Failed to load categories");
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }
    public MyPlaceCategory findCategory(long id) {
        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            return  mapper.find(id);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to load category with id:", id);
        } finally {
            session.close();
        }
        return null;
    }

    /**
     * Updates a MyPlace publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {
        LOG.debug("Updating publisher name", id, uuid, name);

        final Map<String, Object> data = new HashMap<>();
        data.put("publisher_name", name);
        data.put("uuid", uuid);
        data.put("id", id);

        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            int rows = mapper.updatePublisherName(data);
            session.commit();
            return rows;
        } catch (Exception e) {
            LOG.error(e, "Failed to update publisher name", data);
        } finally {
            session.close();
        }
        return 0;
    }

    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList) {
        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            return mapper.findByIds(idList);
        } catch (Exception e) {
            LOG.error(e, "Failed load list", idList);
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }

    public List<MyPlaceCategory> getMyPlaceLayersBySearchKey(final String search) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("searchKey", search + ":*");
        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            return mapper.freeFind(data);
        } catch (Exception e) {
            LOG.error(e, "Failed searchwith", search);
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }


    public void deleteByUid(final String uid) {
        final SqlSession session = factory.openSession();
        try {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            mapper.deleteByUid(uid);
        } catch (Exception e) {
            LOG.error(e, "Failed delete by uid ", uid);
        } finally {
            session.close();
        }
    }
}
