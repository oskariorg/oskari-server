package fi.nls.oskari.wfs;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by SMAKINEN on 1.11.2016.
 */
public interface WFSChannelConfigMapper {
    @Select("SELECT c.id, c.wfs_layer_id, c.topic, c.description, c.params_for_search, c.is_default, c.is_address, " +
            "l.name as layerName, l.url, l.srs_name as srs, l.version, l.username, l.password " +
            " FROM oskari_wfs_search_channels c JOIN oskari_maplayer l ON c.wfs_layer_id=l.id where c.id=#{id}")
    @Results({
            @Result(property = "WFSLayerId", column = "wfs_layer_id"),
            @Result(property = "topic", column = "topic"),
            @Result(property = "desc", column = "description"),
            @Result(property = "paramsForSearch", column = "params_for_search"),
            @Result(property = "isDefault", column = "is_default"),
            @Result(property = "isAddress", column = "is_address")
    })
    WFSSearchChannelsConfiguration findChannelById(final long id);

    @Select("SELECT c.id, c.wfs_layer_id, c.topic, c.description, c.params_for_search, c.is_default, c.is_address, " +
            "l.name as layerName, l.url, l.srs_name as srs, l.version, l.username, l.password " +
            " FROM oskari_wfs_search_channels c JOIN oskari_maplayer l ON c.wfs_layer_id=l.id ORDER BY c.id ASC")
    @Results({
            @Result(property = "WFSLayerId", column = "wfs_layer_id"),
            @Result(property = "topic", column = "topic"),
            @Result(property = "desc", column = "description"),
            @Result(property = "paramsForSearch", column = "params_for_search"),
            @Result(property = "isDefault", column = "is_default"),
            @Result(property = "isAddress", column = "is_address")
    })
    List<WFSSearchChannelsConfiguration> findAll();

    @Insert("INSERT INTO oskari_wfs_search_channels (wfs_layer_id, topic, description, params_for_search, is_default, is_address) " +
            "VALUES (#{WFSLayerId}, #{topic}, #{desc}, #{paramsForSearch}, #{isDefault}, #{isAddress})")
    @Options(useGeneratedKeys=true)
    void insert(final WFSSearchChannelsConfiguration config);

    @Update("UPDATE oskari_wfs_search_channels SET " +
            "wfs_layer_id=#{WFSLayerId}, " +
            "topic=#{topic}, " +
            "description=#{desc}, " +
            "params_for_search=#{paramsForSearch}, " +
            "is_default=#{isDefault}, " +
            "is_address=#{isAddress} " +
            "WHERE id=#{id}")
    void update(final WFSSearchChannelsConfiguration config);

    @Delete("DELETE FROM oskari_wfs_search_channels WHERE id=#{id}")
    void delete(long id);
}
