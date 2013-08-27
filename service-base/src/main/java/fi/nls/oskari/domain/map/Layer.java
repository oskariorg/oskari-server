package fi.nls.oskari.domain.map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Date;

public abstract class Layer implements Comparable<Layer> {

    private Logger log = LogFactory.getLogger(Layer.class);
	public static final String TYPE_WMS = "wmslayer";
	public static final String TYPE_WFS = "wfslayer";	
	public static final String WMTS_LAYER = "wmtslayer";
    public static final String TYPE_STATS = "statslayer";
    public static final String TYPE_ANALYSIS = "analysislayer";

    public static final String LOCALE_NAME = "name";
    public static final String LOCALE_SUBTITLE = "subtitle";
	
	private int id;
	private String type;
	private String nameFi;	
	private String nameSv;	
	private String nameEn;
	
	private String titleFi;	
	private String titleSv;	
	private String titleEn;
	
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
    private String locale;
	
	
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
		return this.getNameFi().compareTo(l.getNameFi());
	}
	
	@Override
	public String toString() {
		return "Layer [baseLayerIds=" + baseLayerIds
				+ ", dataUrl=" + dataUrl + ", descriptionLink="
				+ descriptionLink + ", id=" + id + ", inspireThemeId="
				+ inspireThemeId + ", layerClassId=" + layerClassId
				+ ", legendImage=" + legendImage + ", maxScale=" + maxScale
				+ ", metadataUrl=" + metadataUrl + ", minScale=" + minScale
				+ ", nameEn=" + nameEn + ", nameFi=" + nameFi + ", nameSv="
				+ nameSv + ", opacity=" + opacity + ", orderNumber="
				+ orderNumber + ", style=" + style + ", type=" + type
				+ ", wmsName=" + wmsName + ", wmsUrl=" + wmsUrl + "]";
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
	public String getNameFi() {
		if (nameFi == null) {
			return "";
		}
		return nameFi;
	}
	
	public String getName(final String language) {
        return getLocalizedValue(language, LOCALE_NAME);
	}
    
    public String getTitle(final String language) {
        return getLocalizedValue(language, LOCALE_SUBTITLE);
    }

    private String getLocalizedValue(String language, String key) {
        try {
            JSONObject loc = JSONHelper.getJSONObject(JSONHelper.createJSONObject(locale), language);
            return JSONHelper.getStringFromJSON(loc, key , "");
        } catch(Exception ex) {
            log.error("Couldn't get", key, "from", locale, "for language", language, "- layerId:", getId(), "- name:", getNameFi());
        }
        return "";
    }
	
	public void setNameFi(String nameFi) {
		this.nameFi = nameFi;
	}
	public String getNameSv() {
		return nameSv;
	}
	public void setNameSv(String nameSv) {
		this.nameSv = nameSv;
	}
	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
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
	public void setWmsUrl(String wmsUrl) {
		this.wmsUrl = wmsUrl;
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

	public String getTitleFi() {
		return titleFi;
	}

	public void setTitleFi(String titleFi) {
		this.titleFi = titleFi;
	}

	public String getTitleSv() {
		return titleSv;
	}

	public void setTitleSv(String titleSv) {
		this.titleSv = titleSv;
	}

	public String getTitleEn() {
		return titleEn;
	}

	public void setTitleEn(String titleEn) {
		this.titleEn = titleEn;
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

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
