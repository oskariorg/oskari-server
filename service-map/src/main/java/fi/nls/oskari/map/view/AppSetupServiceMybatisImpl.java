package fi.nls.oskari.map.view;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.user.Role;
import org.oskari.user.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.sql.DataSource;
import java.util.*;

@Oskari
public class AppSetupServiceMybatisImpl extends ViewService {

    private static final Logger LOG = LogFactory.getLogger(AppSetupServiceMybatisImpl.class);

    private static final String PROP_VIEW_DEFAULT = "view.default";
    private static final String PROP_VIEW_DEFAULT_ROLES = "view.default.roles";

    private final Map<String, Long> roleToDefaultViewId;
    private final String[] defaultViewRoles;
    private final long defaultViewId;

    private SqlSessionFactory factory = null;

    public AppSetupServiceMybatisImpl() {
        this(null);
    }
    public AppSetupServiceMybatisImpl(DataSource dataSource) {
        if (dataSource == null) {
            final DatasourceHelper helper = DatasourceHelper.getInstance();
            dataSource = helper.getDataSource();
            if (dataSource == null) {
                dataSource = helper.createDataSource();
            }
            if (dataSource == null) {
                LOG.error("Couldn't get datasource for app setup service");
            }
        }
        factory = initializeMyBatis(dataSource);

        defaultViewRoles = PropertyUtil.getCommaSeparatedList(PROP_VIEW_DEFAULT_ROLES);
        roleToDefaultViewId = initDefaultViewsByRole(defaultViewRoles);
        defaultViewId = initDefaultViewId();

    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, Bundle.class, View.class);
        MyBatisHelper.addMappers(configuration, AppSetupMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private Map<String, Long> initDefaultViewsByRole(String[] roles) {
        if (roles.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Long> roleToDefaultViewId = new HashMap<>();
        for (String role : roles) {
            String roleViewIdStr = PropertyUtil.get(PROP_VIEW_DEFAULT + "." + role);
            long roleViewId = ConversionHelper.getLong(roleViewIdStr, -1);
            if (roleViewId != -1) {
                roleToDefaultViewId.put(role, roleViewId);
                LOG.debug("Added default view", roleViewId, "for role", role);
            } else {
                LOG.info("Failed to set default view id for role", role,
                        "property missing or value invalid");
            }
        }
        return roleToDefaultViewId;
    }

    private long initDefaultViewId() {
        LOG.debug("Init default view id");
        long property = ConversionHelper.getLong(PropertyUtil.get(PROP_VIEW_DEFAULT), -1);
        if (property != -1) {
            LOG.debug("Global default view id from properties:" , property);
            return property;
        }
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            Long database = mapper.getDefaultViewId(ViewTypes.DEFAULT);
            if (database != null) {
                return database;
            }
        } catch (Exception e) {
            LOG.warn(e, "Exception while init deafult view id");
        }
        return -1;
    }

    public boolean hasPermissionToAlterView(final View view, final User user) {

        // uuids are much longer than 10 actually but check for atleast 10
        if(user.getUuid() == null || user.getUuid().length() < 10) {
            LOG.debug("Users uuid is missing or invalid: ", user.getUuid());
            // user doesn't have an uuid, he shouldn't have any published maps
            return false;
        }
        if(view == null) {
            LOG.debug("View is null");
            // view with id not found
            return false;
        }
        if(user.isGuest()) {
            LOG.debug("User is default/guest user");
            return false;
        }
        if(view.getCreator() != user.getId()) {
            // check current user id against view creator (is it the same user)
            LOG.debug("Users id:", user.getId(), "didn't match view creator:", view.getCreator());
            return false;
        }
        return true;
    }

    public List<View> getViews(int page, int pageSize) {
        LOG.debug("Get views");
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            final Map<String, Object> params = new HashMap<>();
            int offset = (page -1) * pageSize;
            return mapper.getViews(offset, pageSize);
        } catch (Exception e) {
            LOG.warn(e, "");
        }
        return Collections.emptyList();
    }

