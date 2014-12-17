package fi.nls.oskari.eu.elf.geographicalnames.masterlod1;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.eu.inspire.gmlas.geographicalnames.GeographicalNameType;


@JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0")
public class GeographicalName extends GeographicalNameType {

    @JacksonXmlProperty
    public Boolean referenceName;
    

}
