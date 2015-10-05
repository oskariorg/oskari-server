package fi.nls.oskari.eu.inspire.addresses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.xml.util.Reference;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

/**
 * 
- URL http://inspire.ec.europa.eu/schemas/ad/3.0/Addresses.xsd
- timestamp Fri Dec 12 19:46:21 EET 2014
 */
public class INSPIRE_ad_Address
{

   public static final String TIMESTAMP = "Fri Dec 12 19:46:21 EET 2014";
   public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/ad/3.0/Addresses.xsd";

   @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0")
   public static class Address extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "Address");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public _inspireId inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "alternativeIdentifier")
      @XmlElement(required = false)
      public NillableType<String> alternativeIdentifier;
      @XmlElement(required = false)
      public java.util.List<_position> position = new java.util.ArrayList<_position>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "status")
      @XmlElement(required = false)
      public NillableType<String> status;
      @XmlElement(required = false)
      public java.util.List<_locator> locator = new java.util.ArrayList<_locator>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<String> validFrom;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "validTo")
      @XmlElement(required = false)
      public NillableType<String> validTo;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> parcel = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "parentAddress")
      @XmlElement(required = false)
      public Reference parentAddress;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> building = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<A_1_component> component = new java.util.ArrayList<A_1_component>();

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "position")
      public void setPosition(final _position obj)
      {
         if (obj != null)
         {
            position.add(obj);
         }
      }

      java.util.List<_position> getPosition()
      {
         return position;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "locator")
      public void setLocator(final _locator obj)
      {
         if (obj != null)
         {
            locator.add(obj);
         }
      }

      java.util.List<_locator> getLocator()
      {
         return locator;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "parcel")
      public void setParcel(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            parcel.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getParcel()
      {
         return parcel;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "building")
      public void setBuilding(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            building.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBuilding()
      {
         return building;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "component")
      public void setComponent(final A_1_component obj)
      {
         if (obj != null)
         {
            component.add(obj);
         }
      }

      java.util.List<A_1_component> getComponent()
      {
         return component;
      }
   }

   public static class _inspireId extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
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

   public static class _position extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "position");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "GeographicPosition")
      @XmlElement(required = false)
      public GeographicPosition GeographicPosition;
   }

   public static class GeographicPosition extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "GeographicPosition");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "specification")
      @XmlElement(required = false)
      public NillableType<String> specification;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "method")
      @XmlElement(required = false)
      public NillableType<String> method;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "default")
      @XmlElement(required = false)
      public Boolean _default;
   }

   public static class _locator extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "locator");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "AddressLocator")
      @XmlElement(required = false)
      public AddressLocator AddressLocator;
   }

   public static class AddressLocator extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "AddressLocator");
      @XmlElement(required = false)
      public java.util.List<_designator> designator = new java.util.ArrayList<_designator>();
      @XmlElement(required = false)
      public java.util.List<_name> name = new java.util.ArrayList<_name>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "level")
      @XmlElement(required = false)
      public String level;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "withinScopeOf")
      @XmlElement(required = false)
      public Reference withinScopeOf;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "designator")
      public void setDesignator(final _designator obj)
      {
         if (obj != null)
         {
            designator.add(obj);
         }
      }

      java.util.List<_designator> getDesignator()
      {
         return designator;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "name")
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
   }

   public static class _designator extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "designator");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "LocatorDesignator")
      @XmlElement(required = false)
      public LocatorDesignator LocatorDesignator;
   }

   public static class LocatorDesignator extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "LocatorDesignator");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "designator")
      @XmlElement(required = false)
      public String designator;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "type")
      @XmlElement(required = false)
      public String type;
   }

   public static class _name extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "name");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "LocatorName")
      @XmlElement(required = false)
      public LocatorName LocatorName;
   }

   public static class LocatorName extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "LocatorName");
      @XmlElement(required = false)
      public java.util.List<_name> name = new java.util.ArrayList<_name>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "type")
      @XmlElement(required = false)
      public String type;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "name")
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
   }

   public static class A_1_component extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "component");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
   }
}
