package fi.nls.oskari.domain.map.wfs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

public class FeatureType {
	private int id;
	private QName qname;
	private String wfsTitleFi;
	private String wfsTitleSv;
	private String wfsTitleEn;
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
				+ ", wfsTitleEn=" + wfsTitleEn + ", wfsTitleFi="
				+ wfsTitleFi + ", wfsTitleSv=" + wfsTitleSv + "]";
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
	public String getWfsTitleFi() {
		if (wfsTitleFi == null) {
			return "";
		}
		return wfsTitleFi;
	}
	public void setWfsTitleFi(String wfsTitleFi) {
		this.wfsTitleFi = wfsTitleFi;
	}
	public String getWfsTitleSv() {
		if (wfsTitleSv == null) {
			return "";
		}
		return wfsTitleSv;
	}
	public void setWfsTitleSv(String wfsTitleSv) {
		this.wfsTitleSv = wfsTitleSv;
	}
	public String getWfsTitleEn() {
		if (wfsTitleEn == null) {
			return "";
		}
		return wfsTitleEn;
	}
	public void setWfsTitleEn(String wfsTitleEn) {
		this.wfsTitleEn = wfsTitleEn;
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
