package fi.nls.oskari.eu.elf.geographicalnames.masterlod1;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class NamedPlaceType extends
        fi.nls.oskari.eu.inspire.gmlas.geographicalnames.NamedPlaceType {

    @JacksonXmlProperty
    public List<GeographicalName> name;

}
