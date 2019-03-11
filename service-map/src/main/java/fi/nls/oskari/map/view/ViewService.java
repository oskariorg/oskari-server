package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import java.util.Collection;
import java.util.List;

public abstract class ViewService extends OskariComponent {

    public static String PROPERTY_PUBLISH_TEMPLATE = "view.template.publish";

    public abstract boolean hasPermissionToAlterView(final View view, final User user);
    public abstract List<View> getViews(int page, int pagesize);
    public abstract View getViewWithConf(long viewId);
    public abstract View getViewWithConfByUuId(String uuId);
    public abstract View getViewWithConfByOldId(long oldId);
    public abstract View getViewWithConf(String viewName);
    public abstract List<View> getViewsForUser(long userId);

    public abstract long addView(View view)
            throws ViewException;

    public abstract void updateAccessFlag(View view);

    public abstract void updateView(View view);

    public abstract void deleteViewById(long id)
            throws DeleteViewException;

    public abstract void deleteViewByUserId(long id)
            throws DeleteViewException;

    public abstract long getDefaultViewId();
    public abstract void updateViewUsage(View view);
    public abstract void resetUsersDefaultViews(long userId);

    /**
     * Returns default view id for given role name
     *
     * @param roleName
     * @return
     */
    public abstract long getDefaultViewIdForRole(final String roleName);

    /**
     * Returns default view id for the user (based on role or personalized default view)
     *
     * @param user
     * @return view id
     */
    public abstract long getDefaultViewId(final User user);

    /**
     * Check if view is configured as default in system to distinguish between non-personalized default views
     * @param id
     * @return
     */
    public abstract boolean isSystemDefaultView(final long id);

    /**
     * Returns if for system default view for given role set.
     * @param roles
     * @return
     */
    public abstract long getSystemDefaultViewId(Collection<Role> roles);

    public abstract List<Long> getSystemDefaultViewIds() throws ServiceException;

    public abstract void updatePublishedView(View view)
            throws ViewException;

    /**
     * Updates bundle settings for single bundle in given view.
     *
     * @param viewId
     * @param bundle
     * @throws ViewException if bundle is not part of the view or update failed.
     */
    public abstract void updateBundleSettingsForView(final long viewId, final Bundle bundle)
            throws ViewException;

}
