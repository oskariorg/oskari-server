package fi.nls.oskari.domain.map.wms;

import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * This class represents WMS layer.
 */
@Deprecated
public class MapLayer extends Layer {
	
	public MapLayer() {
		super.setType(Layer.TYPE_WMS);
	}
	
	public JSONObject toJSON() throws JSONException {
	    JSONObject json = new JSONObject();
	    
	    json.put("dataUrl_uuid", this.getDataUrl());
	    json.put("wmsName", this.getWmsName());
	    json.put("styles", new JSONObject()); //TODO: need fix
	    json.put("descriptionLink", this.getDescriptionLink());
	    json.put("baseLayerId", this.getBaseLayerIds());
	    
	    json.put("orgName", "orgNane"); // TODO: need fix
	    json.put("type", this.getType());
	    json.put("legendImage", this.getLegendImage());
	    json.put("formats", new JSONObject());
	    json.put("isQueryable", false); //TODO: need check
	    json.put("id", this.getId());
	    json.put("minScale", this.getMinScale());
	    json.put("dataUrl", this.getDataUrl());
	    json.put("style", this.getStyle());
	    json.put("updated", this.getUpdated());
	    json.put("created", this.getCreated());
	    
	    json.put("name", this.getName(PropertyUtil.getDefaultLanguage()));
	    
	    json.put("wmsUrl", this.getWmsUrl());
        
	    JSONObject adminJSON = new JSONObject();
	    
	    adminJSON.put("wms_dcp_http", "");
	    adminJSON.put("resource_url_scheme_pattern", "");
	    adminJSON.put("layerType", this.getType());
	    adminJSON.put("wmsName", this.getWmsName());

        JSONObject names = new JSONObject();
        JSONObject titles = new JSONObject();
        JSONArray locales = new JSONArray();

        Set<String> langs = new TreeSet<String>(getLanguages());
        // make sure we have entries for all supported languages just to be nice...
        langs.addAll(Arrays.asList(PropertyUtil.getSupportedLanguages()));

        for (String lang : langs) {
            names.put(lang, getName(lang));
            titles.put(lang, getTitle(lang));
            JSONObject locale = new JSONObject();
            locale.put("lang", lang);
            locale.put("name", getName(lang));
            locale.put("title", getTitle(lang));
            locales.put(locale);
        }

        adminJSON.put("name", names);
        adminJSON.put("title", titles);
        adminJSON.put("locales", locales);
	    
	    adminJSON.put("wms_parameter_layers","");
	    adminJSON.put("inspireTheme", this.getInspireThemeId());
	    adminJSON.put("tileMatrixSetId", "");
	    adminJSON.put("legendImage", this.getLegendImage());
	    adminJSON.put("version", this.getVersion());
        adminJSON.put("selection_style", "");
        adminJSON.put("style", this.getStyle());
        adminJSON.put("dataUrl", this.getDataUrl());
        
        adminJSON.put("epsg", this.getEpsg());
        adminJSON.put("opacity", this.getOpacity());
        adminJSON.put("gfiType", this.getGfiType());
        adminJSON.put("metadataUrl", this.getMetadataUrl());
        adminJSON.put("tileMatrixSetData",this.getTileMatrixSetData());
        adminJSON.put("minScale", this.getMinScale());
        adminJSON.put("maxScale", this.getMaxScale());
        
        adminJSON.put("resource_url_scheme", this.getResource_url_scheme());
        adminJSON.put("resource_daily_max_per_ip", this.getResource_daily_max_per_ip());
        adminJSON.put("descriptionLink",this.getDescriptionLink());
        adminJSON.put("xslt", this.getXslt());
        adminJSON.put("wmsUrl", this.getWmsUrl());
        adminJSON.put("orderNumber", this.getOrdernumber());
	    
	    json.put("admin", adminJSON);

	    return json;
	}
}
