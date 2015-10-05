package fi.nls.oskari.eu.inspire.administrativeunits;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.CodeType;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.xml.util.Reference;
import fi.nls.oskari.isotc211.gmd.LocalisedCharacterString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.net.URI;

/**
 * 
- URL http://inspire.ec.europa.eu/schemas/au/3.0/AdministrativeUnits.xsd
- timestamp Wed Dec 17 13:47:08 EET 2014
 */
public class INSPIRE_au_AdministrativeUnit
{

   public static final String TIMESTAMP = "Wed Dec 17 13:47:08 EET 2014";
   public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/au/3.0/AdministrativeUnits.xsd";

   @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0")
   public static class AdministrativeUnit extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "AdministrativeUnit");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalCode")
      @XmlElement(required = false)
      public String nationalCode;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public _inspireId inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevel")
      @XmlElement(required = false)
      public String nationalLevel;
      @XmlElement(required = false)
      public java.util.List<_nationalLevelName> nationalLevelName = new java.util.ArrayList<_nationalLevelName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "country")
      @XmlElement(required = false)
      public CodeType country;
      @XmlElement(required = false)
      public java.util.List<_name> name = new java.util.ArrayList<_name>();
      @XmlElement(required = false)
      public java.util.List<A_2_residenceOfAuthority> residenceOfAuthority = new java.util.ArrayList<A_2_residenceOfAuthority>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> NUTS = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> condominium = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<A_3_lowerLevelUnit> lowerLevelUnit = new java.util.ArrayList<A_3_lowerLevelUnit>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "upperLevelUnit")
      @XmlElement(required = false)
      public Reference upperLevelUnit;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> administeredBy = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> coAdminister = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> boundary = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevelName")
      public void setNationalLevelName(final _nationalLevelName obj)
      {
         if (obj != null)
         {
            nationalLevelName.add(obj);
         }
      }

      java.util.List<_nationalLevelName> getNationalLevelName()
      {
         return nationalLevelName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "name")
      public void setName(final _name obj)
      {
         if (obj != null)
         {
            name.add(obj);
         }
      }

      java.util.List<_name> getName()
      {
         return name;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "residenceOfAuthority")
      public void setResidenceOfAuthority(final A_2_residenceOfAuthority obj)
      {
         if (obj != null)
         {
            residenceOfAuthority.add(obj);
         }
      }

      java.util.List<A_2_residenceOfAuthority> getResidenceOfAuthority()
      {
         return residenceOfAuthority;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "NUTS")
      public void setNUTS(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            NUTS.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getNUTS()
      {
         return NUTS;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "condominium")
      public void setCondominium(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            condominium.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getCondominium()
      {
         return condominium;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "lowerLevelUnit")
      public void setLowerLevelUnit(final A_3_lowerLevelUnit obj)
      {
         if (obj != null)
         {
            lowerLevelUnit.add(obj);
         }
      }

      java.util.List<A_3_lowerLevelUnit> getLowerLevelUnit()
      {
         return lowerLevelUnit;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "administeredBy")
      public void setAdministeredBy(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            administeredBy.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getAdministeredBy()
      {
         return administeredBy;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "coAdminister")
      public void setCoAdminister(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            coAdminister.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getCoAdminister()
      {
         return coAdminister;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "boundary")
      public void setBoundary(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            boundary.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBoundary()
      {
         return boundary;
      }
   }

   public static class _inspireId extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "inspireId");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", localName = "Identifier")
      @XmlElement(required = false)
      public Identifier Identifier;
   }

   public static class Identifier extends Nillable
   {
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

   public static class _nationalLevelName extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "nationalLevelName");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "LocalisedCharacterString")
      @XmlElement(required = false)
      public LocalisedCharacterString LocalisedCharacterString;
   }

   public static class _name extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "name");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = false)
      public GeographicalName GeographicalName;
   }

   public static class GeographicalName extends Nillable
   {
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
      public A_1_pronunciation pronunciation;
      @XmlElement(required = false)
      public java.util.List<_spelling> spelling = new java.util.ArrayList<_spelling>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalGender")
      @XmlElement(required = false)
      public NillableType<String> grammaticalGender;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalNumber")
      @XmlElement(required = false)
      public NillableType<String> grammaticalNumber;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "spelling")
      public void setSpelling(final _spelling obj)
      {
         if (obj != null)
         {
            spelling.add(obj);
         }
      }

      java.util.List<_spelling> getSpelling()
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
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "PronunciationOfName")
      @XmlElement(required = false)
      public PronunciationOfName PronunciationOfName;
   }

   public static class PronunciationOfName extends Nillable
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

   public static class _spelling extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "spelling");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "SpellingOfName")
      @XmlElement(required = false)
      public SpellingOfName SpellingOfName;
   }

   public static class SpellingOfName extends Nillable
   {
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

   public static class A_2_residenceOfAuthority extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "residenceOfAuthority");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "ResidenceOfAuthority")
      @XmlElement(required = false)
      public ResidenceOfAuthority ResidenceOfAuthority;
   }

   public static class ResidenceOfAuthority extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "ResidenceOfAuthority");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "name")
      @XmlElement(required = false)
      public _name name;
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
}
