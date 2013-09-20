package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.domain.map.JSONLocalizedTitle;
import fi.nls.oskari.util.PropertyUtil;

import java.util.ArrayList;
import java.util.List;

public class SelectedFeatureType extends JSONLocalizedTitle {
	private int id;
	private int wfsLayerId;
	private int maxNumDisplayedItems = -1;
	private FeatureType featureType;
	private List<SelectedFeatureParameter> selectedFeatureParameters;
	
	private String licenseJson;
	
	public String getLicenseJson() {
		return licenseJson;
	}

	public void setLicenseJson(String licenseJson) {
		this.licenseJson = licenseJson;
	}
	
	
	@Override
	public String toString() {
        String ret =
		    "SelectedFeatureType [featureType=" + featureType + ", id=" + id
				+ ", maxNumDisplayedItems=" + maxNumDisplayedItems;
        for (String locale : PropertyUtil.getSupportedLocales()) {
            String key = locale.substring(0, locale.indexOf("_"));
            String lang = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            ret += ", title" + lang + "=" + getTitle(key);
        }
        ret +=  ", wfsLayerId=" + wfsLayerId + "]";
        return ret;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWfsLayerId() {
		return wfsLayerId;
	}
	public void setWfsLayerId(int wfsLayerId) {
		this.wfsLayerId = wfsLayerId;
	}

	public int getMaxNumDisplayedItems() {
		return maxNumDisplayedItems;
	}

	public void setMaxNumDisplayedItems(int maxNumDisplayedItems) {
		this.maxNumDisplayedItems = maxNumDisplayedItems;
	}

	public FeatureType getFeatureType() {
		return featureType;
	}

	public void setFeatureType(FeatureType featureType) {
		this.featureType = featureType;
	}

	public List<SelectedFeatureParameter> getSelectedFeatureParameters() {
		if (selectedFeatureParameters == null) {
			selectedFeatureParameters = new ArrayList<SelectedFeatureParameter>();
		}
		return selectedFeatureParameters;
	}

	public void setSelectedFeatureParameters(
			List<SelectedFeatureParameter> selectedFeatureParameters) {
		this.selectedFeatureParameters = selectedFeatureParameters;
	}
}
