package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.CodeType;
import fi.nls.oskari.fe.input.jackson.GeometryProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.isotc211.gmd.LocalisedCharacterString;
import fi.nls.oskari.isotc211.gmd.MD_Resolution;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
public class NamedPlaceType {
    @JacksonXmlProperty(isAttribute = true, localName = "id", namespace = "http://www.opengis.net/gml/3.2")
    public String id;

    @XmlElement(required = false, namespace = "http://www.opengis.net/gml/3.2")
    public String identifier;

    @JacksonXmlProperty
    public Nillable beginLifespanVersion;

    @JacksonXmlProperty
    public Nillable endLifespanVersion;

    @JacksonXmlProperty
    public GeometryProperty geometry;

    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "inspireId")
    public Identifier inspireId;

    @JacksonXmlProperty
    public List<MD_Resolution> leastDetailedViewingResolution ;
    
    // localType
    @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd")
    public List<LocalisedCharacterString> localType;
    
    @JacksonXmlProperty
    public List<MD_Resolution> mostDetailedViewingResolution ;
    

    // relatedSpatialObject
    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
    public List<Identifier> relatedSpatialObject ;
    
    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
    public List<CodeType> type;
}
