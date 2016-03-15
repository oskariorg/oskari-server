package fi.nls.oskari.eu.elf.cadastralparcels;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.xml.util.Reference;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

public class ELF_MasterLoD0_CadastralParcel
{

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/CadastralParcels/MasterLoD0/1.0")
   public static class CadastralParcel
   {
      public static final String NS = "http://www.locationframework.eu/schemas/CadastralParcels/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "CadastralParcel");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "areaValue")
      @XmlElement(required = false)
      public NillableType<String> areaValue;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "label")
      @XmlElement(required = false)
      public String label;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "nationalCadastralReference")
      @XmlElement(required = false)
      public String nationalCadastralReference;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "referencePoint")
      @XmlElement(required = false)
      public GeometryProperty referencePoint;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<String> validFrom;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "validTo")
      @XmlElement(required = false)
      public NillableType<String> validTo;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "zoning")
      @XmlElement(required = false)
      public Reference zoning;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> basicPropertyUnit = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "administrativeUnit")
      @XmlElement(required = false)
      public Reference administrativeUnit;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "basicPropertyUnit")
      public void setBasicPropertyUnit(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            basicPropertyUnit.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBasicPropertyUnit()
      {
         return basicPropertyUnit;
      }
   }
}
