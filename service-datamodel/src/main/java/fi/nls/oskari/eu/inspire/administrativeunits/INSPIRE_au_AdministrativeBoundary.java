package fi.nls.oskari.eu.inspire.administrativeunits;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.CodeType;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

/**
 * 
- URL http://inspire.ec.europa.eu/schemas/au/3.0/AdministrativeUnits.xsd
- timestamp Wed Dec 17 13:46:35 EET 2014
 */
public class INSPIRE_au_AdministrativeBoundary
{

   public static final String TIMESTAMP = "Wed Dec 17 13:46:35 EET 2014";
   public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/au/3.0/AdministrativeUnits.xsd";

   @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0")
   public static class AdministrativeBoundary extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "AdministrativeBoundary");
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
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public _inspireId inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "country")
      @XmlElement(required = false)
      public CodeType country;
      @XmlElement(required = false)
      public java.util.List<java.lang.String> nationalLevel = new java.util.ArrayList<java.lang.String>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "legalStatus")
      @XmlElement(required = false)
      public NillableType<String> legalStatus;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "technicalStatus")
      @XmlElement(required = false)
      public NillableType<String> technicalStatus;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> admUnit = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevel")
      public void setNationalLevel(final java.lang.String obj)
      {
         if (obj != null)
         {
            nationalLevel.add(obj);
         }
      }

      java.util.List<java.lang.String> getNationalLevel()
      {
         return nationalLevel;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "admUnit")
      public void setAdmUnit(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            admUnit.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getAdmUnit()
      {
         return admUnit;
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
}
