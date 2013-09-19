package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.domain.map.JSONLocalizedTitle;
import fi.nls.oskari.util.PropertyUtil;

public class SelectedFeatureParameter extends JSONLocalizedTitle {
	private int id;
	private int selectedFeatureTypeId;
	private FeatureParameter featureParameter;
	private WFSLayerView wfsLayerView;
	
	@Override
	public String toString() {
		String ret = "SelectedFeatureParameter [featureParameter=" + featureParameter
				+ ", id=" + id + ", selectedFeatureTypeId=" + selectedFeatureTypeId;
                for (String locale : PropertyUtil.getSupportedLocales()) {
                   String lang = locale.split("_")[0];
                   ret += ", title" + Character.toUpperCase(lang.charAt(0)) + lang.substring(1) + "=" + getTitle(lang);
                }
        ret += ", wfsLayerView=" + wfsLayerView + "]";
        return ret;
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
