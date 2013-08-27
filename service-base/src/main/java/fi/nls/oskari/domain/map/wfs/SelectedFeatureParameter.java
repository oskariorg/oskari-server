package fi.nls.oskari.domain.map.wfs;

public class SelectedFeatureParameter {
	private int id;
	private int selectedFeatureTypeId;
	private String titleFi;
	private String titleSv;
	private String titleEn;
	private FeatureParameter featureParameter;
	private WFSLayerView wfsLayerView;
	
	@Override
	public String toString() {
		return "SelectedFeatureParameter [featureParameter=" + featureParameter
				+ ", id=" + id + ", selectedFeatureTypeId="
				+ selectedFeatureTypeId + ", titleEn=" + titleEn + ", titleFi="
				+ titleFi + ", titleSv=" + titleSv + ", wfsLayerView="
				+ wfsLayerView + "]";
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSelectedFeatureTypeId() {
		return selectedFeatureTypeId;
	}
	public void setSelectedFeatureTypeId(int selectedFeatureTypeId) {
		this.selectedFeatureTypeId = selectedFeatureTypeId;
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
	public FeatureParameter getFeatureParameter() {
		return featureParameter;
	}
	public void setFeatureParameter(FeatureParameter featureParameter) {
		this.featureParameter = featureParameter;
	}
	public WFSLayerView getWfsLayerView() {
		return wfsLayerView;
	}
	public void setWfsLayerView(WFSLayerView wfsLayerView) {
		this.wfsLayerView = wfsLayerView;
	}
}
