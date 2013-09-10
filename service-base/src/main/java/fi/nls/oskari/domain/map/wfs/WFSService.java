package fi.nls.oskari.domain.map.wfs;

import java.util.ArrayList;
import java.util.List;

public class WFSService extends OGCService {
	private List<FeatureType> featureTypes;
	
	public WFSService() {
	}
	
	public WFSService(String title) {
		super.setTitle("fi", title);
	}
	
	public List<FeatureType> getFeatureTypes() {
		if (featureTypes == null) {
			featureTypes = new ArrayList<FeatureType>();
		}
		return featureTypes;
	}
	public void setFeatureTypes(List<FeatureType> featureTypes) {
		this.featureTypes = featureTypes;
	}
}
