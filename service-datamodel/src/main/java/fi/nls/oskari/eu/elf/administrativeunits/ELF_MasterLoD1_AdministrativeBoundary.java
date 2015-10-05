package fi.nls.oskari.eu.elf.administrativeunits;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.CodeType;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.NillableType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.util.logging.Logger;

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
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "country")
      @XmlElement(required = false)
      public CodeType country;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "legalStatus")
      @XmlElement(required = false)
      public NillableType<String> legalStatus;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> nationalLevel = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "technicalStatus")
      @XmlElement(required = false)
      public NillableType<String> technicalStatus;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> admUnit = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();

      // Handle unknown deserialization parameters
      @JsonAnySetter
      protected void handleUnknown(String key, Object value) {
         Logger.getLogger(this.getClass().getName()).info("Unknown property: "+key);
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0", localName = "nationalLevel")
      public void setNationalLevel(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            nationalLevel.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getNationalLevel()
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
}
