package fi.nls.oskari.domain.map.wms;

public class MapConfigurationLayer {
	
	private int id;
	private Integer map_config_id;
	private Integer rank;
	private Integer layer_id;
	private boolean base;
	
	
	
	
	public boolean isBase() {
		return base;
	}
	public void setBase(boolean base) {
		this.base = base;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Integer getMap_config_id() {
		return map_config_id;
	}
	public void setMap_config_id(Integer mapConfigId) {
		map_config_id = mapConfigId;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public Integer getLayer_id() {
		return layer_id;
	}
	public void setLayer_id(Integer layerId) {
		layer_id = layerId;
	}
	
	

}
