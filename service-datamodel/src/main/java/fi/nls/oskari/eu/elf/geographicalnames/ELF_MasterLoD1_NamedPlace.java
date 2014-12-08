package fi.nls.oskari.eu.elf.geographicalnames;

import java.net.URI;
import fi.nls.oskari.fe.gml.util.CodeType;
import fi.nls.oskari.isotc211.gco.Distance;
import fi.nls.oskari.isotc211.gmd.LocalisedCharacterString;
import javax.xml.bind.annotation.XmlElement;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.bind.annotation.XmlAttribute;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.Calendar;
import java.math.BigInteger;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.isotc211.gmd.MD_Resolution;
import java.util.List;
import fi.nls.oskari.fe.xml.util.Reference;

public class ELF_MasterLoD1_NamedPlace
{

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0")
   public static class NamedPlace
   {
      public static final String NS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0";
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
      public NillableType<Calendar> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "geometry")
      @XmlElement(required = true)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "inspireId")
      @XmlElement(required = true)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "leastDetailedViewingResolution")
      @XmlElement(required = false)
      public List<MD_Resolution> leastDetailedViewingResolution;
      private java.util.List<java.lang.String> localType = new java.util.ArrayList<java.lang.String>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "mostDetailedViewingResolution")
      @XmlElement(required = false)
      public List<MD_Resolution> mostDetailedViewingResolution;
      private java.util.List<GeographicalName> name = new java.util.ArrayList<GeographicalName>();
      private java.util.List<Identifier> relatedSpatialObject = new java.util.ArrayList<Identifier>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> type = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "localType")
      @XmlElement(required = false)
      public void setLocalType(final java.util.List<java.lang.String> list)
      {
         if (list != null)
         {
            localType.addAll(list);
         }
         else
         {
            localType.clear();
         }
      }

      @JsonGetter
      public java.util.List<java.lang.String> getLocalType()
      {
         return localType;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "name")
      @XmlElement(required = true)
      public void setName(final java.util.List<GeographicalName> list)
      {
         if (list != null)
         {
            name.addAll(list);
         }
         else
         {
            name.clear();
         }
      }

      @JsonGetter
      public java.util.List<GeographicalName> getName()
      {
         return name;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "relatedSpatialObject")
      @XmlElement(required = false)
      public void setRelatedSpatialObject(
            final java.util.List<Identifier> list)
      {
         if (list != null)
         {
            relatedSpatialObject.addAll(list);
         }
         else
         {
            relatedSpatialObject.clear();
         }
      }

      @JsonGetter
      public java.util.List<Identifier> getRelatedSpatialObject()
      {
         return relatedSpatialObject;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "type")
      @XmlElement(required = false)
      public void setType(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            type.addAll(list);
         }
         else
         {
            type.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getType()
      {
         return type;
      }
   }

   public static class A_1_leastDetailedViewingResolution extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS,
            "leastDetailedViewingResolution");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "MD_Resolution")
      @XmlElement(required = true)
      public MD_Resolution MD_Resolution;
   }

   public static class A_2_mostDetailedViewingResolution extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS,
            "mostDetailedViewingResolution");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "MD_Resolution")
      @XmlElement(required = true)
      public MD_Resolution MD_Resolution;
   }

   public static class _name
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "name");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = true)
      public GeographicalName GeographicalName;
   }

   public static class GeographicalName
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "GeographicalName");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "language")
      @XmlElement(required = false)
      public NillableType<String> language;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "nativeness")
      @XmlElement(required = false)
      public Reference nativeness;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "nameStatus")
      @XmlElement(required = false)
      public Reference nameStatus;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "sourceOfName")
      @XmlElement(required = false)
      public NillableType<String> sourceOfName;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciation")
      @XmlElement(required = false)
      public PronunciationOfName pronunciation;
      private java.util.List<SpellingOfName> spelling = new java.util.ArrayList<SpellingOfName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalGender")
      @XmlElement(required = false)
      public Reference grammaticalGender;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalNumber")
      @XmlElement(required = false)
      public Reference grammaticalNumber;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "spelling")
      @XmlElement(required = true)
      public void setSpelling(final java.util.List<SpellingOfName> list)
      {
         if (list != null)
         {
            spelling.addAll(list);
         }
         else
         {
            spelling.clear();
         }
      }

      @JsonGetter
      public java.util.List<SpellingOfName> getSpelling()
      {
         return spelling;
      }
   }

   public static class A_3_pronunciation extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "pronunciation");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "PronunciationOfName")
      @XmlElement(required = true)
      public PronunciationOfName PronunciationOfName;
   }

   public static class PronunciationOfName
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "PronunciationOfName");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciationSoundLink")
      @XmlElement(required = false)
      public NillableType<URI> pronunciationSoundLink;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciationIPA")
      @XmlElement(required = false)
      public NillableType<String> pronunciationIPA;
   }

   public static class _spelling
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "spelling");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "SpellingOfName")
      @XmlElement(required = true)
      public SpellingOfName SpellingOfName;
   }

   public static class SpellingOfName
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "SpellingOfName");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "text")
      @XmlElement(required = true)
      public String text;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "script")
      @XmlElement(required = false)
      public NillableType<String> script;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "transliterationScheme")
      @XmlElement(required = false)
      public NillableType<String> transliterationScheme;
   }

   public static class A_4_relatedSpatialObject extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "relatedSpatialObject");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base/3.3rc3/", localName = "Identifier")
      @XmlElement(required = true)
      public Identifier Identifier;
   }
}
