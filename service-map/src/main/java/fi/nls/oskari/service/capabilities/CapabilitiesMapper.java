package fi.nls.oskari.service.capabilities;

import java.sql.Timestamp;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    @Select("INSERT INTO oskari_capabilities_cache"
            + " (layertype, url, data, version, updated) VALUES"
            + " (#{layertype}, #{url}, #{data}, #{version}, current_timestamp)"
            + " RETURNING id, created, updated")
    OskariLayerCapabilitiesInsertInfo insert(OskariLayerCapabilitiesDraft draft);

    @Select("UPDATE oskari_capabilities_cache SET"
            + " data = #{data},"
            + " updated = current_timestamp"
            + " WHERE id = #{id}"
            + " RETURNING updated")
    Timestamp updateData(@Param("id") final long id, @Param("data") final String data);

}
