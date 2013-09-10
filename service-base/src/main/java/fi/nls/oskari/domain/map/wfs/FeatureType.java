package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.domain.map.JSONLocalizedTitle;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class FeatureType extends JSONLocalizedTitle {
	private int id;
	private QName qname;
	private String schemaLocation;
	private String bboxParameterName ="";
	private WFSService wfsService;
	private List<FeatureParameter> featureParameters;
	
	private String licenseJson;
	
	public String getLicenseJson() {
		return licenseJson;
	}

	public void setLicenseJson(String licenseJson) {
		this.licenseJson = licenseJson;
	}
	
	
	@Override
	public String toString() {
		return "FeatureType [id=" + id + ", qname=" + qname
				+ ", schemaLocation=" + schemaLocation + ", wfsService="
				+ wfsService + ", bboxParameterName=" + bboxParameterName 
				+ ", wfsTitleEn=" + getTitle("en") + ", wfsTitleFi="
				+ getTitle("fi") + ", wfsTitleSv=" + getTitle("sv") + "]";
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public QName getQname() {
		if (qname == null) {
			qname = new QName("", "");
		}
		return qname;
	}

	public void setQname(QName qname) {
		this.qname = qname;
	}

	public String getSchemaLocation() {
		if (schemaLocation == null) {
			return "";
		}
		return schemaLocation;
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	public String getBboxParameterName() {
		return bboxParameterName;
	}

	public void setBboxParameterName(String bboxParameterName) {
		this.bboxParameterName = bboxParameterName;
	}

	public WFSService getWfsService() {
		return wfsService;
	}

	public void setWfsService(WFSService wfsService) {
		this.wfsService = wfsService;
	}

	public List<FeatureParameter> getFeatureParameters() {
		if (featureParameters == null) {
			featureParameters = new ArrayList<FeatureParameter>();
		}
		return featureParameters;
	}

	public void setFeatureParameters(List<FeatureParameter> featureParameters) {
		this.featureParameters = featureParameters;
	}
	
	public FeatureParameter getFilterBboxFeatureParameter() {
		List<FeatureParameter> result = new ArrayList<FeatureParameter>();
		for(FeatureParameter param: featureParameters) {
			if (param.isBboxQueryParameter()) {
				result.add(param);
			}
		}
		
		if (result.size() > 1) {
			String found = "";
			for (FeatureParameter fp: result) {
				found += fp.getQname() + ", ";
			}
			throw new RuntimeException("We have identified a case in which there are multiple FeatureParameters marked " +
					"as BBOX query parameters. Settings cannot be like this, because queries will return wrong results. " +
					"Found parameters are: " + found);
			
		}
		
		if (result.size() == 0) {
			throw new RuntimeException("Cannot find Filter BBox FeatureParameter for '" + getQname() + "' ");
		}
		
		return result.get(0);
	}
}
