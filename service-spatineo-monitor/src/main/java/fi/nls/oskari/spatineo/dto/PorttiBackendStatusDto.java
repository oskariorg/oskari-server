package fi.nls.oskari.spatineo.dto;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.Calendar;

/**
 * A data transfer object for interacting with the ``portti_backendstatus`` database table.
 */
public class PorttiBackendStatusDto {

    /**
     * Source field enum
     */
    public enum SourceEnum {
        SPATINEO_SERVAL, SPATINEO_MONITORING
    }
    
    /**
     * Status field enum
     */
    public enum StatusEnum {
        OK, DOWN, MAINTENANCE, UNKNOWN, UNSTABLE;
        
        public static StatusEnum getEnumByNewAPI(String val) {
            switch (val) {
                case "NO_INDICATOR":
                    return UNKNOWN;
                case "NO_ALERTS":
                    return UNKNOWN; // ????
                case "NEW":
                    return OK;
                case "OK":
                    return OK;
                case "WARNING":
                    return UNSTABLE;
                case "ALERT":
                    return DOWN;
                case "INSUFFICIENT_DATA":
                    return UNKNOWN;
            }
            return null;
        }
    }
    
    public static interface Mapper {
        @Insert("INSERT INTO portti_backendstatus " +
                " (ts, maplayer_id, status, statusmessage, infourl, source) " +
                " VALUES (NOW(), #{mapLayerId}, #{status}, #{statusMessage}, #{infoUrl}, #{source})")
        public void saveStatus(final PorttiBackendStatusDto status);

        @Update("TRUNCATE portti_backendstatus")
        public void truncateStatusTable();
    }

    public PorttiBackendStatusDto() {
    }

    public PorttiBackendStatusDto(final Long id, final Calendar timestamp, final Long mapLayerId,
                                  final String status, final String statusMessage, final String infoUrl,
                                  final String statusJson, final String source)
    {
        this.id = id;
        this.timestamp = timestamp;
        this.mapLayerId = mapLayerId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.infoUrl = infoUrl;
        this.statusJson = statusJson;
        this.source = source;
    }

    public PorttiBackendStatusDto(final Long mapLayerId, final String status, final String statusMessage, final String infoUrl, final String source) {
        this(null, null, mapLayerId, status, statusMessage, infoUrl, null, source);
    }

    public Long id;
    public Calendar timestamp;
    public Long mapLayerId;
    public String status;
    public String statusMessage;
    public String infoUrl;
    public String statusJson;
    
    /**
     * Where the status change originated from (f.ex. Spatineo Monitoring API)
     * 
     * Can be free text or use SourceEnum.
     * 
     * @see SourceEnum
     */
    public String source;
}
