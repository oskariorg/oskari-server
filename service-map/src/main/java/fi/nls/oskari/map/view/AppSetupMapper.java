package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;

import java.util.List;
import java.util.Map;

public interface AppSetupMapper {
    long getDefaultViewId(String type);
    List<View> getViews(final Map<String, Object> params);
    View getViewWithConfByViewId(long viewId);
    View getViewWithConfByUuId(String uuId);
    View getViewWithConfByOldId(long oldId);
    View getViewWithConfByViewName(String viewName);
    List<View> getViewsWithConfByUserId(long userId);
    List<Bundle> getBundlesByViewId(long viewId);
    long addView(View view);
    void updateAccessFlag(View view);
    void deleteBundleByView(long id);
    void deleteView(long id);
    void deleteViewByUser(long userId);
    void resetUsersDefaultViews(long userId);
    void update(View view);
    void updateUsage(View view);
    void addBundle(Bundle bundle);
    int updateBundleSettingsInView(final Map<String, Object> params);
    List<Long> getDefaultViewIds();
    long geDefaultViewIdByUserId(long userId);
}
