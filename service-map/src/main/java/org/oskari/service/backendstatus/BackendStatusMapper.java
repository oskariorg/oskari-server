package org.oskari.service.backendstatus;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import fi.nls.oskari.domain.map.BackendStatus;

public interface BackendStatusMapper {

    static final String getAll = "SELECT * FROM portti_backendstatus";
    static final String getAllAlert = getAll
            + " WHERE NOT status IS NULL"
            + " AND NOT status = 'UNKNOWN'"
            + " AND NOT status = 'OK'";

    static final String truncate = "TRUNCATE portti_backendstatus";
    static final String insert = "INSERT INTO portti_backendstatus"
            + " (maplayer_id, status, statusmessage, infourl)"
            + " VALUES (#{mapLayerId}, #{status}, #{statusMessage}, #{infoUrl})";

    @Select(getAll)
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "ts", column = "ts"),
            @Result(property = "mapLayerId", column = "maplayer_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "statusMessage", column = "statusmessage"),
            @Result(property = "infoUrl", column = "infourl")
    })
    List<BackendStatus> getAll();

    @Select(getAllAlert)
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "ts", column = "ts"),
            @Result(property = "mapLayerId", column = "maplayer_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "statusMessage", column = "statusmessage"),
            @Result(property = "infoUrl", column = "infourl")
    })
    List<BackendStatus> getAllAlert();

    @Insert(insert)
    void saveStatus(final BackendStatus status);

    @Update(truncate)
    void truncate();

}
