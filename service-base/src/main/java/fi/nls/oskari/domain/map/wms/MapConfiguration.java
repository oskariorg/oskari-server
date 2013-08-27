package fi.nls.oskari.domain.map.wms;

public class MapConfiguration {
	
	private int id;
	private boolean scala_bar;
	private boolean pan;
	private boolean zoom_bar;
	private boolean map_function;
	private boolean index_map;
	private boolean footer;
	private boolean plane_list;
	private String north;
	private String east;
	private Integer scale;
	private Integer width;
	private Integer height;
	private String projection;
	private String portlet_id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isScala_bar() {
		return scala_bar;
	}
	public void setScala_bar(boolean scalaBar) {
		scala_bar = scalaBar;
	}
	public boolean isPan() {
		return pan;
	}
	public void setPan(boolean pan) {
		this.pan = pan;
	}
	public boolean isZoom_bar() {
		return zoom_bar;
	}
	public void setZoom_bar(boolean zoomBar) {
		zoom_bar = zoomBar;
	}
	public boolean isMap_function() {
		return map_function;
	}
	public void setMap_function(boolean mapFunction) {
		map_function = mapFunction;
	}
	public boolean isIndex_map() {
		return index_map;
	}
	public void setIndex_map(boolean indexMap) {
		index_map = indexMap;
	}
	public boolean isFooter() {
		return footer;
	}
	public void setFooter(boolean footer) {
		this.footer = footer;
	}
	public boolean isPlane_list() {
		return plane_list;
	}
	public void setPlane_list(boolean planeList) {
		plane_list = planeList;
	}
	public String getNorth() {
		return north;
	}
	public void setNorth(String north) {
		this.north = north;
	}
	public String getEast() {
		return east;
	}
	public void setEast(String east) {
		this.east = east;
	}
	public Integer getScale() {
		return scale;
	}
	public void setScale(Integer scale) {
		this.scale = scale;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public String getProjection() {
		return projection;
	}
	public void setProjection(String projection) {
		this.projection = projection;
	}
	public String getPortlet_id() {
		return portlet_id;
	}
	public void setPortlet_id(String portletId) {
		portlet_id = portletId;
	}
	
	
	

}
