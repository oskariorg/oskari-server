package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface AppSetupMapper {

    @Select("SELECT MAX(id) FROM oskari_appsetup WHERE is_default = TRUE AND type = #{type}")
    Long getDefaultViewId(String type);

    @Select("SELECT id, name, description, type, uuid, only_uuid, application, page, creator, is_public, is_default, domain, lang, metadata, created, updated" +
            " FROM oskari_appsetup" +
            " ORDER BY created ASC" +
            " OFFSET ${offset} limit ${pageSize}")
    @ResultMap("viewWithConf")
    List<View> getViews(@Param("offset") int offset, @Param("pageSize") int pageSize);


    @Select("SELECT id, name, description, type, uuid, only_uuid, application, page, creator, is_public, is_default, domain, lang, metadata, created, updated" +
            " FROM oskari_appsetup" +
            " WHERE id = #{viewId}")
    @ResultMap("viewWithConf")
    View getViewWithConfByViewId(long viewId);

    @Select("SELECT id, name, description, type, uuid, only_uuid, application, page, creator, is_public, is_default, domain, lang, metadata, created, updated" +
            " FROM oskari_appsetup" +
            " WHERE uuid::text = #{uuId}")
    @ResultMap("viewWithConf")
    View getViewWithConfByUuId(String uuId);

    // This doesn't have a mapping, FIXME: remove
    View getViewWithConfByOldId(long oldId);

    @Select("SELECT id, name, description, type, uuid, only_uuid, application, page, creator, is_public, is_default, domain, lang, metadata, created, updated" +
            " FROM oskari_appsetup" +
            " WHERE name = #{name}")
    @ResultMap("viewWithConf")
    View getViewWithConfByViewName(String viewName);

    @Select("SELECT id, name, description, type, uuid, only_uuid, application, page, creator, is_public, is_default, domain, lang, metadata, created, updated" +
            " FROM oskari_appsetup" +
            " WHERE creator = #{userId}" +
            " ORDER BY name ASC")
    @ResultMap("viewWithConf")
    List<View> getViewsWithConfByUserId(long userId);

    List<Bundle> getBundlesByViewId(long viewId);

    @Insert("INSERT INTO oskari_appsetup ( name, \"description\", type, page, application, " +
            "            uuid, only_uuid, domain, lang, creator, is_public, is_default, metadata) " +
            "        VALUES ( #{name}, #{description}, #{type}, #{page}, #{application}, '${uuid}', ${onlyForUuId}, " +
            "        #{pubDomain}, #{lang}, #{creator}, #{isPublic}, #{isDefault}, #{metadataAsString} )")
    @Options(useGeneratedKeys=true, keyColumn = "id", keyProperty = "id")
    void addView(View view);

    @Update("UPDATE oskari_appsetup SET is_public = #{isPublic}, updated = #{updated} WHERE id = #{id}")
    void updateAccessFlag(View view);

    @Delete("DELETE FROM oskari_appsetup_bundles WHERE appsetup_id = #{id}")
    void deleteBundleByView(long id);

    @Delete("DELETE FROM oskari_appsetup WHERE id = #{id}")
    void deleteView(long id);
    @Delete("DELETE FROM oskari_appsetup WHERE creator = #{userId}")
    void deleteViewByUser(long userId);

    @Update("UPDATE oskari_appsetup SET is_default = FALSE, updated = NOW() WHERE creator = #{userId} AND is_default = TRUE AND type = 'USER'")
    void resetUsersDefaultViews(long userId);

    @Update("UPDATE oskari_appsetup SET" +
            " name = #{name}," +
            " description = #{description}," +
            " application = #{application}," +
            " page = #{page}," +
            " domain = #{pubDomain}," +
            " lang = #{lang}," +
            " is_public = #{isPublic}," +
            " is_default = #{isDefault}," +
            " metadata = #{metadataAsString}," +
            " updated = #{updated}" +
            " WHERE id = #{id}")
    void update(View view);

    @Update("UPDATE oskari_appsetup SET " +
            " used=now()," +
            " usagecount=(SELECT usagecount+1 FROM oskari_appsetup WHERE id=#{id}) " +
            " WHERE id=#{id}")
    void updateUsage(long id);

    @Insert("INSERT INTO oskari_appsetup_bundles (appsetup_id, bundle_id, seqno, state, config, bundleinstance)" +
            " VALUES ( #{viewId}, #{bundleId}, #{seqNo}, #{state}, #{config}, #{bundleinstance})")
    void addBundle(Bundle bundle);


    @Update("UPDATE oskari_appsetup_bundles SET" +
            " config = #{bundle.config}," +
            " state = #{bundle.state}," +
            " bundleinstance = #{bundle.bundleinstance}" +
            " WHERE appsetup_id = #{view_id} AND bundle_id = #{bundle.bundleId}")
    int updateBundleSettingsInView(@Param("view_id") long viewId, @Param("bundle") Bundle bundle);

    @Select("SELECT id FROM oskari_appsetup WHERE is_default = TRUE AND type = 'DEFAULT' AND creator = -1")
    List<Long> getDefaultViewIds();

    @Select("SELECT MAX(id) FROM oskari_appsetup WHERE is_default = TRUE AND type = 'USER' AND creator = #{userId}")
    Long geDefaultViewIdByUserId(long userId);
}
