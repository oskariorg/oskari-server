package fi.nls.oskari.map.style;

import fi.nls.oskari.domain.map.style.VectorStyle;
import org.apache.ibatis.annotations.*;

import java.time.OffsetDateTime;
import java.util.List;

public interface VectorStyleMapper {
    @Results(id = "VectorStyle", value = {
            @Result(property="id", column="id", id=true),
            @Result(property="layerId", column="layer_id"),
            @Result(property="type", column="type"),
            @Result(property="creator", column="creator"),
            @Result(property="name", column="name"),
            @Result(property="style", column="style"),
            @Result(property="created", column="created", javaType=OffsetDateTime.class),
            @Result(property="updated", column="updated", javaType= OffsetDateTime.class)
    })
    @Select("SELECT * FROM oskari_maplayer_style WHERE id = #{id}")
    VectorStyle getStyleById(long id);

    @ResultMap("VectorStyle")
    @Select("SELECT * FROM oskari_maplayer_style WHERE creator = #{creator}")
    List<VectorStyle> getStylesByUser(@Param("creator") long creator);

    @ResultMap("VectorStyle")
    @Select("SELECT * FROM oskari_maplayer_style WHERE layer_id = #{layerId} AND creator IS NULL")
    List<VectorStyle> getStylesByLayerId(@Param("layerId") int layerId);

    @ResultMap("VectorStyle")
    @Select("SELECT * FROM oskari_maplayer_style WHERE layer_id = #{layerId} AND (creator IS NULL OR creator=#{user})")
    List<VectorStyle> getStyles(@Param("user") long user, @Param("layerId") int layerId);

    @Delete("DELETE FROM oskari_maplayer_style WHERE id = #{id} RETURNING id")
    long deleteStyle(@Param("id") long id);

    @Select("INSERT INTO oskari_maplayer_style"
            + " (layer_id, type, creator, name, style) VALUES"
            + " (#{layerId}, #{type}, #{creator}, #{name}, #{style})"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    long saveStyle(final VectorStyle style);

    @Select("UPDATE oskari_maplayer_style"
            + " SET layer_id = #{layerId}, type = #{type},"
            + " name = #{name} , style = #{style}"
            + " WHERE id = #{id}"
            + " RETURNING id")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    long updateStyle(final VectorStyle style);
}
