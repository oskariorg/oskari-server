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
import fi.nls.oskari.fe.xml.util.Nillable;

public class ELF_MasterLoD1_AdministrativeBoundary
{

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0")
   public static class AdministrativeBoundary
   {
      public static final String NS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "AdministrativeBoundary");
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
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "legalStatus")
      @XmlElement(required = false)
      public A_1_legalStatus legalStatus;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> nationalLevel = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "technicalStatus")
      @XmlElement(required = false)
      public A_2_technicalStatus technicalStatus;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> admUnit = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevel")
      @XmlElement(required = true)
      public void setNationalLevel(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            nationalLevel.addAll(list);
         }
         else
         {
            nationalLevel.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getNationalLevel()
      {
         return nationalLevel;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "admUnit")
      @XmlElement(required = false)
      public void setAdmUnit(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            admUnit.addAll(list);
         }
         else
         {
            admUnit.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getAdmUnit()
      {
         return admUnit;
      }
   }

   public static class A_1_legalStatus extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "legalStatus");
   }

   public static class A_2_technicalStatus extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";
      public static final QName QN = new QName(NS, "technicalStatus");
   }
}