    public View getViewWithConf(long viewId) {
        LOG.debug("Get view with conf by view id: " + viewId);
        if (viewId < 1) {
            return null;
        }
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            return mapper.getViewWithConfByViewId(viewId);
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting view with conf by view id: " + viewId);
        }
        return null;
    }

    public View getViewWithConfByUuId(String uuId) {
        if (uuId == null) {
            return null;
        }

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            View view = mapper.getViewWithConfByUuId(uuId);
            if (view != null) {
                view.setBundles(mapper.getBundlesByViewId(view.getId()));
            }
            return view;
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting view with config by uuid: " + uuId);
        }
        return null;
    }

    public View getViewWithConfByOldId(long oldId) {
        LOG.debug("Get view with conf by old id");
        if (oldId < 1) {
            return null;
        }
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            return mapper.getViewWithConfByOldId(oldId);
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting view with config by old id");
        }
        return null;
    }

    public View getViewWithConf(String viewName) {
        LOG.debug("Get view with conf by view name: " + viewName);
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            return mapper.getViewWithConfByViewName(viewName);
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting view with conf by view name");
        }
        return null;
    }

    public List<View> getViewsForUser(long userId) {
        LOG.debug("Get views for user with id: " + userId);
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            List<View> views = mapper.getViewsWithConfByUserId(userId);
            LOG.debug("Found", views.size(), "views for user", userId);
            return views;
        } catch (Exception e) {
            LOG.warn(e, "Error getting views for user", userId);
        }

        return Collections.emptyList();
    }

    public long addView(View view) throws ViewException {
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            view.setUuid(UUID.randomUUID().toString());
            mapper.addView(view);
            long id = view.getId();
            LOG.info("Inserted view with id", id);
            for (Bundle bundle : view.getBundles()) {
                bundle.setViewId(id);
                mapper.addBundle(bundle);
            }
            session.commit();
            return id;
        } catch (Exception e) {
            LOG.warn(e, "Exception while adding a new view");
        }
        return -1;
    }

    public void updateAccessFlag(View view) {
        LOG.debug("Update access flag");

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            mapper.updateAccessFlag(view);
            session.commit();
        } catch (Exception e) {
            LOG.warn(e, "Exception while updating access flag");
        }
    }

    public void deleteViewById(final long id) throws DeleteViewException {
        View view = getViewWithConf(id);
        if (view == null) {
            throw new DeleteViewException("Couldn't find a view with id:" + id);
        }
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            mapper.deleteBundleByView(id);
            mapper.deleteView(id);
            session.commit();
        } catch (Exception e) {
            throw new DeleteViewException("Error deleting a view with id:" + id, e);
        }
    }

    public void deleteViewByUserId(long userId) throws DeleteViewException {
        LOG.debug("Delete view by user id: " + userId);

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            mapper.deleteViewByUser(userId);
            session.commit();
        } catch (Exception e) {
            throw new DeleteViewException("Error deleting a view with user id:" + userId, e);
        }
    }

    public void resetUsersDefaultViews(long userId) {
        LOG.debug("Reset users default views for user id : " + userId);

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            mapper.resetUsersDefaultViews(userId);
            session.commit();
        } catch (Exception e) {
            LOG.warn(e, "Exception while resetting users default views");
        }
    }

    public void updateView(View view) {
        LOG.debug("Update view");

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            mapper.update(view);
            session.commit();
        } catch (Exception e) {
            LOG.warn(e, "Exception while updating view");
        }
    }

    public void updateViewUsage(View view) {
        LOG.debug("Update view usage");

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            mapper.updateUsage(view.getId());
            session.commit();
        } catch (Exception e) {
            LOG.warn(e, "Exception while updating view usage");
        }
    }

    public void updatePublishedView(final View view) throws ViewException {
        LOG.debug("Update published view");
        long id = view.getId();

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            updateView(view);
            mapper.deleteBundleByView(id);
            for (Bundle bundle : view.getBundles()) {
                bundle.setViewId(id);
                mapper.addBundle(bundle);
            }
            session.commit();
        } catch (Exception e) {
            throw new ViewException("Error updating a view with id:" + id, e);
        }
    }

    public void updateBundleSettingsForView(final long viewId, final Bundle bundle) throws ViewException {
        LOG.debug("Update bundle settings for view");

        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            final int numUpdated = mapper.updateBundleSettingsInView(viewId, bundle);
            if(numUpdated == 0) {
                // not updated, bundle not found
                throw new ViewException("Failed to update - bundle not found in view?");
            }
            session.commit();
        } catch (Exception e) {
            throw new ViewException("Failed to update", e);
        }
    }

    public long getDefaultViewId() {
        return defaultViewId;
    }

    /**
     * Returns default view id for the user, based on user roles. Configured by properties:
     *
     * view.default=[global default view id that is used if role-based default view is not found]
     * view.default.roles=[comma-separated list of role names in descending order f.ex. Admin, User, Guest]
     * view.default.[role name]=[default view id for the role]
     *
     * If properties are not found, defaults to #getDefaultViewId()
     * @param user to get default view for
     * @return view id based on users roles
     */
    public long getDefaultViewId(final User user) {
        if(user == null) {
            LOG.debug("Tried to get default view for <null> user");
            return getDefaultViewId();
        }
        else {
            final long personalizedId = getPersonalizedDefaultViewId(user);
            if(personalizedId != -1) {
                return personalizedId;
            }
            return getSystemDefaultViewId(user.getRoles());
        }
    }

    public long getSystemDefaultViewId(Collection<Role> roles) {
        if (roles == null) {
            LOG.debug("Tried to get default view for <null> roles");
        } else {
            // Check the roles in given order and return the first match
            for (String defaultViewRole : defaultViewRoles) {
                if (Role.hasRoleWithName(roles, defaultViewRole)) {
                    Long rolesDefaultViewId = roleToDefaultViewId.get(defaultViewRole);
                    if (rolesDefaultViewId != null) {
                        LOG.debug("Default view found for role", defaultViewRole, ":", rolesDefaultViewId);
                        return rolesDefaultViewId;
                    }
                }
            }
        }
        LOG.debug("No role based default views matched user roles:", roles, ". Defaulting to global default.");
        return getDefaultViewId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getSystemDefaultViewIds() throws ServiceException {
        LOG.debug("Get system default view ids");
        try (final SqlSession session = factory.openSession()) {
            final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
            return mapper.getDefaultViewIds();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public boolean isSystemDefaultView(final long id) {
        return roleToDefaultViewId.containsValue(id) || getDefaultViewId() == id;
    }

    /**
     * Returns the saved default view id for the user, if one exists
     *
     * @param user to get default view for
     * @return view id of a saved default view
     */
    private long getPersonalizedDefaultViewId(final User user) {
        LOG.debug("Get personalized default view id");
        if (!user.isGuest() && user.getId() != -1) {

            try (final SqlSession session = factory.openSession()) {
                final AppSetupMapper mapper = session.getMapper(AppSetupMapper.class);
                Long queryResult = mapper.geDefaultViewIdByUserId(user.getId());
                if (queryResult != null) {
                    return queryResult;
                }
            } catch (Exception e) {
                LOG.warn(e, "Exception while getting personalized default view id");
            }
        }
        return -1;
    }

    /**
     * Returns default view id for given role name
     * @param roleName
     * @return
     */
    public long getDefaultViewIdForRole(final String roleName) {
        Long rolesDefaultViewId = roleToDefaultViewId.get(roleName);
        return rolesDefaultViewId != null ? rolesDefaultViewId : defaultViewId;
    }

}