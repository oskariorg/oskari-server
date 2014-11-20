package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.eu.inspire.util.GeometryProperty;
import fi.nls.oskari.eu.inspire.util.Nillable;

@JacksonXmlRootElement(namespace="urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
public class NamedPlaceType {
    @JacksonXmlProperty(isAttribute = true, localName = "id", namespace = "http://www.opengis.net/gml/3.2")
    public String id;
    
    @XmlElement(required = false, namespace = "http://www.opengis.net/gml/3.2")
    public String identifier;
    
    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:GeographicalNames:3.0",localName="beginLifespanVersion")
    public Nillable beginLifespanVersion;
    
    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:GeographicalNames:3.0",localName="geometry")
    public GeometryProperty geometry ;
    
    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:BaseTypes:3.2",localName="inspireId")
    public Identifier inspireId;
    
    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:GeographicalNames:3.0",localName="endLifespanVersion")
    public Nillable endLifespanVersion;
    
}
