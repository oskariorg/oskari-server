package fi.nls.oskari.wfs;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by SMAKINEN on 1.11.2016.
 */
public interface WFSChannelConfigMapper {
    @Select("SELECT * FROM oskari_wfs_search_channels where id=#{id}")
    @Results({
            @Result(property = "WFSLayerId", column = "wfs_layer_id"),
            @Result(property = "topic", column = "topic"),
            @Result(property = "desc", column = "description"),
            @Result(property = "paramsForSearch", column = "params_for_search"),
            @Result(property = "isDefault", column = "is_default"),
            @Result(property = "isAddress", column = "is_address")
    })
    WFSSearchChannelsConfiguration findChannelById(final long id);

    @Select("SELECT * FROM oskari_wfs_search_channels ORDER BY id ASC")
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
    void insert(final WFSSearchChannelsConfiguration metadata);

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
