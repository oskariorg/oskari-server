package fi.nls.oskari.eu.inspire.geographicalnames;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.isotc211.gmd.LocalisedCharacterString;
import fi.nls.oskari.isotc211.gmd.MD_Resolution;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.net.URI;

/**
 * 
 - URL http://inspire.ec.europa.eu/schemas/gn/3.0/GeographicalNames.xsd -
 * timestamp Fri Dec 12 19:33:58 EET 2014
 */
public class INSPIRE_gn_NamedPlace {

    public static final String TIMESTAMP = "Fri Dec 12 19:33:58 EET 2014";
    public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/gn/3.0/GeographicalNames.xsd";

    @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
    public static class NamedPlace extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "NamedPlace");
        @XmlAttribute(required = true, name = "id")
        public String id;
        @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
        @XmlElement(required = false)
        public BoundingProperty boundedBy;
        @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
        @XmlElement(required = false)
        public LocationProperty location;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "beginLifespanVersion")
        @XmlElement(required = false)
        public NillableType<String> beginLifespanVersion;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "endLifespanVersion")
        @XmlElement(required = false)
        public NillableType<String> endLifespanVersion;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "geometry")
        @XmlElement(required = false)
        public GeometryProperty geometry;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "inspireId")
        @XmlElement(required = false)
        public _inspireId inspireId;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "leastDetailedViewingResolution")
        @XmlElement(required = false)
        public A_1_leastDetailedViewingResolution leastDetailedViewingResolution;
        @XmlElement(required = false)
        public java.util.List<_localType> localType = new java.util.ArrayList<_localType>();
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "mostDetailedViewingResolution")
        @XmlElement(required = false)
        public A_2_mostDetailedViewingResolution mostDetailedViewingResolution;
        @XmlElement(required = false)
        public java.util.List<_name> name = new java.util.ArrayList<_name>();
        @XmlElement(required = false)
        public java.util.List<A_4_relatedSpatialObject> relatedSpatialObject = new java.util.ArrayList<A_4_relatedSpatialObject>();
        @XmlElement(required = false)
        public java.util.List<java.lang.String> type = new java.util.ArrayList<java.lang.String>();

        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "localType")
        public void setLocalType(final _localType obj) {
            if (obj != null) {
                localType.add(obj);
            }
        }

        java.util.List<_localType> getLocalType() {
            return localType;
        }

        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "name")
        public void setName(final _name obj) {
            if (obj != null) {
                name.add(obj);
            }
        }

        java.util.List<_name> getName() {
            return name;
        }

        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "relatedSpatialObject")
        public void setRelatedSpatialObject(final A_4_relatedSpatialObject obj) {
            if (obj != null) {
                relatedSpatialObject.add(obj);
            }
        }

        java.util.List<A_4_relatedSpatialObject> getRelatedSpatialObject() {
            return relatedSpatialObject;
        }

        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "type")
        public void setType(final java.lang.String obj) {
            if (obj != null) {
                type.add(obj);
            }
        }

        java.util.List<java.lang.String> getType() {
            return type;
        }
    }

    public static class _inspireId extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "inspireId");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "Identifier")
        @XmlElement(required = false)
        public Identifier Identifier;
    }

    public static class Identifier extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:BaseTypes:3.2";
        public static final QName QN = new QName(NS, "Identifier");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "localId")
        @XmlElement(required = false)
        public String localId;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "namespace")
        @XmlElement(required = false)
        public String namespace;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "versionId")
        @XmlElement(required = false)
        public NillableType<String> versionId;
    }

    public static class A_1_leastDetailedViewingResolution extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS,
                "leastDetailedViewingResolution");
        @XmlAttribute(required = false, name = "owns")
        public String owns;
        @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "MD_Resolution")
        @XmlElement(required = false)
        public MD_Resolution MD_Resolution;
    }

    public static class _localType extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "localType");
        @XmlAttribute(required = false, name = "uuidref")
        public String uuidref;
        @XmlAttribute(required = false, name = "nilReason")
        public String nilReason;
        @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "LocalisedCharacterString")
        @XmlElement(required = false)
        public LocalisedCharacterString LocalisedCharacterString;
    }

    public static class A_2_mostDetailedViewingResolution extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS,
                "mostDetailedViewingResolution");
        @XmlAttribute(required = false, name = "owns")
        public String owns;
        @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "MD_Resolution")
        @XmlElement(required = false)
        public MD_Resolution MD_Resolution;
    }

    public static class _name extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "name");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
        @XmlElement(required = false)
        public GeographicalName GeographicalName;
    }

    public static class GeographicalName extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "GeographicalName");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "language")
        @XmlElement(required = false)
        public NillableType<String> language;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "nativeness")
        @XmlElement(required = false)
        public NillableType<String> nativeness;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "nameStatus")
        @XmlElement(required = false)
        public NillableType<String> nameStatus;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "sourceOfName")
        @XmlElement(required = false)
        public NillableType<String> sourceOfName;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciation")
        @XmlElement(required = false)
        public A_3_pronunciation pronunciation;
        @XmlElement(required = false)
        public java.util.List<_spelling> spelling = new java.util.ArrayList<_spelling>();
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalGender")
        @XmlElement(required = false)
        public NillableType<String> grammaticalGender;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalNumber")
        @XmlElement(required = false)
        public NillableType<String> grammaticalNumber;

        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "spelling")
        public void setSpelling(final _spelling obj) {
            if (obj != null) {
                spelling.add(obj);
            }
        }

        java.util.List<_spelling> getSpelling() {
            return spelling;
        }
    }

    public static class A_3_pronunciation extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "pronunciation");
        @XmlAttribute(required = false, name = "nilReason")
        public String nilReason;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "PronunciationOfName")
        @XmlElement(required = false)
        public PronunciationOfName PronunciationOfName;
    }

    public static class PronunciationOfName extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "PronunciationOfName");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciationSoundLink")
        @XmlElement(required = false)
        public NillableType<URI> pronunciationSoundLink;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciationIPA")
        @XmlElement(required = false)
        public NillableType<String> pronunciationIPA;
    }

    public static class _spelling extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "spelling");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "SpellingOfName")
        @XmlElement(required = false)
        public SpellingOfName SpellingOfName;
    }

    public static class SpellingOfName extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "SpellingOfName");
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "text")
        @XmlElement(required = false)
        public String text;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "script")
        @XmlElement(required = false)
        public NillableType<String> script;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "transliterationScheme")
        @XmlElement(required = false)
        public NillableType<String> transliterationScheme;
    }

    public static class A_4_relatedSpatialObject extends Nillable {
        public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
        public static final QName QN = new QName(NS, "relatedSpatialObject");
        @XmlAttribute(required = false, name = "nilReason")
        public String nilReason;
        @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "Identifier")
        @XmlElement(required = false)
        public Identifier Identifier;
    }
}
