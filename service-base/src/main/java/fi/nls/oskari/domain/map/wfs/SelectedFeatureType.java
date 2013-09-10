package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.domain.map.JSONLocalizedTitle;

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
		return "SelectedFeatureType [featureType=" + featureType + ", id=" + id
				+ ", maxNumDisplayedItems=" + maxNumDisplayedItems
				+ ", titleEn=" + getTitle("en") + ", titleFi=" + getTitle("fi")
				+ ", titleSv=" + getTitle("sv") + ", wfsLayerId=" + wfsLayerId + "]";
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
