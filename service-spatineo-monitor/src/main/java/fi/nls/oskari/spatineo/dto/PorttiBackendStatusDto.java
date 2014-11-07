package fi.nls.oskari.spatineo.dto;

import org.apache.ibatis.annotations.Insert;

import java.util.Calendar;

/**
 * A data transfer object for interacting with the ``portti_backendstatus`` database table.
 */
public class PorttiBackendStatusDto {

    public static interface Mapper {
        @Insert("INSERT INTO portti_backendstatus (ts, maplayer_id, status, statusmessage, infourl) VALUES (NOW(), #{mapLayerId}, #{status}, #{statusMessage}, #{infoUrl})")
        public void saveStatus(final PorttiBackendStatusDto status);
    }

    public PorttiBackendStatusDto() {
    }

    public PorttiBackendStatusDto(final Long id, final Calendar timestamp, final Long mapLayerId,
                                  final String status, final String statusMessage, final String infoUrl,
                                  final String statusJson)
    {
        this.id = id;
        this.timestamp = timestamp;
        this.mapLayerId = mapLayerId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.infoUrl = infoUrl;
        this.statusJson = statusJson;
    }

    public PorttiBackendStatusDto(final Long mapLayerId, final String status, final String statusMessage, final String infoUrl) {
        this(null, null, mapLayerId, status, statusMessage, infoUrl, null);
    }

    public Long id;

    public Calendar timestamp;

    public Long mapLayerId;

    public String status;

    public String statusMessage;

    public String infoUrl;

    public String statusJson;

}
