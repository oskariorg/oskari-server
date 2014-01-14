package fi.nls.oskari.domain.map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Date;

/**
 * Deprecated, use OskariLayer instead!
 */
@Deprecated
public abstract class Layer extends JSONLocalizedNameAndTitle implements Comparable<Layer> {

    private Logger log = LogFactory.getLogger(Layer.class);
	public static final String TYPE_WMS = "wmslayer";
	public static final String TYPE_WFS = "wfslayer";	
	public static final String TYPE_WMTS = "wmtslayer";
    public static final String TYPE_STATS = "statslayer";
    public static final String TYPE_ANALYSIS = "analysislayer";

    private int id;
	private String type;
	
	private Integer opacity;
	private Double minScale;
	private Double maxScale;
	
	/** 
	 * These are layer ids for baselayer which is used at least in
	 * creation of map link. In map link creation we need a way to
	 * represent multiple layers with just one MapLayer object and we
	 * need this special field to store all ids in this case.
	 * This field is not stored in database and works only
	 * as a way to transfer data from portlet to browser
	 */
	private String baseLayerIds;
	
	private Integer layerClassId;
	private String wmsName;
	private String wmsUrl;
    private String simplifiedWmsUrl;
	private String style;
	private String descriptionLink;
	private String legendImage;
	private Integer inspireThemeId;
	private String dataUrl;
	private String metadataUrl;
	private Integer orderNumber;
	
	private String TileMatrixSetId;
	private String TileMatrixSetData;
	
	private Date created = null;
	private Date updated = null;
	
	private String wms_dcp_http;
	private String wms_parameter_layers;
	private String resource_url_scheme;
	private String resource_url_scheme_pattern;
	private String resource_url_client_pattern;
	private Integer resource_daily_max_per_ip;
	
	
	private String xslt;
	private String gfiType;
	
	private String geom;
	
	private String selection_style;
	private String version;
	private Integer epsg;
	
	
	public String getGeom() {
		return geom;
	}

	public void setGeom(String geom) {
		this.geom = geom;
	}

	public String getXslt() {
		return xslt;
	}

	public void setXslt(String xslt) {
		this.xslt = xslt;
	}

	public String getGfiType() {
		return gfiType;
	}

	public void setGfiType(String gfiType) {
		this.gfiType = gfiType;
	}

	private InspireTheme inspireTheme;
	
	public InspireTheme getInspireTheme() {
		return inspireTheme;
	}

	public void setInspireTheme(InspireTheme inspireTheme) {
		this.inspireTheme = inspireTheme;
	}

	public Integer getResource_daily_max_per_ip() {
		return resource_daily_max_per_ip;
	}

	public void setResource_daily_max_per_ip(Integer resource_daily_max_per_ip) {
		this.resource_daily_max_per_ip = resource_daily_max_per_ip;
	}

	public String getWms_dcp_http() {
		return wms_dcp_http;
	}

	public void setWms_dcp_http(String wms_dcp_http) {
		this.wms_dcp_http = wms_dcp_http;
	}

	public String getWms_parameter_layers() {
		return wms_parameter_layers;
	}

	public void setWms_parameter_layers(String wms_parameter_layers) {
		this.wms_parameter_layers = wms_parameter_layers;
	}

	public String getResource_url_scheme() {
		return resource_url_scheme;
	}

	public void setResource_url_scheme(String resource_url_scheme) {
		this.resource_url_scheme = resource_url_scheme;
	}

	public String getResource_url_scheme_pattern() {
		return resource_url_scheme_pattern;
	}

	public void setResource_url_scheme_pattern(String resource_url_scheme_pattern) {
		this.resource_url_scheme_pattern = resource_url_scheme_pattern;
	}

	public String getResource_url_client_pattern() {
		return resource_url_client_pattern;
	}

	public void setResource_url_client_pattern(String resource_url_client_pattern) {
		this.resource_url_client_pattern = resource_url_client_pattern;
	}

	
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public int compareTo(Layer l) {
        String defaultLang = PropertyUtil.getDefaultLanguage();
		return this.getName(defaultLang).compareTo(l.getName(defaultLang));
	}
	
