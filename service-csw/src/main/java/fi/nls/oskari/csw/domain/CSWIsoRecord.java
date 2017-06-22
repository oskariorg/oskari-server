package fi.nls.oskari.csw.domain;

import com.vividsolutions.jts.geom.GeometryCollection;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by TMIKKOLAINEN on 2.9.2014.
 */
public class CSWIsoRecord {
    private String fileIdentifier;
    private String metadataLanguage;
    private String metadataCharacterSet;
    private List<String> scopeCodes = new ArrayList<String>();
    private List<ResponsibleParty> metadataResponsibleParties = new ArrayList<ResponsibleParty>();
    private Date metadataDateStamp;
    private String metadataStandardName;
    private String metadataStandardVersion;
    private List<Identification> identifications = new ArrayList<Identification>();
    private List<DistributionFormat> distributionFormats = new ArrayList<DistributionFormat>();
    private List<OnlineResource> onlineResources = new ArrayList<OnlineResource>();
    private List<DataQuality> dataQualities = new ArrayList<DataQuality>();
    private URL metadataURL;
    private List<String> referenceSystems = new ArrayList<String>();

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public String getMetadataLanguage() {
        return metadataLanguage;
    }

    public void setMetadataLanguage(String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    public String getMetadataCharacterSet() {
        return metadataCharacterSet;
    }

    public void setMetadataCharacterSet(String metadataCharacterSet) {
        this.metadataCharacterSet = metadataCharacterSet;
    }

    public List<String> getScopeCodes() {
        return scopeCodes;
    }

    public void setScopeCodes(List<String> scopeCodes) {
        this.scopeCodes = scopeCodes;
    }

    public List<ResponsibleParty> getMetadataResponsibleParties() {
        return metadataResponsibleParties;
    }

    public void setMetadataResponsibleParties(List<ResponsibleParty> metadataResponsibleParties) {
        this.metadataResponsibleParties = metadataResponsibleParties;
    }

    public Date getMetadataDateStamp() {
        return metadataDateStamp;
    }

    public void setMetadataDateStamp(Date metadataDateStamp) {
        this.metadataDateStamp = metadataDateStamp;
    }

    public String getMetadataStandardName() {
        return metadataStandardName;
    }

    public void setMetadataStandardName(String metadataStandardName) {
        this.metadataStandardName = metadataStandardName;
    }

    public String getMetadataStandardVersion() {
        return metadataStandardVersion;
    }

    public void setMetadataStandardVersion(String metadataStandardVersion) {
        this.metadataStandardVersion = metadataStandardVersion;
    }

    public List<Identification> getIdentifications() {
        return identifications;
    }

    public void setIdentifications(List<Identification> identifications) {
        this.identifications = identifications;
    }

    public List<DistributionFormat> getDistributionFormats() {
        return distributionFormats;
    }

    public void setDistributionFormats(List<DistributionFormat> distributionFormats) {
        this.distributionFormats = distributionFormats;
    }

    public List<OnlineResource> getOnlineResources() {
        return onlineResources;
    }

    public void setOnlineResources(List<OnlineResource> onlineResources) {
        this.onlineResources = onlineResources;
    }

    public List<DataQuality> getDataQualities() {
        return dataQualities;
    }

    public void setDataQualities(List<DataQuality> dataQualities) {
        this.dataQualities = dataQualities;
    }

    public URL getMetadataURL() {
        return metadataURL;
    }

    public void setMetadataURL(URL metadataURL) {
        this.metadataURL = metadataURL;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        JSONHelper.putValue(ret, "fileIdentifier", fileIdentifier);
        JSONHelper.putValue(ret, "metadataLanguage", metadataLanguage);
        JSONHelper.putValue(ret, "metadataCharacterSet", metadataCharacterSet);
        JSONHelper.putValue(ret, "scopeCodes", scopeCodes);
        JSONHelper.putValue(ret, "referenceSystems", referenceSystems);
        JSONArray arr = new JSONArray();
        for (ResponsibleParty responsibleParty : metadataResponsibleParties) {
            arr.put(responsibleParty.toJSON());
        }
        JSONHelper.putValue(ret, "metadataResponsibleParties", arr);
        JSONHelper.putValue(ret, "metadataDateStamp", metadataDateStamp);
        JSONHelper.putValue(ret, "metadataStandardName", metadataStandardName);
        JSONHelper.putValue(ret, "metadataStandardVersion", metadataStandardVersion);
        JSONHelper.putValue(ret, "metadataURL", metadataURL);
        arr = new JSONArray();
        for (Identification identification : identifications) {
            arr.put(identification.toJSON());
        }
        JSONHelper.putValue(ret, "identifications", arr);

        arr = new JSONArray();
        for (DistributionFormat distributionFormat : distributionFormats) {
            arr.put(distributionFormat.toJSON());
        }
        JSONHelper.putValue(ret, "distributionFormats", arr);

        arr = new JSONArray();
        for (OnlineResource onlineResource : onlineResources) {
            arr.put(onlineResource.toJSON());
        }
        JSONHelper.putValue(ret, "onlineResources", arr);

        arr = new JSONArray();
        for (DataQuality dataQuality : dataQualities) {
            arr.put(dataQuality.toJSON());
        }
        JSONHelper.putValue(ret, "dataQualities", arr);
        return ret;
    }

    public List<String> getReferenceSystems() {
        return referenceSystems;
    }


    public static class DataQualityObject {
        private List<DataQualityNode> dataQualityNodes = new ArrayList<>();

        public List<DataQualityNode> getDataQualityNodes() {
            return dataQualityNodes;
        }

        public void setDataQualityNodes(List<DataQualityNode> dataQualityNodes) {
            this.dataQualityNodes = dataQualityNodes;
        }
    }

    public static class DataQualityValue {
        private String label;
        private String value;

        public DataQualityValue() {}

        public DataQualityValue(String label, String value){
            setLabel(label);
            setValue(value);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class DataQualityNode {
        private String nodeName;
        private DataQualityValue nameOfMeasure;
        private DataQualityValue measureIdentificationCode;
        private DataQualityValue measureIdentificationAuthorization;
        private DataQualityValue measureDescription;
        private DataQualityValue evaluationMethodType;
        private Object evaluationProcecdure; // TODO parse
        private List<DataQualityValue> dateTime;
        private List<DataQualityConformanceResult> conformanceResultList = new ArrayList<>();
        private List<DataQualityQuantitativeResult> quantitativeResultList = new ArrayList<>();

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public DataQualityValue getNameOfMeasure() {
            return nameOfMeasure;
        }

        public void setNameOfMeasure(DataQualityValue nameOfMeasure) {
            this.nameOfMeasure = nameOfMeasure;
        }

        public DataQualityValue getMeasureIdentificationCode() {
            return measureIdentificationCode;
        }

        public void setMeasureIdentificationCode(DataQualityValue measureIdentificationCode) {
            this.measureIdentificationCode = measureIdentificationCode;
        }

        public DataQualityValue getMeasureIdentificationAuthorization() {
            return measureIdentificationAuthorization;
        }

        public void setMeasureIdentificationAuthorization(DataQualityValue measureIdentificationAuthorization) {
            this.measureIdentificationAuthorization = measureIdentificationAuthorization;
        }

        public DataQualityValue getMeasureDescription() {
            return measureDescription;
        }

        public void setMeasureDescription(DataQualityValue measureDescription) {
            this.measureDescription = measureDescription;
        }

        public DataQualityValue getEvaluationMethodType() {
            return evaluationMethodType;
        }

        public void setEvaluationMethodType(DataQualityValue evaluationMethodType) {
            this.evaluationMethodType = evaluationMethodType;
        }

        public Object getEvaluationProcecdure() {
            return evaluationProcecdure;
        }

        public void setEvaluationProcedure(Object evaluationProcecdure) {
            this.evaluationProcecdure = evaluationProcecdure;
        }

        public List<DataQualityValue> getDateTime() {
            return dateTime;
        }

        public void setDateTime(List<DataQualityValue> dateTime) {
            this.dateTime = dateTime;
        }

        public List<DataQualityConformanceResult> getConformanceResultList() {
            return conformanceResultList;
        }

        public List<DataQualityQuantitativeResult> getQuantitativeResultList() {
            return quantitativeResultList;
        }
    }

    public static class DataQualityConformanceResult {
        private Object specification; //TODO parse
        private DataQualityValue explanation;
        private DataQualityValue pass;

        public Object getSpecification() {
            return specification;
        }

        public void setSpecification(Object specification) {
            this.specification = specification;
        }

        public DataQualityValue getExplanation() {
            return explanation;
        }

        public void setExplanation(DataQualityValue explanation) {
            this.explanation = explanation;
        }

        public DataQualityValue getPass() {
            return pass;
        }

        public void setPass(DataQualityValue pass) {
            this.pass = pass;
        }
    }

    public static class DataQualityQuantitativeResult {
        private DataQualityValue valueType;
        private DataQualityValue valueUnit;
        private DataQualityValue errorStatistic;
        private List<DataQualityValue> value;

        public DataQualityValue getValueType() {
            return valueType;
        }

        public void setValueType(DataQualityValue valueType) {
            this.valueType = valueType;
        }

        public DataQualityValue getValueUnit() {
            return valueUnit;
        }

        public void setValueUnit(DataQualityValue valueUnit) {
            this.valueUnit = valueUnit;
        }

        public DataQualityValue getErrorStatistic() {
            return errorStatistic;
        }

        public void setErrorStatistic(DataQualityValue errorStatistic) {
            this.errorStatistic = errorStatistic;
        }

        public List<DataQualityValue> getValue() {
            return value;
        }

        public void setValue(List<DataQualityValue> value) {
            this.value = value;
        }
    }

    public static class DataQuality {

        public static class DataQualityObject {
            private List<String> list = new ArrayList<String>();
            private String pass = "false";

            public List<String> getList() {
                return this.list;
            }
            public void setList(List<String> list) {
                this.list = list; 
            }
            public String getPass() {
                return this.pass;
            }
            public void setPass(String pass) {
                this.pass = pass; 
            }

            public JSONObject toJSON() {
                JSONObject ret = new JSONObject();
                JSONHelper.putValue(ret, "list", list);
                JSONHelper.putValue(ret, "pass", pass);
                return ret;
            }

        }


        private List<String> reportConformances = new ArrayList<String>();
        private String lineageStatement;


        private List<DataQualityObject> absoluteExternalPositionalAccuracyList = new ArrayList<DataQualityObject>();
        private List<DataQualityObject> accuracyOfTimeMeasurementList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> completenessCommissionList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> completenessOmissionList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> conceptualConsistencyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> domainConsistencyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> formatConsistencyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> griddedDataPositionalAccuracyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> nonQuantitativeAttributeAccuracyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> quantitativeAttributeAccuracyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> relativeInternalPositionalAccuracyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> temporalConsistencyList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> temporalValidityList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> thematicClassificationCorrectnessList = new ArrayList<DataQualityObject>(); 
        private List<DataQualityObject> topologicalConsistencyList= new ArrayList<DataQualityObject>(); 


        public List<String> getReportConformances() {
            return reportConformances;
        }

        public void setReportConformances(List<String> reportConformances) {
            this.reportConformances = reportConformances;
        }

        public String getLineageStatement() {
            return lineageStatement;
        }

        public void setLineageStatement(String lineageStatement) {
            this.lineageStatement = lineageStatement;
        }

        public List<DataQualityObject> getAbsoluteExternalPositionalAccuracyList() {
            return absoluteExternalPositionalAccuracyList;
        }

        public void setAbsoluteExternalPositionalAccuracyList(List<DataQualityObject> absoluteExternalPositionalAccuracyList) {
            this.absoluteExternalPositionalAccuracyList = absoluteExternalPositionalAccuracyList;
        }

        public List<DataQualityObject> getAccuracyOfTimeMeasurementList() {
            return accuracyOfTimeMeasurementList;
        }

        public void setAccuracyOfTimeMeasurementList(List<DataQualityObject> accuracyOfTimeMeasurementList) {
            this.accuracyOfTimeMeasurementList = accuracyOfTimeMeasurementList; 
        }
        public List<DataQualityObject> getCompletenessCommissionList() {
            return completenessCommissionList;
        }
        public void setCompletenessCommissionList(List<DataQualityObject> completenessCommissionList) {
            this.completenessCommissionList = completenessCommissionList; 
        }
        public List<DataQualityObject> getCompletenessOmissionList() {
            return completenessOmissionList;
        }
        public void setCompletenessOmissionList(List<DataQualityObject> completenessOmissionList) {
            this.completenessOmissionList = completenessOmissionList; 
        }
        public List<DataQualityObject> getConceptualConsistencyList() {
            return conceptualConsistencyList;
        }
        public void setConceptualConsistencyList(List<DataQualityObject> conceptualConsistencyList) {
            this.conceptualConsistencyList = conceptualConsistencyList; 
        }
        public List<DataQualityObject> getDomainConsistencyList() {
            return domainConsistencyList;
        }
        public void setDomainConsistencyList(List<DataQualityObject> domainConsistencyList) {
            this.domainConsistencyList = domainConsistencyList; 
        }
        public List<DataQualityObject> getFormatConsistencyList() {
            return formatConsistencyList;
        }
        public void setFormatConsistencyList(List<DataQualityObject> formatConsistencyList) {
            this.formatConsistencyList = formatConsistencyList;
        }
        public List<DataQualityObject> getGriddedDataPositionalAccuracyList() {
            return griddedDataPositionalAccuracyList;
        }
        public void setGriddedDataPositionalAccuracyList(List<DataQualityObject> griddedDataPositionalAccuracyList) {
            this.griddedDataPositionalAccuracyList = griddedDataPositionalAccuracyList;
        }
        public List<DataQualityObject> getNonQuantitativeAttributeAccuracyList() {
            return nonQuantitativeAttributeAccuracyList;
        }
        public void setNonQuantitativeAttributeAccuracyList(List<DataQualityObject> nonQuantitativeAttributeAccuracyList) {
            this.nonQuantitativeAttributeAccuracyList = nonQuantitativeAttributeAccuracyList;
        }
        public List<DataQualityObject> getQuantitativeAttributeAccuracyList() {
            return quantitativeAttributeAccuracyList;
        }
        public void setQuantitativeAttributeAccuracyList(List<DataQualityObject> quantitativeAttributeAccuracyList) {
            this.quantitativeAttributeAccuracyList = quantitativeAttributeAccuracyList;
        }
        public List<DataQualityObject> getRelativeInternalPositionalAccuracyList() {
            return relativeInternalPositionalAccuracyList;
        }
        public void setRelativeInternalPositionalAccuracyList(List<DataQualityObject> relativeInternalPositionalAccuracyList) {
            this.relativeInternalPositionalAccuracyList = relativeInternalPositionalAccuracyList;
        }
        public List<DataQualityObject> getTemporalConsistencyList() {
            return temporalConsistencyList;
        }
        public void setTemporalConsistencyList(List<DataQualityObject> temporalConsistencyList) {
            this.temporalConsistencyList = temporalConsistencyList; 
        }
        public List<DataQualityObject> getTemporalValidityList() {
            return temporalValidityList;
        }
        public void setTemporalValidityList(List<DataQualityObject> temporalValidityList) {
            this.temporalValidityList = temporalValidityList;
        }
        public List<DataQualityObject> getThematicClassificationCorrectnessList() {
            return thematicClassificationCorrectnessList;
        }
        public void setThematicClassificationCorrectnessList(List<DataQualityObject> thematicClassificationCorrectnessList) {
            this.thematicClassificationCorrectnessList = thematicClassificationCorrectnessList;
        }
        public List<DataQualityObject> getTopologicalConsistencyList() {
            return topologicalConsistencyList;
        }
        public void setTopologicalConsistencyList(List<DataQualityObject> topologicalConsistencyList) {
            this.topologicalConsistencyList = topologicalConsistencyList;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "reportConformances", reportConformances);
            JSONHelper.putValue(ret, "lineageStatement", lineageStatement);

//            JSONHelper.putValue(ret, "absoluteExternalPositionalAccuracy", absoluteExternalPositionalAccuracy.toJSON());
//            JSONHelper.putValue(ret, "absoluteExternalPositionalAccuracy", absoluteExternalPositionalAccuracy.toJSON());

//            JSONHelper.putValue(ret, "absoluteExternalPositionalAccuracy", absoluteExternalPositionalAccuracy);
            JSONArray arr = new JSONArray();
            for (DataQualityObject dqObject : absoluteExternalPositionalAccuracyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "absoluteExternalPositionalAccuracyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : accuracyOfTimeMeasurementList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "accuracyOfTimeMeasurementList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : completenessCommissionList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "completenessCommissionList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : completenessOmissionList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "completenessOmissionList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : conceptualConsistencyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "conceptualConsistencyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : domainConsistencyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "domainConsistencyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : formatConsistencyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "formatConsistencyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : griddedDataPositionalAccuracyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "griddedDataPositionalAccuracyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : nonQuantitativeAttributeAccuracyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "nonQuantitativeAttributeAccuracyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : quantitativeAttributeAccuracyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "quantitativeAttributeAccuracyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : relativeInternalPositionalAccuracyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "relativeInternalPositionalAccuracyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : temporalConsistencyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "temporalConsistencyList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : temporalValidityList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "temporalValidityList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : thematicClassificationCorrectnessList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "thematicClassificationCorrectnessList", arr);

            arr = new JSONArray();
            for (DataQualityObject dqObject : topologicalConsistencyList) {
                arr.put(dqObject.toJSON());
            }
            JSONHelper.putValue(ret, "topologicalConsistencyList", arr);

            return ret;
        }
    }

    public static class DistributionFormat {
        private String name;
        private String version;

        public DistributionFormat() {

        }

        public DistributionFormat(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "name", name);
            JSONHelper.putValue(ret, "version", version);
            return ret;
        }
    }

    public static class OnlineResource {
        private String name;
        private String url;

        public OnlineResource() {

        }

        public OnlineResource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "name", name);
            JSONHelper.putValue(ret, "url", url);
            return ret;
        }
    }

