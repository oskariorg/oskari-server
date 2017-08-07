package org.oskari.spatineo.monitor.backendstatus;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

public interface BackendStatusMapper {
    
    @Insert("INSERT INTO oskari_backendstatus " +
            " (ts, maplayer_id, status, statusmessage, infourl) " +
            " VALUES (NOW(), #{mapLayerId}, #{status}, #{statusMessage}, #{infoUrl})")
    void saveStatus(final BackendStatus status);

    @Update("TRUNCATE oskari_backendstatus")
    void truncateStatusTable();

}
