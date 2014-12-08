package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "PronunciationOfName")
public class PronunciationOfName {
    public String PronunciationOfNameType;
    public String pronunciationIPA;
}
