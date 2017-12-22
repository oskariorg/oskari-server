package fi.nls.oskari.service.capabilities;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CapabilitiesMapper {

    @Select("SELECT id, url, layertype, version, data, created, updated"
            + " FROM oskari_capabilities_cache"
            + " WHERE id = #{id}")
    OskariLayerCapabilities findById(@Param("id") final long id);

    @Select("SELECT id, url, layertype, version, data, created, updated"
            + " FROM oskari_capabilities_cache"
            + " WHERE url = #{url}"
            + " AND layertype = #{type}"
            + " AND (version = #{version} OR version IS NULL)"
            + " ORDER BY version ASC"
            + " LIMIT 1")
    OskariLayerCapabilities findByUrlTypeVersion(
            @Param("url") final String url,
            @Param("type") final String type,
            @Param("version") final String version);

    @Select("SELECT id"
            + " FROM oskari_capabilities_cache"
            + " WHERE url = #{url}"
            + " AND layertype = #{type}"
            + " AND (version = #{version} OR version IS NULL)"
            + " ORDER BY version ASC"
            + " LIMIT 1")
    Long selectIdByUrlTypeVersion(
            @Param("url") final String url,
            @Param("type") final String type,
            @Param("version") final String version);

    @Insert("INSERT INTO oskari_capabilities_cache"
            + " (layertype, url, data, version) VALUES"
            + " (#{layertype}, #{url}, #{data}, #{version})")
    void insert(OskariLayerCapabilities draft);

    @Update("UPDATE oskari_capabilities_cache SET"
            + " data = #{data},"
            + " updated = current_timestamp"
            + " WHERE id = #{id}")
    void updateData(@Param("id") final long id, @Param("data") final String data);

}
