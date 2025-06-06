package fi.nls.oskari.myplaces;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Oskari
public class MyPlacesServiceMybatisImpl extends MyPlacesService {

    private static final Logger LOG = LogFactory.getLogger(
            MyPlacesServiceMybatisImpl.class);

    private final Cache<MyPlaceCategory> cache;

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
        cache = CacheManager.getCache(getClass().getName());
    }

    private PermissionService getPermissionsService() {
        // Working around timing issues with runtime fetching instead of getting reference on constructor
        return OskariComponentManager.getComponentOfType(PermissionService.class);
    }

    private MyPlaceCategory getFromCache(long id) {
        return cache.get(getCacheKey(id));
    }

    public static String getCacheKey(long id) {
        return UserContentMyPlacesService.getPlaceCacheKey(id);
    }

    private MyPlaceCategory cache(MyPlaceCategory layer) {
        if (layer != null) {
            cache.put(Long.toString(layer.getId()), layer);
        }
        return layer;
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, MyPlace.class, MyPlaceCategory.class);
        MyBatisHelper.addMappers(configuration, MyPlaceMapper.class);

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
        return getPermissionsService().findResource(ResourceType.myplaces, myplacesLayerId).orElseGet(() -> {
                    final Resource resource = new Resource();
                    resource.setType(ResourceType.myplaces);
                    resource.setMapping(myplacesLayerId);
                    return resource;
        });
    }

    /**
     * Check if user can update/delete place
     * @param user
     * @return
     */
    public boolean canModifyPlace(final User user, final long placeId) {

        try (final SqlSession session = factory.openSession()) {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            MyPlace place =  mapper.findPlace(placeId);
            if(place == null) {
                return false;
            }
            return place.isOwnedBy(user.getUuid());

        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to load place with id:", placeId);
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
            MyPlaceCategory cat = findCategory(categoryId);
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

        try (final SqlSession session = factory.openSession()) {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            return mapper.findAll();
        } catch (Exception e) {
            LOG.error(e, "Failed to load categories");
        }
        return Collections.emptyList();
    }
    public MyPlaceCategory findCategory(long id) {
        MyPlaceCategory layer = getFromCache(id);
        if (layer != null) {
            return layer;
        }
        try (final SqlSession session = factory.openSession()) {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            layer = mapper.find(id);
            return cache(layer);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to load category with id:", id);
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

        try (final SqlSession session = factory.openSession()) {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            int rows = mapper.updatePublisherName(name, uuid, id);
            session.commit();
            // update data in cache
            MyPlaceCategory layer = getFromCache(id);
            if (layer != null && rows > 0) {
                cache.remove(getCacheKey(id));
            }
            return rows;
        } catch (Exception e) {
            LOG.error(e, "Failed to update publisher name", name, uuid, id);
        }
        return 0;
    }

    public List<MyPlaceCategory> getMyPlaceLayersById(List<Long> idList) {
        try (final SqlSession session = factory.openSession()) {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            return mapper.findByIds(idList);
        } catch (Exception e) {
            LOG.error(e, "Failed load list", idList);
        }
        return Collections.emptyList();
    }

    public void deleteByUid(final String uid) {
        try (final SqlSession session = factory.openSession()) {
            final MyPlaceMapper mapper = session.getMapper(MyPlaceMapper.class);
            mapper.deleteByUid(uid);
        } catch (Exception e) {
            LOG.error(e, "Failed delete by uid ", uid);
        }
    }
}
