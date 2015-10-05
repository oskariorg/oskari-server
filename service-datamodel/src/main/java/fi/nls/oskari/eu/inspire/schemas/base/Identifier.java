package fi.nls.oskari.eu.inspire.schemas.base;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.xml.util.Nillable;

@JacksonXmlRootElement(namespace = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/", localName = "Identifier")
public class Identifier {

    public static class Content {

        public String localId;

        public String namespace;

        /*
         * public static class VersionId {
         * 
         * @JacksonXmlText(value = true) public String value;
         * 
         * @XmlAttribute(name = "nilReason", required = false) public String
         * nilReason;
         * 
         * @XmlAttribute(required = false, name = "nil", namespace =
         * "http://www.w3.org/2001/XMLSchema-instance") public boolean nil; };
         */

        public Nillable versionId;

    }

    @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/", localName = "Identifier")
    public Content identifier;

}
