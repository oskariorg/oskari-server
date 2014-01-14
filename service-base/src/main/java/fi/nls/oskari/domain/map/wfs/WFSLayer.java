package fi.nls.oskari.domain.map.wfs;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.domain.map.Layer;

@Deprecated
public class WFSLayer extends Layer {
	private List<WFSService> selectedWfsServices;
	private List<SelectedFeatureType> selectedFeatureTypes;
	private String style;
	
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public WFSLayer() {
		super.setType(Layer.TYPE_WFS);
	}
	
	public List<WFSService> getSelectedWfsServices() {
		if (selectedWfsServices == null) {
			selectedWfsServices = new ArrayList<WFSService>();
		}
		return selectedWfsServices;
	}
	public void setSelectedWfsServices(List<WFSService> selectedWfsServices) {
		this.selectedWfsServices = selectedWfsServices;
	}
	public List<SelectedFeatureType> getSelectedFeatureTypes() {
		if (selectedFeatureTypes == null) {
			selectedFeatureTypes = new ArrayList<SelectedFeatureType>();
		}
		return selectedFeatureTypes;
	}
	public void setSelectedFeatureTypes(
			List<SelectedFeatureType> selectedFeatureTypes) {
		this.selectedFeatureTypes = selectedFeatureTypes;
	}
}
