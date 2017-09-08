package org.oskari.service.backendstatus;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import fi.nls.oskari.domain.map.BackendStatus;

public interface BackendStatusMapper {

    static final String getAll = "SELECT"
            + " maplayer_id, status, statusmessage, infourl, ts"
            + " FROM oskari_backendstatus";
    static final String getAllAlert = getAll
            + " WHERE status = 'DOWN'"
            + " OR (status = 'ERROR' AND statusmessage NOT LIKE 'Unknown%')";

    static final String truncate = "TRUNCATE oskari_backendstatus";
    static final String insert = "INSERT INTO oskari_backendstatus"
            + " (maplayer_id, status, statusmessage, infourl)"
            + " VALUES (#{mapLayerId}, #{status}, #{statusMessage}, #{infoUrl})";

    @Select(getAll)
    @Results(value = {
            @Result(property = "mapLayerId", column = "maplayer_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "statusMessage", column = "statusmessage"),
            @Result(property = "infoUrl", column = "infourl"),
            @Result(property = "timeStamp", column = "ts")
    })
    List<BackendStatus> getAll();

    @Select(getAllAlert)
    @Results(value = {
            @Result(property = "mapLayerId", column = "maplayer_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "statusMessage", column = "statusmessage"),
            @Result(property = "infoUrl", column = "infourl"),
            @Result(property = "timeStamp", column = "ts")
    })
    List<BackendStatus> getAllAlert();

    @Insert(insert)
    void saveStatus(final BackendStatus status);

    @Update(truncate)
    void truncate();

}
