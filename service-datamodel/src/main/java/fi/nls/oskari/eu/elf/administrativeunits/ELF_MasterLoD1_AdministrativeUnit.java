package fi.nls.oskari.eu.elf.administrativeunits;

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
import fi.nls.oskari.fe.xml.util.Reference;
import fi.nls.oskari.fe.xml.util.Nillable;

public class ELF_MasterLoD1_AdministrativeUnit
{

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0")
   public static class AdministrativeUnit
   {
      public static final String NS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "AdministrativeUnit");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "country")
      @XmlElement(required = true)
      public CodeType country;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "geometry")
      @XmlElement(required = true)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "inspireId")
      @XmlElement(required = true)
      public Identifier inspireId;
      private java.util.List<GeographicalName> name = new java.util.ArrayList<GeographicalName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalCode")
      @XmlElement(required = true)
      public String nationalCode;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevel")
      @XmlElement(required = true)
      public Reference nationalLevel;
      private java.util.List<java.lang.String> nationalLevelName = new java.util.ArrayList<java.lang.String>();
      private java.util.List<ResidenceOfAuthority> residenceOfAuthority = new java.util.ArrayList<ResidenceOfAuthority>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> boundary = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<A_3_lowerLevelUnit> lowerLevelUnit = new java.util.ArrayList<A_3_lowerLevelUnit>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "upperLevelUnit")
      @XmlElement(required = false)
      public Reference upperLevelUnit;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> condominium = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> administeredBy = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> coAdminister = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0", localName = "SHNcode")
      @XmlElement(required = true)
      public ThematicIdentifier SHNcode;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<Calendar> validFrom;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "name")
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

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevelName")
      @XmlElement(required = false)
      public void setNationalLevelName(
            final java.util.List<java.lang.String> list)
      {
         if (list != null)
         {
            nationalLevelName.addAll(list);
         }
         else
         {
            nationalLevelName.clear();
         }
      }

      @JsonGetter
      public java.util.List<java.lang.String> getNationalLevelName()
      {
         return nationalLevelName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "residenceOfAuthority")
      @XmlElement(required = false)
      public void setResidenceOfAuthority(
            final java.util.List<ResidenceOfAuthority> list)
      {
         if (list != null)
         {
            residenceOfAuthority.addAll(list);
         }
         else
         {
            residenceOfAuthority.clear();
         }
      }

      @JsonGetter
      public java.util.List<ResidenceOfAuthority> getResidenceOfAuthority()
      {
         return residenceOfAuthority;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "boundary")
      @XmlElement(required = false)
      public void setBoundary(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            boundary.addAll(list);
         }
         else
         {
            boundary.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBoundary()
      {
         return boundary;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "lowerLevelUnit")
      @XmlElement(required = false)
      public void setLowerLevelUnit(
            final java.util.List<A_3_lowerLevelUnit> list)
      {
         if (list != null)
         {
            lowerLevelUnit.addAll(list);
         }
         else
         {
            lowerLevelUnit.clear();
         }
      }

      @JsonGetter
      public java.util.List<A_3_lowerLevelUnit> getLowerLevelUnit()
      {
         return lowerLevelUnit;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "condominium")
      @XmlElement(required = false)
      public void setCondominium(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            condominium.addAll(list);
         }
         else
         {
            condominium.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getCondominium()
      {
         return condominium;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "administeredBy")
      @XmlElement(required = false)
      public void setAdministeredBy(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            administeredBy.addAll(list);
         }
         else
         {
            administeredBy.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getAdministeredBy()
      {
         return administeredBy;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "coAdminister")
      @XmlElement(required = false)
      public void setCoAdminister(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            coAdminister.addAll(list);
         }
         else
         {
            coAdminister.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getCoAdminister()
      {
         return coAdminister;
      }
   }

   public static class _name
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
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

   public static class A_1_pronunciation extends Nillable
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

   public static class A_2_residenceOfAuthority extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "residenceOfAuthority");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "ResidenceOfAuthority")
      @XmlElement(required = true)
      public ResidenceOfAuthority ResidenceOfAuthority;
   }

   public static class ResidenceOfAuthority
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "ResidenceOfAuthority");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "name")
      @XmlElement(required = true)
      public GeographicalName name;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
   }

   public static class A_3_lowerLevelUnit extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "lowerLevelUnit");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
   }

   public static class SHNcode
   {
      public static final String NS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "SHNcode");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "ThematicIdentifier")
      @XmlElement(required = true)
      public ThematicIdentifier ThematicIdentifier;
   }

   public static class ThematicIdentifier
   {
      public static final String NS = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3";
      public static final QName QN = new QName(NS, "ThematicIdentifier");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "identifier")
      @XmlElement(required = true)
      public String identifier;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "identifierScheme")
      @XmlElement(required = true)
      public String identifierScheme;
   }
}
