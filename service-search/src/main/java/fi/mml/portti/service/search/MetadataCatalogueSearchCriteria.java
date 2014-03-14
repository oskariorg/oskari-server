package fi.mml.portti.service.search;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Deprecated, use SearchCriteria.addParam(final String key, final Object value) instead.
 */
@Deprecated
public class MetadataCatalogueSearchCriteria implements Serializable {
	
	private static final long serialVersionUID = -7935284078629412075L;

	private static final String SCALE_REGEX = "\\d:\\d+";
	
	boolean showOnlyDownloadable = false;
	
	String name ="";
	String type ="";
	String InspireTheme ="";
	String areaId ="";
	String responsibility ="";
	
	String metaInfo ="";
	boolean metaInfoTargetDescription = false;
	boolean metaInfoTargetHistory = false;
	
	String topicClass ="";
	String keyWord ="";
	String rule ="";
	String serviceType ="";
	String searchType ="";
	String resourceId ="";
	
	String timeStart ="";
	String timeEnd ="";
	
	String sampleResolutionMin ="";
	String sampleResolutionMax ="";
	
	boolean scaleSelected = false;
	String scaleMin ="1:500";
	String scaleMax ="1:12000000";
	String scaleMinDenominator ="500";
	String scaleMaxDenominator ="12000000";
	
	String bbox ="";
	
	String accessConstraints ="";
	String securityClassification ="";
	String otherConstraints ="";
	String accessConditions ="";
	
	String conformanceDegree ="";
	String conformanceDateStart ="";
	String conformanceDateEnd ="";
	String conformanceDateType ="";
	
	/* for example, if scale is 1:40000, denominator is 40000 */
	private String getScaleDenominator(String scale) {
		if (scale == null || "".equals(scale)) {
			return "";
		}
		
		return scale.substring(scale.indexOf(":") + 1);
	}
	
	private boolean checkDateFormat(String inputDate) {
		DateFormat acceptedFormat = new SimpleDateFormat("dd.MM.yyyy");
		
		try {
			acceptedFormat.parse(inputDate);
		} catch (ParseException e) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		String scaleString = null;
		
		if (scaleSelected) {
			scaleString = stringVariableToString(scaleMax, "scaleMax") 
			+ stringVariableToString(scaleMaxDenominator, "scaleMaxDenominator") 
			+ stringVariableToString(scaleMin, "scaleMin") 
			+ stringVariableToString(scaleMinDenominator, "scaleMinDenominator");
		} else {
			scaleString = "";
		}
		
		return " MetadataCatalogueSearchCriteria [" + stringVariableToString(InspireTheme, "InspireTheme") 
		+ stringVariableToString(accessConditions, "accessConditions") 
		+ stringVariableToString(accessConstraints, "accessConstraints") 
		+ stringVariableToString(areaId, "areaId") 
		+ stringVariableToString(bbox, "bbox") 
		+ stringVariableToString(conformanceDateEnd, "conformanceDateEnd") 
		+ stringVariableToString(conformanceDateStart, "conformanceDateStart") 
		+ stringVariableToString(conformanceDateType, "conformanceDateType") 
		+ stringVariableToString(conformanceDegree, "conformanceDegree") 
		+ stringVariableToString(keyWord, "keyWord") 
		+ stringVariableToString(metaInfo, "metaInfo") 
		+ booleanVariableToString(metaInfoTargetDescription, "metaInfoTargetDescription") 
		+ booleanVariableToString(metaInfoTargetHistory, "metaInfoTargetHistory")
		+ stringVariableToString(name, "name") 
		+ stringVariableToString(otherConstraints, "otherConstraints") 
		+ stringVariableToString(resourceId, "resourceId") 
		+ stringVariableToString(responsibility, "responsibility") 
		+ stringVariableToString(rule, "rule") 
		+ stringVariableToString(sampleResolutionMax, "sampleResolutionMax") 
		+ stringVariableToString(sampleResolutionMin, "sampleResolutionMin") 
		+ scaleString
		+ booleanVariableToString(scaleSelected, "scaleSelected") 
		+ stringVariableToString(searchType, "searchType") 
		+ stringVariableToString(securityClassification, "securityClassification") 
		+ stringVariableToString(serviceType, "serviceType") 
		+ booleanVariableToString(showOnlyDownloadable, "showOnlyDownloadable") 
		+ stringVariableToString(timeEnd, "timeEnd") 
		+ stringVariableToString(timeStart, "timeStart") 
		+ stringVariableToString(topicClass, "topicClass") 
		+ stringVariableToString(type, "type") + "]"; 
	}
	
	private String stringVariableToString(String variableValue, String variableLabel) {
		if (variableValue == null || "".equals(variableValue)) {
			return "";
		}
		
		return ", " + variableLabel + "=" + variableValue;
	}
	
	private String booleanVariableToString(boolean variableValue, String variableLabel) {
		if (variableValue) {
			return ", " + variableLabel + "=" + variableValue;
		}
		
		return "";
	}

