package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import java.util.List;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
public class GeographicalName {
    public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
    public static final QName QN = new QName(NS, "NamedPlace");

    public String language;
    public String nativeness;
    public String nameStatus;
    public String sourceOfName;

    public List<PronunciationOfName> pronunciation;
    public List<SpellingOfName> spelling;

    public String grammaticalGender;
    public String grammaticalNumber;
}