    public static class ResponsibleParty {
        private String organisationName;
        private List<String> electronicMailAddresses = new ArrayList<String>();

        public String getOrganisationName() {
            return organisationName;
        }

        public void setOrganisationName(String organisationName) {
            this.organisationName = organisationName;
        }

        public List<String> getElectronicMailAddresses() {
            return electronicMailAddresses;
        }

        public void setElectronicMailAddresses(List<String> electronicMailAddresses) {
            this.electronicMailAddresses = electronicMailAddresses;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "organisationName", organisationName);
            JSONHelper.putValue(ret, "electronicMailAddresses", electronicMailAddresses);
            return ret;
        }
    }

    public static abstract class Identification {
        private Citation citation;
        private String abstractText;
        private List<ResponsibleParty> responsibleParties = new ArrayList<ResponsibleParty>();
        private List<BrowseGraphic> browseGraphics = new ArrayList<BrowseGraphic>();
        private List<String> descriptiveKeywords = new ArrayList<String>();
        private List<String> accessConstraints = new ArrayList<String>();
        private List<String> otherConstraints = new ArrayList<String>();
        private List<String> classifications = new ArrayList<String>();
        private List<String> useLimitations = new ArrayList<String>();
        private List<TemporalExtent> temporalExtents = new ArrayList<TemporalExtent>();
        private GeometryCollection extents;
        private List<Envelope> envelopes = new ArrayList<Envelope>();

        public Citation getCitation() {
            return citation;
        }

        public void setCitation(Citation citation) {
            this.citation = citation;
        }

        public String getAbstractText() {
            return abstractText;
        }

        public void setAbstractText(String abstractText) {
            this.abstractText = abstractText;
        }

        public List<ResponsibleParty> getResponsibleParties() {
            return responsibleParties;
        }

        public void setResponsibleParties(List<ResponsibleParty> responsibleParties) {
            this.responsibleParties = responsibleParties;
        }

        public List<BrowseGraphic> getBrowseGraphics() {
            return browseGraphics;
        }

        public void setBrowseGraphics(List<BrowseGraphic> browseGraphics) {
            this.browseGraphics = browseGraphics;
        }

        public List<String> getDescriptiveKeywords() {
            return descriptiveKeywords;
        }

        public void setDescriptiveKeywords(List<String> descriptiveKeywords) {
            this.descriptiveKeywords = descriptiveKeywords;
        }

        public List<String> getAccessConstraints() {
            return accessConstraints;
        }

        public void setAccessConstraints(List<String> accessConstraints) {
            this.accessConstraints = accessConstraints;
        }

        public List<String> getOtherConstraints() {
            return otherConstraints;
        }

        public void setOtherConstraints(List<String> otherConstraints) {
            this.otherConstraints = otherConstraints;
        }

        public List<String> getClassifications() {
            return classifications;
        }

        public void setClassifications(List<String> classifications) {
            this.classifications = classifications;
        }

        public List<String> getUseLimitations() {
            return useLimitations;
        }

        public void setUseLimitations(List<String> useLimitations) {
            this.useLimitations = useLimitations;
        }

        public List<TemporalExtent> getTemporalExtents() {
            return temporalExtents;
        }

        public void setTemporalExtents(List<TemporalExtent> temporalExtents) {
            this.temporalExtents = temporalExtents;
        }

        public GeometryCollection getExtents() {
            return extents;
        }

        public void setExtents(GeometryCollection extents) {
            this.extents = extents;
        }

        public List<Envelope> getEnvelopes() {
            return envelopes;
        }

        public void setEnvelopes(List<Envelope> envelopes) {
            this.envelopes = envelopes;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "citation", citation == null ? null : citation.toJSON());
            JSONHelper.putValue(ret, "abstractText", abstractText);

            JSONArray arr = new JSONArray();
            for (ResponsibleParty responsibleParty : responsibleParties) {
                arr.put(responsibleParty.toJSON());
            }
            JSONHelper.putValue(ret, "responsibleParties", arr);
            arr = new JSONArray();
            for (BrowseGraphic browseGraphic : browseGraphics) {
                arr.put(browseGraphic.toJSON());
            }
            JSONHelper.putValue(ret, "browseGraphics", arr);

            JSONHelper.putValue(ret, "descriptiveKeywords", descriptiveKeywords);
            JSONHelper.putValue(ret, "accessConstraints", accessConstraints);
            JSONHelper.putValue(ret, "otherConstraints", otherConstraints);
            JSONHelper.putValue(ret, "classifications", classifications);
            JSONHelper.putValue(ret, "useLimitations", useLimitations);
            arr = new JSONArray();
            for (TemporalExtent temporalExtent : temporalExtents) {
                arr.put(temporalExtent.toJSON());
            }
            JSONHelper.putValue(ret, "temporalExtents", arr);
            // TODO magic up JSON from extents
            arr = new JSONArray();
            for (Envelope envelope : envelopes) {
                arr.put(envelope.toJSON());
            }
            JSONHelper.putValue(ret, "envelopes", arr);
            return ret;
        }

        public static class Citation {
            private String title;
            private String alternateTitle;
            private DateWithType date;
            private List<ResourceIdentifier> resourceIdentifiers = new ArrayList<ResourceIdentifier>();

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getAlternateTitle() {
                return alternateTitle;
            }

            public void setAlternateTitle(String alternateTitle) {
                this.alternateTitle = alternateTitle;
            }

            public DateWithType getDate() {
                return date;
            }

            public void setDate(DateWithType date) {
                this.date = date;
            }

            public List<ResourceIdentifier> getResourceIdentifiers() {
                return resourceIdentifiers;
            }

            public void setResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers) {
                this.resourceIdentifiers = resourceIdentifiers;
            }

            public static class ResourceIdentifier {
                private String code;
                private String codeSpace;

                public String getCodeSpace() {
                    return codeSpace;
                }

                public void setCodeSpace(String codeSpace) {
                    this.codeSpace = codeSpace;
                }

                public String getCode() {
                    return code;
                }

                public void setCode(String code) {
                    this.code = code;
                }


                public JSONObject toJSON() {
                    JSONObject ret = new JSONObject();
                    JSONHelper.putValue(ret, "code", code);
                    JSONHelper.putValue(ret, "codeSpace", codeSpace);
                    return ret;
                }
            }

            public JSONObject toJSON() {
                JSONObject ret = new JSONObject();
                JSONHelper.putValue(ret, "title", title);
                JSONHelper.putValue(ret, "alternateTitle", alternateTitle);
                JSONHelper.putValue(ret, "date", date == null ? null : date.toJSON());
                JSONArray arr = new JSONArray();
                for (ResourceIdentifier resourceIdentifier : resourceIdentifiers) {
                    arr.put(resourceIdentifier.toJSON());
                }
                JSONHelper.putValue(ret, "resourceIdentifiers", arr);
                return ret;
            }
        }

        public static class DateWithType {
            private Date date;
            private String dateType;
            private String xmlDate;

            public String getXmlDate() { return xmlDate; }

            public void setXmlDate(String xmlDate) {
                this.xmlDate = xmlDate;
            }

            public Date getDate() {
                return date;
            }

            public void setDate(Date date) {
                this.date = date;
            }

            public String getDateType() {
                return dateType;
            }

            public void setDateType(String dateType) {
                this.dateType = dateType;
            }

            public JSONObject toJSON() {
                JSONObject ret = new JSONObject();
                String formattedDate = null;
                if (xmlDate == null || xmlDate.isEmpty()) {
                    try {
                        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        formattedDate = sdf.format(date);
                    }
                    catch (Exception e){
                        //do nothing
                    }
                }
                JSONHelper.putValue(ret, "date", formattedDate != null ? formattedDate : xmlDate);
                JSONHelper.putValue(ret, "dateType", dateType != null ? dateType : "");
                return ret;
            }
        }

        public static class TemporalExtent {
            private String begin, end;

            public TemporalExtent() {

            }

            public TemporalExtent (String begin, String end) {
                this.begin = begin;
                this.end = end;
            }

            public String getBegin() {
                return begin;
            }

            public void setBegin(String begin) {
                this.begin = begin;
            }

            public String getEnd() {
                return end;
            }

            public void setEnd(String end) {
                this.end = end;
            }

            public JSONObject toJSON() {
                JSONObject ret = new JSONObject();
                JSONHelper.putValue(ret, "begin", begin);
                JSONHelper.putValue(ret, "end", end);
                return ret;
            }
        }
    }

    public static class DataIdentification extends Identification {
        private List<String> characterSets = new ArrayList<String>();
        private List<String> languages = new ArrayList<String>();
        private List<String> topicCategories = new ArrayList<String>();
        private List<Integer> spatialResolutions = new ArrayList<Integer>();
        private List<String> spatialRepresentationTypes = new ArrayList<String>();

        public List<String> getCharacterSets() {
            return characterSets;
        }

        public void setCharacterSets(List<String> characterSets) {
            this.characterSets = characterSets;
        }

        public List<String> getLanguages() {
            return languages;
        }

        public void setLanguages(List<String> languages) {
            this.languages = languages;
        }

        public List<String> getTopicCategories() {
            return topicCategories;
        }

        public void setTopicCategories(List<String> topicCategories) {
            this.topicCategories = topicCategories;
        }

        public List<Integer> getSpatialResolutions() {
            return spatialResolutions;
        }

        public void setSpatialResolutions(List<Integer> spatialResolutions) {
            this.spatialResolutions = spatialResolutions;
        }

        public List<String> getSpatialRepresentationTypes() {
            return spatialRepresentationTypes;
        }

        public void setSpatialRepresentationTypes(List<String> spatialRepresentationTypes) {
            this.spatialRepresentationTypes = spatialRepresentationTypes;
        }

        public JSONObject toJSON() {
            JSONObject ret = super.toJSON();
            JSONHelper.putValue(ret, "characterSets", characterSets);
            JSONHelper.putValue(ret, "languages", languages);
            JSONHelper.putValue(ret, "topicCategories", topicCategories);
            JSONHelper.putValue(ret, "spatialResolutions", spatialResolutions);
            JSONHelper.putValue(ret, "spatialRepresentationTypes", spatialRepresentationTypes);
            JSONHelper.putValue(ret, "type", "data");
            return ret;
        }
    }

    public static class ServiceIdentification extends Identification {
        private String serviceType;
        private String serviceTypeVersion;
        private List<String> operatesOn = new ArrayList<String>();

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getServiceTypeVersion() {
            return serviceTypeVersion;
        }

        public void setServiceTypeVersion(String serviceTypeVersion) {
            this.serviceTypeVersion = serviceTypeVersion;
        }

        public List<String> getOperatesOn() {
            return operatesOn;
        }

        public void setOperatesOn(List<String> operatesOn) {
            this.operatesOn = operatesOn;
        }

        public JSONObject toJSON() {
            JSONObject ret = super.toJSON();
            JSONHelper.putValue(ret, "type", "service");
            JSONHelper.putValue(ret, "serviceType", serviceType);
            JSONHelper.putValue(ret, "serviceTypeVersion", serviceTypeVersion);
            JSONHelper.putValue(ret, "operatesOn", operatesOn);
            return ret;
        }
    }

    public static class BrowseGraphic {
        private String fileName;
        private String fileDescription;
        private String fileType;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileDescription() {
            return fileDescription;
        }

        public void setFileDescription(String fileDescription) {
            this.fileDescription = fileDescription;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "fileName", fileName);
            JSONHelper.putValue(ret, "fileDescription", fileDescription);
            JSONHelper.putValue(ret, "fileType", fileType);
            return ret;
        }
    }

    public static class Envelope {
        Double westBoundLongitude;
        Double eastBoundLongitude;
        Double southBoundLatitude;
        Double northBoundLatitude;

        public Double getEastBoundLongitude() {
            return eastBoundLongitude;
        }

        public void setEastBoundLongitude(Double eastBoundLongitude) {
            this.eastBoundLongitude = eastBoundLongitude;
        }

        public Double getNorthBoundLatitude() {
            return northBoundLatitude;
        }

        public void setNorthBoundLatitude(Double northBoundLatitude) {
            this.northBoundLatitude = northBoundLatitude;
        }

        public Double getSouthBoundLatitude() {
            return southBoundLatitude;
        }

        public void setSouthBoundLatitude(Double southBoundLatitude) {
            this.southBoundLatitude = southBoundLatitude;
        }

        public Double getWestBoundLongitude() {
            return westBoundLongitude;
        }

        public void setWestBoundLongitude(Double westBoundLongitude) {
            this.westBoundLongitude = westBoundLongitude;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "westBoundLongitude", westBoundLongitude);
            JSONHelper.putValue(ret, "eastBoundLongitude", eastBoundLongitude);
            JSONHelper.putValue(ret, "southBoundLatitude", southBoundLatitude);
            JSONHelper.putValue(ret, "northBoundLatitude", northBoundLatitude);
            return ret;
        }
    }
}
