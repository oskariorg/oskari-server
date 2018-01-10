package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

import java.util.*;

/**
 * Not thread-safe implementation of ViewService
 * Stores the objects in an ArrayList
 */
public class ViewServiceMemory implements ViewService {

    private static final Logger LOG = LogFactory.getLogger(ViewServiceMemory.class);

    private final List<View> list = new ArrayList<>();
    private long defaultView = -1L;

    private int indexOf(int id) {
        for (int i = 0; i < list.size(); i++) {
            View item = list.get(i);
            if (item.getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private int indexOf(String name) {
        if (name != null && !name.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                View item = list.get(i);
                if (name.equals(item.getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public View find(int id) {
        for (View item : list) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @Override
    public View find(String id) {
        if (id != null && !id.isEmpty()) {
            for (View item : list) {
                if (id.equals(item.getName())) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public List<Object> findAll() {
        return new ArrayList<Object>(list);
    }

    @Override
    public void delete(int id) {
        int i = indexOf(id);
        if (i >= 0) {
            list.remove(i);
        }
    }

    @Override
    public void delete(Map<String, String> parameterMap) {
        // Do nothing
    }

    @Override
    public void update(Object layerClass) {
        if (layerClass instanceof View) {
            updateView((View) layerClass);
        }
    }

    @Override
    public int insert(Object layerClass) {
        if (layerClass instanceof View) {
            return (int) addView((View) layerClass);
        }
        return -1;
    }

    @Override
    public boolean hasPermissionToAlterView(View view, User user) {
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

    @Override
    public List<View> getViews(int page, int pagesize) {
        int size = list.size();
        int i = (page - 1) * pagesize;
        if (i >= size) {
            return new ArrayList<View>(0);
        }
        int j = Math.min(i + pagesize, size);
        return list.subList(i, j);
    }

    @Override
    public View getViewWithConf(long viewId) {
        return find((int) viewId);
    }

    @Override
    public View getViewWithConfByUuId(String uuId) {
        if (uuId != null && !uuId.isEmpty()) {
            for (View item : list) {
                if (uuId.equals(item.getUuid())) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public View getViewWithConfByOldId(long oldId) {
        for (View item : list) {
            if (oldId == item.getOldId()) {
                return item;
            }
        }
        return null;
    }

    @Override
    public View getViewWithConf(String viewName) {
        if (viewName != null && !viewName.isEmpty()) {
            for (View item : list) {
                if (viewName.equals(item.getName())) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public List<View> getViewsForUser(long userId) {
        List<View> toReturn = new ArrayList<>();
        for (View item : list) {
            if (userId == item.getCreator()) {
                toReturn.add(item);
            }
        }
        return toReturn;
    }

    @Override
    public long addView(View view) {
        view.setUuid(UUID.randomUUID().toString());
        int seq = list.size();
        list.add(view);
        view.setId(seq);
        return seq;
    }

    @Override
    public void updateAccessFlag(View view) {
        View item = find(view.getName());
        if (item != null) {
            item.setIsPublic(view.isPublic());
        }
    }

    @Override
    public void updateView(View view) {
        int i = indexOf(view.getName());
        if (i >= 0) {
            list.set(i, view);
        }
    }

    @Override
    public void deleteViewById(long id) throws DeleteViewException {
        delete((int) id);
    }

    @Override
    public void deleteViewByUserId(long id) throws DeleteViewException {
        Iterator<View> it = list.iterator();
        while (it.hasNext()) {
            View view = it.next();
            if (view.getCreator() == id) {
                it.remove();
            }
        }
    }

    @Override
    public void updateViewUsage(View view) {
        // Not implemented in POJO model
    }

    @Override
    public long getDefaultViewId() {
        return defaultView;
    }

    @Override
    public long getDefaultViewIdForRole(String roleName) {
        return defaultView;
    }

    @Override
    public long getDefaultViewId(User user) {
        return defaultView;
    }

    @Override
    public void resetUsersDefaultViews(long userId) {
        // TODO: implement
    }

    @Override
    public boolean isSystemDefaultView(long id) {
        return id == defaultView;
    }

    @Override
    public long getSystemDefaultViewId(Collection<Role> roles) {
        return defaultView;
    }

    @Override
    public List<Long> getSystemDefaultViewIds() throws ServiceException {
        return null;
    }

    @Override
    public void updatePublishedView(View view) throws ViewException {
        // TODO: implement
    }

    @Override
    public void addBundleForView(long viewId, Bundle bundle) {
        View view = find((int) viewId);
        if (view != null) {
            view.addBundle(bundle);
        }
    }

    @Override
    public void updateBundleSettingsForView(long viewId, Bundle bundle)
            throws ViewException {
        View view = find((int) viewId);
        if (view != null) {
            List<Bundle> bundles = view.getBundles();
            for (int i = 0; i < bundles.size(); i++) {
                if (bundles.get(i).getBundleId() == bundle.getBundleId()) {
                    bundles.set(i, bundle);
                    break;
                }
            }
        }
    }

}
