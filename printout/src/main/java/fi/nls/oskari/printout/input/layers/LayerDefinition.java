package fi.nls.oskari.printout.input.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class LayerDefinition {

	public static class Style {
		String title;
		String legend;
		String name;
		Map<String, ?> styleMap;
		String sld;

		public String getLegend() {
			return legend;
		}

		public String getName() {
			return name;
		}

		public String getSld() {
			return sld;
		}

		public Map<String, ?> getStyleMap() {
			return styleMap;
		}

		public String getTitle() {
			return title;
		}

		public void setLegend(String legend) {
			this.legend = legend;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setSld(String sld) {
			this.sld = sld;
		}

		public void setStyleMap(Map<String, ?> styleMap) {
			this.styleMap = styleMap;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	}

	String wmsname;
	String wmsurl;

	Double minScale;
	Double maxScale;
	Double scale;
	int opacity;
	String style;

	String layerid;

	String layerType;

	boolean isCacheable = true;

	String credentials;

	final ArrayList<LayerDefinition> subLayers = new ArrayList<LayerDefinition>();
	final HashMap<String, Style> styles = new HashMap<String, Style>();

	boolean visibility = true;

	Geometry geom;

	FeatureCollection<SimpleFeatureType, SimpleFeature> data;

	List<Map<String, ?>> tiles;

	/* WMTS */
	String tileMatrixSetId = null;

	public void copyTo(LayerDefinition def) {
		def.setWmsname(wmsname);
		def.setWmsurl(wmsurl);
		def.setMinScale(minScale);
		def.setMaxScale(maxScale);
		def.setOpacity(opacity);
		def.setLayerid(layerid);
		def.setScale(scale);
		def.setLayerType(layerType);
		def.setCacheable(isCacheable);
		def.setCredentials(credentials);
		def.setStyle(style);
		def.setGeom(geom);
		def.getStyles().putAll(styles);
		def.setData(data);
		def.setTiles(tiles);
		def.setTileMatrixSetId(tileMatrixSetId);

		for (LayerDefinition subdef : getSubLayers()) {
			LayerDefinition subCopy = new LayerDefinition();
			subdef.copyTo(subCopy);
			def.getSubLayers().add(subCopy);
		}
	}

	public String getCredentials() {
		return credentials;
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> getData() {
		return data;
	}

	public Geometry getGeom() {
		return geom;
	}

	public String getLayerid() {
		return layerid;
	}

	public String getLayerType() {
		return layerType;
	}

	public Double getMaxScale() {
		return maxScale;
	}

	public Double getMinScale() {
		return minScale;
	}

	public int getOpacity() {
		return opacity;
	}

	public Double getScale() {
		return scale;
	}

	public String getStyle() {
		return style;
	}

	public HashMap<String, Style> getStyles() {
		return styles;
	}

	public ArrayList<LayerDefinition> getSubLayers() {
		return subLayers;
	}

	public String getTileMatrixSetId() {
		return tileMatrixSetId;
	}

	public List<Map<String, ?>> getTiles() {
		return tiles;
	}

	public String getWmsname() {
		return wmsname;
	}

	public String getWmsurl() {
		return wmsurl;
	}

	public boolean isCacheable() {
		return isCacheable;
	}

	public boolean isVisibility() {
		return visibility;
	}

	public void setCacheable(boolean isCacheable) {
		this.isCacheable = isCacheable;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public void setData(FeatureCollection<SimpleFeatureType, SimpleFeature> data) {
		this.data = data;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public void setLayerid(String layerid) {
		this.layerid = layerid;
	}

	public void setLayerType(String layerType) {
		this.layerType = layerType;
	}

	public void setMaxScale(Double maxScale) {
		this.maxScale = maxScale;
	}

	public void setMinScale(Double minScale) {
		this.minScale = minScale;
	}

	public void setOpacity(int opacity) {
		this.opacity = opacity;
	}

	public void setScale(Double scale) {
		this.scale = scale;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setTileMatrixSetId(String tileMatrixSetId) {
		this.tileMatrixSetId = tileMatrixSetId;
	}

	public void setTiles(List<Map<String, ?>> tiles) {
		this.tiles = tiles;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public void setWmsname(String wmsname) {
		this.wmsname = wmsname;
	}

	public void setWmsurl(String wmsurl) {
		this.wmsurl = wmsurl;
	}

}
