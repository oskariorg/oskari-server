package fi.nls.oskari.service.styles;

import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by Oskari team.
 */
public interface SldStylesMapper {

    @Select("SELECT id, name, sld_style" +
            " FROM portti_wfs_layer_style")
    List<SldStyle> selectAll();

    @Insert("INSERT INTO portti_wfs_layer_style(name, sld_style) VALUES (#{name}, #{sld_style})")
    @Options(useGeneratedKeys=true,  keyProperty = "id")
    void saveStyle(SldStyle style);

}
