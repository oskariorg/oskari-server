package fi.nls.oskari.csw.dto;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Calendar;

/**
 * A data transfer object for interacting with the ``oskari_maplayer_metadata`` database table.
 */
public class OskariLayerMetadataDto {

    public static interface Mapper {
        @Select("SELECT id, metadataid, wkt, json, ts FROM oskari_maplayer_metadata where metadataid=#{metadataId}")
        public OskariLayerMetadataDto find(final String metadataId);

        @Insert("INSERT INTO oskari_maplayer_metadata (metadataid, wkt, json, ts) VALUES (#{metadataId}, #{wkt}, #{json}, NOW())")
        public void insert(final OskariLayerMetadataDto metadata);

        @Update("UPDATE oskari_maplayer_metadata SET wkt=#{wkt}, json=#{json}, ts=NOW() WHERE id=#{id}")
        public void update(final OskariLayerMetadataDto metadata);
    }

    public OskariLayerMetadataDto() {
    }

    public OskariLayerMetadataDto(final Long id, final Calendar timestamp, final String metadataId,
                                  final String wkt, final String json)
    {
        this.id = id;
        this.timestamp = timestamp;
        this.metadataId = metadataId;
        this.wkt = wkt;
        this.json = json;
    }

    public OskariLayerMetadataDto(final String metadataId, final String wkt, final String json) {
        this(null, null, metadataId, wkt, json);
    }

    public Long id;

    public Calendar timestamp;

    public String metadataId;

    public String wkt;

    public String json;

}
