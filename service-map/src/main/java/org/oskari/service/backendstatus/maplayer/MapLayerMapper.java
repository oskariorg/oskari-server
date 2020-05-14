package org.oskari.service.backendstatus.maplayer;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MapLayerMapper {

    @Select("SELECT id, name, url "
            + "FROM oskari_maplayer "
            + "WHERE type = 'wmslayer' "
            + "ORDER BY id")
    List<MapLayer> selectWMSLayers();

    @Select("SELECT id, name, url "
            + "FROM oskari_maplayer "
            + "WHERE type = 'wfslayer' "
            + "ORDER BY id")
    List<MapLayer> selectWFSLayers();
    
}
