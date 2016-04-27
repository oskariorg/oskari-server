package fi.nls.oskari.service.capabilities;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Created by SMAKINEN on 24.8.2015.
 */
public interface CapabilitiesMapper {

    @Select("SELECT id, layertype, url, data, created, updated, version" +
            " FROM oskari_capabilities_cache" +
            " WHERE url = #{url} AND layertype = #{type} AND version = #{version}")
    OskariLayerCapabilities find(@Param("url") final String url, @Param("type")final String type, @Param("version") final String version);

    @Insert("INSERT INTO oskari_capabilities_cache(layertype, url, data, version) VALUES (lower(#{layertype}), lower(#{url}), #{data}, #{version})")
    void insert(OskariLayerCapabilities capabilities);

    @Update("UPDATE oskari_capabilities_cache SET " +
            "   data = #{data}," +
            "   updated = current_timestamp" +
            "   WHERE id = #{id}")
    void updateData(OskariLayerCapabilities capabilities);
}
