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

    public static class DataQuality {
        private List<String> reportConformances = new ArrayList<String>();
        private String lineageStatement;

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

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            JSONHelper.putValue(ret, "reportConformances", reportConformances);
            JSONHelper.putValue(ret, "lineageStatement", lineageStatement);
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
                // TODO fix format
                SimpleDateFormat sdf =
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.US
                        );
                JSONHelper.putValue(ret, "date", sdf.format(date));
                JSONHelper.putValue(ret, "dateType", dateType);
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
