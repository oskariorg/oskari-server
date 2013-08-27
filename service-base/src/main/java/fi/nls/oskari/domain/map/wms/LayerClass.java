package fi.nls.oskari.domain.map.wms;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public class LayerClass implements Comparable<LayerClass> {

	private int id;
	
	private Integer parent;
	
	private List<LayerClass> childrens;
	
	private List<Layer> mapLayers;
	
	private String nameFi;
	
	private String nameSv;
	
	private String nameEn;
	
	private boolean mapLayersSelectable;
	
	private String legendImage;
	
	private String dataUrl;
	
	private boolean groupMap;

    private String locale;
	
	public LayerClass() {
		this.mapLayers = new ArrayList<Layer>();
		this.childrens = new ArrayList<LayerClass>();
	}
	
	public int compareTo(LayerClass lc) {
		return this.getNameFi().compareTo(lc.getNameFi());
	}

    public String getName(final String language) {
        JSONObject loc = JSONHelper.getJSONObject(JSONHelper.createJSONObject(locale), language);
        return JSONHelper.getStringFromJSON(loc, "name" , "");
    }
    
	public String getNameFi() {
		if (nameFi == null) {
			return "";
		}
		return nameFi;
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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Integer getParent() {
		return parent;
	}
	public void setParent(Integer parent) {
		this.parent = parent;
	}
	public List<LayerClass> getChildren() {
		return childrens;
	}
	
	public List<Layer> getMapLayers() {
		return mapLayers;
	}
	
	public void addChildren(List<LayerClass> addedChildren) {
		childrens.addAll(addedChildren);
	}
	
	public void addChild(LayerClass addedChild) {
		childrens.add(addedChild);
	}
	
	public void addMapLayers(List<Layer> addedMapLayers) {
		mapLayers.addAll(addedMapLayers);
	}
	
	public void addMapLayer(MapLayer addedMapLayer) {
		mapLayers.add(addedMapLayer);
	}

	public boolean isMapLayersSelectable() {
		return mapLayersSelectable;
	}

	public void setMapLayersSelectable(boolean mapLayersSelectable) {
		this.mapLayersSelectable = mapLayersSelectable;
	}
	
	public String getLegendImage() {
		return legendImage;
	}

	public void setLegendImage(String legendImage) {
		this.legendImage = legendImage;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public boolean isGroupMap() {
		return groupMap;
	}

	public void setGroupMap(boolean groupMap) {
		this.groupMap = groupMap;
	}

	public List<LayerClass> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<LayerClass> childrens) {
		this.childrens = childrens;
	}

	public void setMapLayers(List<Layer> mapLayers) {
		this.mapLayers = mapLayers;
	}

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
