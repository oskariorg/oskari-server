package fi.nls.oskari.isotc211.gmd;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import fi.nls.oskari.fe.xml.util.Reference;
import fi.nls.oskari.fe.xml.util.ReferencePropertyType;
import fi.nls.oskari.isotc211.gco.Distance;

public class MD_ResolutionType {

    static class Referenceable_MD_RepresentativeFraction extends
            Reference {
                
       @JacksonXmlProperty(localName="MD_RepresentativeFraction")
       public MD_RepresentativeFraction representativeFraction;              
    }

    @JacksonXmlProperty
    @XmlElement(required = false)
    public Referenceable_MD_RepresentativeFraction equivalentScale;

    @JacksonXmlProperty
    @XmlElement(required = false)
    // hack to match choice
    public Distance distance;
}
