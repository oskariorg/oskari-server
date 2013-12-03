package fi.nls.oskari.domain.map.wms;

import fi.nls.oskari.domain.map.JSONLocalizedName;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.util.PropertyUtil;

import java.util.ArrayList;
import java.util.List;

public class LayerClass extends JSONLocalizedName implements Comparable<LayerClass> {

	private int id;
	
	private Integer parent;
	
	private List<LayerClass> children;
	
	private List<Layer> mapLayers;
	
	private boolean mapLayersSelectable;
	
	private String legendImage;
	
	private String dataUrl;
	
	private boolean groupMap;
	
	public LayerClass() {
		this.mapLayers = new ArrayList<Layer>();
		this.children = new ArrayList<LayerClass>();
	}
	
	public int compareTo(LayerClass lc) {
        String defaultLang = PropertyUtil.getDefaultLanguage();
		return this.getName(defaultLang).compareTo(lc.getName(defaultLang));
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
		return children;
	}
	
	public List<Layer> getMapLayers() {
		return mapLayers;
	}
	
	public void addChildren(List<LayerClass> addedChildren) {
		children.addAll(addedChildren);
	}
	
	public void addChild(LayerClass addedChild) {
		children.add(addedChild);
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

	public void setChildren(List<LayerClass> children) {
		this.children = children;
	}

	public void setMapLayers(List<Layer> mapLayers) {
		this.mapLayers = mapLayers;
	}
}
