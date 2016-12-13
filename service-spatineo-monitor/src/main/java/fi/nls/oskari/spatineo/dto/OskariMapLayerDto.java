package fi.nls.oskari.spatineo.dto;

import com.google.common.base.MoreObjects;
import org.apache.ibatis.annotations.Select;

import java.util.Calendar;
import java.util.List;

/**
 * A data transfer object for interacting with the ``oskari_maplayer`` database table.
 */
public class OskariMapLayerDto {

    public interface Mapper {
        @Select("SELECT id, name, url, updated FROM oskari_maplayer WHERE type = 'wmslayer' ORDER BY id")
        List<OskariMapLayerDto> selectWmsLayers();
        
        @Select("SELECT m.id, w.feature_element as name, m.url, m.updated FROM oskari_maplayer m JOIN portti_wfs_layer w ON w.maplayer_id = m.id;")
        List<OskariMapLayerDto> selectWfsLayers();
    }
    // without this, will throw No constructor found in fi.nls.oskari.spatineo.dto.OskariMapLayerDto matching [java.lang.Integer, java.lang.String, java.lang.String, java.sql.Timestamp]
    public OskariMapLayerDto() {}

    public OskariMapLayerDto(final Long id, final String name, final String url, final Calendar updated) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.updated = updated;
    }

    public Long id;
    public Calendar updated;
    public String name;
    public String url;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(OskariMapLayerDto.class)
                .add("id", id)
                .add("updated", updated)
                .add("name", name)
                .add("url", url)
                .toString();
    }
}
