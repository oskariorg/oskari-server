package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.service.db.BaseService;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface ViewService extends BaseService<Object> {

    boolean hasPermissionToAlterView(final View view, final User user);

    List<View> getViews(int page, int pagesize);

    View getViewWithConf(long viewId);

    View getViewWithConfByUuId(String uuId);

    View getViewWithConfByOldId(long oldId);

    View getViewWithConf(String viewName);

    List<View> getViewsForUser(long userId);

    long addView(View view)
            throws ViewException;

    void updateAccessFlag(View view);

    void updateView(View view);

    void deleteViewById(long id)
            throws DeleteViewException;

    void deleteViewByUserId(long id)
            throws DeleteViewException;

    long getDefaultViewId();

    void updateViewUsage(View view);

    void resetUsersDefaultViews(long user_id);

    /**
     * Returns default view id for given role name
     *
     * @param roleName
     * @return
     */
    long getDefaultViewIdForRole(final String roleName);

    /**
     * Returns default view id for the user (based on role or personalized default view)
     *
     * @param user
     * @return view id
     */
    long getDefaultViewId(final User user);

    /**
     * Check if view is configured as default in system to distinguish between non-personalized default views
     * @param id
     * @return
     */
    boolean isSystemDefaultView(final long id);

    /**
     * Returns if for system default view for given role set.
     * @param roles
     * @return
     */
    long getSystemDefaultViewId(Set<Role> roles);

    void updatePublishedView(View view)
            throws ViewException;

    void addBundleForView(final long viewId, final Bundle bundle)
            throws SQLException;

    /**
     * Updates bundle settings for single bundle in given view.
     *
     * @param viewId
     * @param bundle
     * @throws ViewException if bundle is not part of the view or update failed.
     */
    void updateBundleSettingsForView(final long viewId, final Bundle bundle)
            throws ViewException;

}
