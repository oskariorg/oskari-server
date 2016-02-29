package fi.nls.oskari.eu.elf.protectedsites;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.xml.util.Reference;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.net.URI;

/**
 * 
- URL http://elfserver.kartverket.no/schemas/elf1.0/LoD1_ProtectedSites.xsd
- timestamp Wed Dec 17 10:59:19 EET 2014
 */
public class ELF_MasterLoD1_ProtectedSite
{

   public static final String TIMESTAMP = "Wed Dec 17 10:59:19 EET 2014";
   public static final String SCHEMASOURCE = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_ProtectedSites.xsd";

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/ProtectedSites/MasterLoD1/1.0")
   public static class ProtectedSite extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/ProtectedSites/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "ProtectedSite");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "inspireID")
      @XmlElement(required = false)
      public Identifier inspireID;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "legalFoundationDate")
      @XmlElement(required = false)
      public NillableType<String> legalFoundationDate;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "legalFoundationDocument")
      @XmlElement(required = false)
      public A_1_legalFoundationDocument legalFoundationDocument;
      @XmlElement(required = false)
      public java.util.List<A_2_siteDesignation> siteDesignation = new java.util.ArrayList<A_2_siteDesignation>();
      @XmlElement(required = false)
      public java.util.List<A_3_siteName> siteName = new java.util.ArrayList<A_3_siteName>();
      @XmlElement(required = false)
      public java.util.List<java.lang.String> siteProtectionClassification = new java.util.ArrayList<java.lang.String>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/ProtectedSites/MasterLoD1/1.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/ProtectedSites/MasterLoD1/1.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "siteDesignation")
      public void setSiteDesignation(final A_2_siteDesignation obj)
      {
         if (obj != null)
         {
            siteDesignation.add(obj);
         }
      }

      java.util.List<A_2_siteDesignation> getSiteDesignation()
      {
         return siteDesignation;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "siteName")
      public void setSiteName(final A_3_siteName obj)
      {
         if (obj != null)
         {
            siteName.add(obj);
         }
      }

      java.util.List<A_3_siteName> getSiteName()
      {
         return siteName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "siteProtectionClassification")
      public void setSiteProtectionClassification(final java.lang.String obj)
      {
         if (obj != null)
         {
            siteProtectionClassification.add(obj);
         }
      }

      java.util.List<java.lang.String> getSiteProtectionClassification()
      {
         return siteProtectionClassification;
      }
   }

   public static class A_1_legalFoundationDocument extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0";
      public static final QName QN = new QName(NS, "legalFoundationDocument");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Citation")
      @XmlElement(required = false)
      public CI_Citation CI_Citation;
   }

   public static class CI_Citation extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_Citation");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "title")
      @XmlElement(required = false)
      public _title title;
      @XmlElement(required = false)
      public java.util.List<_alternateTitle> alternateTitle = new java.util.ArrayList<_alternateTitle>();
      @XmlElement(required = false)
      public java.util.List<_date> date = new java.util.ArrayList<_date>();
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "edition")
      @XmlElement(required = false)
      public _edition edition;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "editionDate")
      @XmlElement(required = false)
      public _editionDate editionDate;
      @XmlElement(required = false)
      public java.util.List<_identifier> identifier = new java.util.ArrayList<_identifier>();
      @XmlElement(required = false)
      public java.util.List<_citedResponsibleParty> citedResponsibleParty = new java.util.ArrayList<_citedResponsibleParty>();
      @XmlElement(required = false)
      public java.util.List<_presentationForm> presentationForm = new java.util.ArrayList<_presentationForm>();
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "series")
      @XmlElement(required = false)
      public _series series;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "otherCitationDetails")
      @XmlElement(required = false)
      public _otherCitationDetails otherCitationDetails;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "collectiveTitle")
      @XmlElement(required = false)
      public _collectiveTitle collectiveTitle;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "ISBN")
      @XmlElement(required = false)
      public ISBN ISBN;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "ISSN")
      @XmlElement(required = false)
      public ISSN ISSN;

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "alternateTitle")
      public void setAlternateTitle(final _alternateTitle obj)
      {
         if (obj != null)
         {
            alternateTitle.add(obj);
         }
      }

      java.util.List<_alternateTitle> getAlternateTitle()
      {
         return alternateTitle;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "date")
      public void setDate(final _date obj)
      {
         if (obj != null)
         {
            date.add(obj);
         }
      }

      java.util.List<_date> getDate()
      {
         return date;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "identifier")
      public void setIdentifier(final _identifier obj)
      {
         if (obj != null)
         {
            identifier.add(obj);
         }
      }

      java.util.List<_identifier> getIdentifier()
      {
         return identifier;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "citedResponsibleParty")
      public void setCitedResponsibleParty(final _citedResponsibleParty obj)
      {
         if (obj != null)
         {
            citedResponsibleParty.add(obj);
         }
      }

      java.util.List<_citedResponsibleParty> getCitedResponsibleParty()
      {
         return citedResponsibleParty;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "presentationForm")
      public void setPresentationForm(final _presentationForm obj)
      {
         if (obj != null)
         {
            presentationForm.add(obj);
         }
      }

      java.util.List<_presentationForm> getPresentationForm()
      {
         return presentationForm;
      }
   }

   public static class _title extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "title");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class CharacterString extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "CharacterString");
   }

   public static class _alternateTitle extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "alternateTitle");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _date extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "date");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Date")
      @XmlElement(required = false)
      public CI_Date CI_Date;
   }

   public static class CI_Date extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_Date");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "date")
      @XmlElement(required = false)
      public _date date;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "dateType")
      @XmlElement(required = false)
      public _dateType dateType;
   }

   public static class _dateType extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "dateType");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_DateTypeCode")
      @XmlElement(required = false)
      public CI_DateTypeCode CI_DateTypeCode;
   }

   public static class CI_DateTypeCode extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_DateTypeCode");
   }

   public static class _edition extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "edition");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _editionDate extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "editionDate");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "Date")
      @XmlElement(required = false)
      public Date Date;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "DateTime")
      @XmlElement(required = false)
      public DateTime DateTime;
   }

   public static class Date extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "Date");
   }

   public static class DateTime extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "DateTime");
   }

   public static class _identifier extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "identifier");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "MD_Identifier")
      @XmlElement(required = false)
      public MD_Identifier MD_Identifier;
   }

   public static class MD_Identifier extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "MD_Identifier");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "authority")
      @XmlElement(required = false)
      public _authority authority;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "code")
      @XmlElement(required = false)
      public _code code;
   }

   public static class _authority extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "authority");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Citation")
      @XmlElement(required = false)
      public CI_Citation CI_Citation;
   }

   public static class _code extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "code");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _citedResponsibleParty extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "citedResponsibleParty");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_ResponsibleParty")
      @XmlElement(required = false)
      public CI_ResponsibleParty CI_ResponsibleParty;
   }

   public static class CI_ResponsibleParty extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_ResponsibleParty");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "individualName")
      @XmlElement(required = false)
      public _individualName individualName;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "organisationName")
      @XmlElement(required = false)
      public _organisationName organisationName;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "positionName")
      @XmlElement(required = false)
      public _positionName positionName;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "contactInfo")
      @XmlElement(required = false)
      public _contactInfo contactInfo;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "role")
      @XmlElement(required = false)
      public _role role;
   }

   public static class _individualName extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "individualName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _organisationName extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "organisationName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _positionName extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "positionName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _contactInfo extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "contactInfo");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Contact")
      @XmlElement(required = false)
      public CI_Contact CI_Contact;
   }

   public static class CI_Contact extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_Contact");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "phone")
      @XmlElement(required = false)
      public _phone phone;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "address")
      @XmlElement(required = false)
      public _address address;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "onlineResource")
      @XmlElement(required = false)
      public _onlineResource onlineResource;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "hoursOfService")
      @XmlElement(required = false)
      public _hoursOfService hoursOfService;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "contactInstructions")
      @XmlElement(required = false)
      public _contactInstructions contactInstructions;
   }

   public static class _phone extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "phone");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Telephone")
      @XmlElement(required = false)
      public CI_Telephone CI_Telephone;
   }

   public static class CI_Telephone extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_Telephone");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @XmlElement(required = false)
      public java.util.List<_voice> voice = new java.util.ArrayList<_voice>();
      @XmlElement(required = false)
      public java.util.List<_facsimile> facsimile = new java.util.ArrayList<_facsimile>();

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "voice")
      public void setVoice(final _voice obj)
      {
         if (obj != null)
         {
            voice.add(obj);
         }
      }

      java.util.List<_voice> getVoice()
      {
         return voice;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "facsimile")
      public void setFacsimile(final _facsimile obj)
      {
         if (obj != null)
         {
            facsimile.add(obj);
         }
      }

      java.util.List<_facsimile> getFacsimile()
      {
         return facsimile;
      }
   }

   public static class _voice extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "voice");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _facsimile extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "facsimile");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _address extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "address");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Address")
      @XmlElement(required = false)
      public CI_Address CI_Address;
   }

   public static class CI_Address extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_Address");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @XmlElement(required = false)
      public java.util.List<_deliveryPoint> deliveryPoint = new java.util.ArrayList<_deliveryPoint>();
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "city")
      @XmlElement(required = false)
      public _city city;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "administrativeArea")
      @XmlElement(required = false)
      public _administrativeArea administrativeArea;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "postalCode")
      @XmlElement(required = false)
      public _postalCode postalCode;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "country")
      @XmlElement(required = false)
      public _country country;
      @XmlElement(required = false)
      public java.util.List<_electronicMailAddress> electronicMailAddress = new java.util.ArrayList<_electronicMailAddress>();

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "deliveryPoint")
      public void setDeliveryPoint(final _deliveryPoint obj)
      {
         if (obj != null)
         {
            deliveryPoint.add(obj);
         }
      }

      java.util.List<_deliveryPoint> getDeliveryPoint()
      {
         return deliveryPoint;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "electronicMailAddress")
      public void setElectronicMailAddress(final _electronicMailAddress obj)
      {
         if (obj != null)
         {
            electronicMailAddress.add(obj);
         }
      }

      java.util.List<_electronicMailAddress> getElectronicMailAddress()
      {
         return electronicMailAddress;
      }
   }

   public static class _deliveryPoint extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "deliveryPoint");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _city extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "city");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _administrativeArea extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "administrativeArea");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _postalCode extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "postalCode");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _country extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "country");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _electronicMailAddress extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "electronicMailAddress");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _onlineResource extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "onlineResource");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_OnlineResource")
      @XmlElement(required = false)
      public CI_OnlineResource CI_OnlineResource;
   }

   public static class CI_OnlineResource extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_OnlineResource");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "linkage")
      @XmlElement(required = false)
      public _linkage linkage;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "protocol")
      @XmlElement(required = false)
      public _protocol protocol;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "applicationProfile")
      @XmlElement(required = false)
      public _applicationProfile applicationProfile;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "name")
      @XmlElement(required = false)
      public _name name;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "description")
      @XmlElement(required = false)
      public _description description;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "function")
      @XmlElement(required = false)
      public _function function;
   }

   public static class _linkage extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "linkage");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "URL")
      @XmlElement(required = false)
      public URL URL;
   }

   public static class URL extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "URL");
   }

   public static class _protocol extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "protocol");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _applicationProfile extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "applicationProfile");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _name extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "name");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _description extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "description");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _function extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "function");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_OnLineFunctionCode")
      @XmlElement(required = false)
      public CI_OnLineFunctionCode CI_OnLineFunctionCode;
   }

   public static class CI_OnLineFunctionCode extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_OnLineFunctionCode");
   }

   public static class _hoursOfService extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "hoursOfService");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _contactInstructions extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "contactInstructions");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _role extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "role");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_RoleCode")
      @XmlElement(required = false)
      public CI_RoleCode CI_RoleCode;
   }

   public static class CI_RoleCode extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_RoleCode");
   }

   public static class _presentationForm extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "presentationForm");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_PresentationFormCode")
      @XmlElement(required = false)
      public CI_PresentationFormCode CI_PresentationFormCode;
   }

   public static class CI_PresentationFormCode extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_PresentationFormCode");
   }

   public static class _series extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "series");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "CI_Series")
      @XmlElement(required = false)
      public CI_Series CI_Series;
   }

   public static class CI_Series extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "CI_Series");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "name")
      @XmlElement(required = false)
      public _name name;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "issueIdentification")
      @XmlElement(required = false)
      public _issueIdentification issueIdentification;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "page")
      @XmlElement(required = false)
      public _page page;
   }

   public static class _issueIdentification extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "issueIdentification");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _page extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "page");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _otherCitationDetails extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "otherCitationDetails");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class _collectiveTitle extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "collectiveTitle");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class ISBN extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "ISBN");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class ISSN extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "ISSN");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class A_2_siteDesignation extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0";
      public static final QName QN = new QName(NS, "siteDesignation");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "DesignationType")
      @XmlElement(required = false)
      public DesignationType DesignationType;
   }

   public static class DesignationType extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0";
      public static final QName QN = new QName(NS, "DesignationType");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "designationScheme")
      @XmlElement(required = false)
      public Reference designationScheme;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "designation")
      @XmlElement(required = false)
      public Reference designation;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0", localName = "percentageUnderDesignation")
      @XmlElement(required = false)
      public String percentageUnderDesignation;
   }

   public static class A_3_siteName extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:ProtectedSites:3.0";
      public static final QName QN = new QName(NS, "siteName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
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
      public Reference nativeness;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "nameStatus")
      @XmlElement(required = false)
      public Reference nameStatus;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "sourceOfName")
      @XmlElement(required = false)
      public NillableType<String> sourceOfName;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciation")
      @XmlElement(required = false)
      public A_4_pronunciation pronunciation;
      @XmlElement(required = false)
      public java.util.List<_spelling> spelling = new java.util.ArrayList<_spelling>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalGender")
      @XmlElement(required = false)
      public Reference grammaticalGender;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalNumber")
      @XmlElement(required = false)
      public Reference grammaticalNumber;

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

   public static class A_4_pronunciation extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "pronunciation");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
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
}
