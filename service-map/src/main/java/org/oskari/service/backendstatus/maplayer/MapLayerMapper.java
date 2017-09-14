package org.oskari.service.backendstatus.maplayer;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MapLayerMapper {

    @Select("SELECT id, name, url "
            + "FROM oskari_maplayer "
            + "WHERE type = 'wmslayer' "
            + "ORDER BY id")
    List<MapLayer> selectWMSLayers();

    @Select("SELECT m.id, w.feature_element as name, m.url "
            + "FROM oskari_maplayer m "
            + "JOIN portti_wfs_layer w ON w.maplayer_id = m.id "
            + "ORDER BY m.id")
    List<MapLayer> selectWFSLayers();
    
}
