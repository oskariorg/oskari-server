package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface AppSetupMapper {
    @Select("SELECT MAX(id) FROM portti_view WHERE is_default = TRUE AND type = #{type}")
    Long getDefaultViewId(String type);
    List<View> getViews(final Map<String, Object> params);
    View getViewWithConfByViewId(long viewId);
    View getViewWithConfByUuId(String uuId);
    View getViewWithConfByOldId(long oldId);
    View getViewWithConfByViewName(String viewName);
    List<View> getViewsWithConfByUserId(long userId);
    List<Bundle> getBundlesByViewId(long viewId);
    long addView(View view);

    @Update("UPDATE portti_view SET is_public = #{isPublic} WHERE id = #{id}")
    void updateAccessFlag(View view);

    @Delete("DELETE FROM portti_view_bundle_seq WHERE view_id = #{id}")
    void deleteBundleByView(long id);

    @Delete("DELETE FROM portti_view WHERE id = #{id}")
    void deleteView(long id);
    @Delete("DELETE FROM portti_view WHERE creator = #{userId}")
    void deleteViewByUser(long userId);

    @Update("UPDATE portti_view SET is_default = FALSE WHERE creator = #{userId} AND type = 'USER'")
    void resetUsersDefaultViews(long userId);
    void update(View view);
    void updateUsage(View view);
    void addBundle(Bundle bundle);
    int updateBundleSettingsInView(final Map<String, Object> params);

    @Select("SELECT id FROM portti_view WHERE is_default = TRUE AND type = 'DEFAULT' AND creator = -1")
    List<Long> getDefaultViewIds();

    @Select("SELECT MAX(id) FROM portti_view WHERE is_default = TRUE AND type = 'USER' AND creator = #{userId}")
    Long geDefaultViewIdByUserId(long userId);
}