	public boolean isShowOnlyDownloadable() {
		return showOnlyDownloadable;
	}
	public void setShowOnlyDownloadable(boolean showOnlyDownloadable) {
		this.showOnlyDownloadable = showOnlyDownloadable;
	}
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getInspireTheme() {
		return InspireTheme;
	}
	public void setInspireTheme(String inspireTheme) {
		InspireTheme = inspireTheme;
	}
	public String getAreaId() {
		return areaId;
	}
	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}
	public String getResponsibility() {
		return responsibility;
	}
	public void setResponsibility(String responsibility) {
		this.responsibility = responsibility;
	}
	public String getMetaInfo() {
		return metaInfo;
	}
	public void setMetaInfo(String metaInfo) {
		this.metaInfo = metaInfo;
	}	
	public boolean isMetaInfoTargetDescription() {
		return metaInfoTargetDescription;
	}
	public void setMetaInfoTargetDescription(boolean metaInfoTargetDescription) {
		this.metaInfoTargetDescription = metaInfoTargetDescription;
	}
	public boolean isMetaInfoTargetHistory() {
		return metaInfoTargetHistory;
	}
	public void setMetaInfoTargetHistory(boolean metaInfoTargetHistory) {
		this.metaInfoTargetHistory = metaInfoTargetHistory;
	}
	public String getTopicClass() {
		return topicClass;
	}
	public void setTopicClass(String topicClass) {
		this.topicClass = topicClass;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getTimeStart() {
		return timeStart;
	}
	public void setTimeStart(String timeStart) {
		if (checkDateFormat(timeStart)) {
			this.timeStart = timeStart;
		}
	}
	public String getTimeEnd() {
		return timeEnd;
	}
	public void setTimeEnd(String timeEnd) {
		if (checkDateFormat(timeEnd)) {
			this.timeEnd = timeEnd;
		}
	}	
	public String getSampleResolutionMin() {
		return sampleResolutionMin;
	}
	public void setSampleResolutionMin(String sampleResolutionMin) {
		this.sampleResolutionMin = sampleResolutionMin;
	}
	public String getSampleResolutionMax() {
		return sampleResolutionMax;
	}
	public void setSampleResolutionMax(String sampleResolutionMax) {
		this.sampleResolutionMax = sampleResolutionMax;
	}
	public boolean isScaleSelected() {
		return scaleSelected;
	}
	public void setScaleSelected(boolean scaleSelected) {
		this.scaleSelected = scaleSelected;
	}
	public String getScaleMin() {
		return scaleMin;
	}	
	public void setScaleMin(String scaleMin) {
		if (scaleMin.matches(SCALE_REGEX)) {
			this.scaleMin = scaleMin;
			this.scaleMinDenominator = getScaleDenominator(scaleMin);
		}
	}
	public String getScaleMinDenominator() {
		return scaleMinDenominator;
	}
	public void setScaleMinDenominator(String scaleMinDenominator) {
		try {
			Integer.valueOf(scaleMinDenominator);
		} catch (NumberFormatException x) {
			return;
		}
		
		this.scaleMinDenominator = scaleMinDenominator;
		this.scaleMin = "1:" + scaleMinDenominator;
	}
	public String getScaleMax() {
		return scaleMax;
	}
	public void setScaleMax(String scaleMax) {
		if (scaleMax.matches(SCALE_REGEX)) {
			this.scaleMax = scaleMax;
			this.scaleMaxDenominator = getScaleDenominator(scaleMax);
		}
	}
	public String getScaleMaxDenominator() {
		return scaleMaxDenominator;
	}
	public void setScaleMaxDenominator(String scaleMaxDenominator) {
		try {
			Integer.valueOf(scaleMaxDenominator);
		} catch (NumberFormatException x) {
			return;
		}
		
		this.scaleMaxDenominator = scaleMaxDenominator;
		this.scaleMax = "1:" + scaleMaxDenominator;
	}
	public String getBbox() {
		return bbox;
	}
	public void setBbox(String bbox) {
		if ((bbox != null) && (bbox.indexOf("NaN") >= 0)) {
			return;
		}
		
		this.bbox = bbox;
	}
	public String getAccessConstraints() {
		return accessConstraints;
	}
	public void setAccessConstraints(String accessConstraints) {
		this.accessConstraints = accessConstraints;
	}
	public String getSecurityClassification() {
		return securityClassification;
	}
	public void setSecurityClassification(String securityClassification) {
		this.securityClassification = securityClassification;
	}
	public String getOtherConstraints() {
		return otherConstraints;
	}
	public void setOtherConstraints(String otherConstraints) {
		this.otherConstraints = otherConstraints;
	}
	public String getAccessConditions() {
		return accessConditions;
	}
	public void setAccessConditions(String accessConditions) {
		this.accessConditions = accessConditions;
	}

	public String getConformanceDegree() {
		return conformanceDegree;
	}
	public void setConformanceDegree(String conformanceDegree) {
		this.conformanceDegree = conformanceDegree;
	}
	public String getConformanceDateStart() {
		return conformanceDateStart;
	}
	public void setConformanceDateStart(String conformanceDateStart) {
		if (checkDateFormat(conformanceDateStart)) {
			this.conformanceDateStart = conformanceDateStart;
		}
	}
	public String getConformanceDateEnd() {
		return conformanceDateEnd;
	}
	public void setConformanceDateEnd(String conformanceDateEnd) {
		if (checkDateFormat(conformanceDateEnd)) {
			this.conformanceDateEnd = conformanceDateEnd;
		}
	}
	public String getConformanceDateType() {
		return conformanceDateType;
	}
	public void setConformanceDateType(String conformanceDateType) {
		this.conformanceDateType = conformanceDateType;
	}
}