	@Override
	public String toString() {
		String ret =
                "Layer [baseLayerIds=" + baseLayerIds
				+ ", dataUrl=" + dataUrl + ", descriptionLink="
				+ descriptionLink + ", id=" + id + ", inspireThemeId="
				+ inspireThemeId + ", layerClassId=" + layerClassId
				+ ", legendImage=" + legendImage + ", maxScale=" + maxScale
				+ ", metadataUrl=" + metadataUrl + ", minScale=" + minScale;
        for (String locale : PropertyUtil.getSupportedLocales()) {
            String lang = locale.split("_")[0];
            String ucLang = Character.toUpperCase(lang.charAt(0)) + lang.substring(1);
            ret += ", name" + ucLang + "=" + getName(lang);
        }
		ret +=  ", opacity=" + opacity + ", orderNumber=";
        ret += orderNumber + ", style=" + style + ", type=" + type;
		ret += ", wmsName=" + wmsName + ", wmsUrl=" + wmsUrl + "]";
        return ret;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getOpacity() {
		return opacity;
	}
	public void setOpacity(Integer opacity) {
		this.opacity = opacity;
	}
	public Double getMinScale() {
		return minScale;
	}
	public void setMinScale(Double minScale) {
		this.minScale = minScale;
	}
	public Double getMaxScale() {
		return maxScale;
	}
	public void setMaxScale(Double maxScale) {
		this.maxScale = maxScale;
	}
	public String getBaseLayerIds() {
		return baseLayerIds;
	}
	public void setBaseLayerIds(String baseLayerIds) {
		this.baseLayerIds = baseLayerIds;
	}
	public Integer getLayerClassId() {
		return layerClassId;
	}
	public void setLayerClassId(Integer layerClassId) {
		this.layerClassId = layerClassId;
	}
	public String getWmsName() {
		return wmsName;
	}
	public void setWmsName(String wmsName) {
		this.wmsName = wmsName;
	}
	public String getWmsUrl() {
		return wmsUrl;
	}

    /**
     * Returns a simplified version of the wms url. Splits it with comma-character, takes the first one and
     * returns it after removing possible protocol.
     * @return simplified version of wms url or an empty string if there is any problems creating it.
     */
    public String getSimplifiedWmsUrl() {
        if(simplifiedWmsUrl != null) {
            return simplifiedWmsUrl;
        }
        if(wmsUrl == null) {
            return "";
        }
        final String[] splitted = wmsUrl.split("\\s*,\\s*");
        if(splitted == null || splitted.length == 0 ) {
            return "";
        }

        final String protocolSeparator = "://";
        final int protocolIndex = splitted[0].indexOf(protocolSeparator);
        if(protocolIndex == -1) {
            // there was no protocol - weird but possible case
            simplifiedWmsUrl = splitted[0].trim();
        }
        else {
            simplifiedWmsUrl = splitted[0].substring(protocolIndex + protocolSeparator.length()).trim();
        }
        return simplifiedWmsUrl;
    }

	public void setWmsUrl(String wmsUrl) {
		this.wmsUrl = wmsUrl;
        // reset cached simplified url
        this.simplifiedWmsUrl = null;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getDescriptionLink() {
		return descriptionLink;
	}
	public void setDescriptionLink(String descriptionLink) {
		this.descriptionLink = descriptionLink;
	}
	public String getLegendImage() {
		return legendImage;
	}
	public void setLegendImage(String legendImage) {
		this.legendImage = legendImage;
	}
	public Integer getInspireThemeId() {
		return inspireThemeId;
	}
	public void setInspireThemeId(Integer inspireThemeId) {
		this.inspireThemeId = inspireThemeId;
	}
	public String getDataUrl() {
		return dataUrl;
	}
	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}
	public String getMetadataUrl() {
		return metadataUrl;
	}
	public void setMetadataUrl(String metadataUrl) {
		this.metadataUrl = metadataUrl;
	}
	public Integer getOrdernumber() {
		return orderNumber;
	}
	public void setOrdernumber(int ordernumber) {
		this.orderNumber = ordernumber;
	}

	public String getTileMatrixSetId() {
		return TileMatrixSetId;
	}

	public void setTileMatrixSetId(String tileMatrixSetId) {
		TileMatrixSetId = tileMatrixSetId;
	}

	public String getTileMatrixSetData() {
		return TileMatrixSetData;
	}

	public void setTileMatrixSetData(String tileMatrixSetData) {
		TileMatrixSetData = tileMatrixSetData;
	}

    public String getSelection_style() {
        return selection_style;
    }

    public void setSelection_style(String selection_style) {
        this.selection_style = selection_style;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setEpsg(Integer epsg) {
        this.epsg = epsg;
    }

    public Integer getEpsg() {
        return epsg;
    }
}
