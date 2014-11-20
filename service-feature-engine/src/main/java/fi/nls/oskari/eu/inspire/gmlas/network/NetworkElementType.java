package fi.nls.oskari.eu.inspire.gmlas.network;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.eu.inspire.util.Nillable;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:Network:3.2")
public class NetworkElementType {

    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:Network:3.2",localName="beginLifespanVersion")
    public Nillable beginLifespanVersion;
    
    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:Network:3.2",localName="inspireId")
    public Identifier inspireId;
    
    @JacksonXmlProperty(namespace="urn:x-inspire:specification:gmlas:Network:3.2",localName="endLifespanVersion")
    public Nillable endLifespanVersion;
    
    // inNetwork
}
