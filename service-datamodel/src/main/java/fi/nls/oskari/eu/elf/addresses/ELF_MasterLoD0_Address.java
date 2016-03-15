package fi.nls.oskari.eu.elf.addresses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.DirectPositionType;
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
- URL http://elfserver.kartverket.no/schemas/elf1.0/LoD0_Addresses.xsd
- timestamp Thu Dec 18 13:30:21 EET 2014
 */
public class ELF_MasterLoD0_Address
{

   public static final String TIMESTAMP = "Thu Dec 18 13:30:21 EET 2014";
   public static final String SCHEMASOURCE = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_Addresses.xsd";

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0")
   public static class Address extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0";
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
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "alternativeIdentifier")
      @XmlElement(required = false)
      public NillableType<String> alternativeIdentifier;
      @XmlElement(required = false)
      public java.util.List<_position> position = new java.util.ArrayList<_position>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "status")
      @XmlElement(required = false)
      public Reference status;
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
      @XmlElement(required = false)
      public java.util.List<A_1_component> component = new java.util.ArrayList<A_1_component>();
      @XmlElement(required = false)
      public java.util.List<_building> building = new java.util.ArrayList<_building>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "parentAddress")
      @XmlElement(required = false)
      public Reference parentAddress;

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

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "building")
      public void setBuilding(final _building obj)
      {
         if (obj != null)
         {
            building.add(obj);
         }
      }

      java.util.List<_building> getBuilding()
      {
         return building;
      }
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
      public Reference specification;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "method")
      @XmlElement(required = false)
      public Reference method;
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
      public Reference level;
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
      public Reference type;
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
      public Reference type;

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

   public static class _building extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "building");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "AbstractConstruction")
      @XmlElement(required = false)
      public AbstractConstruction AbstractConstruction;
   }

   public static class AbstractConstruction extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "AbstractConstruction");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "conditionOfConstruction")
      @XmlElement(required = false)
      public Reference conditionOfConstruction;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfConstruction")
      @XmlElement(required = false)
      public A_2_dateOfConstruction dateOfConstruction;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfDemolition")
      @XmlElement(required = false)
      public A_3_dateOfDemolition dateOfDemolition;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfRenovation")
      @XmlElement(required = false)
      public A_4_dateOfRenovation dateOfRenovation;
      @XmlElement(required = false)
      public java.util.List<A_5_elevation> elevation = new java.util.ArrayList<A_5_elevation>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<A_6_externalReference> externalReference = new java.util.ArrayList<A_6_externalReference>();
      @XmlElement(required = false)
      public java.util.List<A_7_heightAboveGround> heightAboveGround = new java.util.ArrayList<A_7_heightAboveGround>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @XmlElement(required = false)
      public java.util.List<A_8_name> name = new java.util.ArrayList<A_8_name>();

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevation")
      public void setElevation(final A_5_elevation obj)
      {
         if (obj != null)
         {
            elevation.add(obj);
         }
      }

      java.util.List<A_5_elevation> getElevation()
      {
         return elevation;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "externalReference")
      public void setExternalReference(final A_6_externalReference obj)
      {
         if (obj != null)
         {
            externalReference.add(obj);
         }
      }

      java.util.List<A_6_externalReference> getExternalReference()
      {
         return externalReference;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "heightAboveGround")
      public void setHeightAboveGround(final A_7_heightAboveGround obj)
      {
         if (obj != null)
         {
            heightAboveGround.add(obj);
         }
      }

      java.util.List<A_7_heightAboveGround> getHeightAboveGround()
      {
         return heightAboveGround;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "name")
      public void setName(final A_8_name obj)
      {
         if (obj != null)
         {
            name.add(obj);
         }
      }

      java.util.List<A_8_name> getName()
      {
         return name;
      }
   }

   public static class A_2_dateOfConstruction extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "dateOfConstruction");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "DateOfEvent")
      @XmlElement(required = false)
      public DateOfEvent DateOfEvent;
   }

   public static class DateOfEvent extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "DateOfEvent");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "anyPoint")
      @XmlElement(required = false)
      public NillableType<String> anyPoint;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "beginning")
      @XmlElement(required = false)
      public NillableType<String> beginning;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "end")
      @XmlElement(required = false)
      public NillableType<String> end;
   }

   public static class A_3_dateOfDemolition extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "dateOfDemolition");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "DateOfEvent")
      @XmlElement(required = false)
      public DateOfEvent DateOfEvent;
   }

   public static class A_4_dateOfRenovation extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "dateOfRenovation");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "DateOfEvent")
      @XmlElement(required = false)
      public DateOfEvent DateOfEvent;
   }

   public static class A_5_elevation extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "elevation");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "Elevation")
      @XmlElement(required = false)
      public Elevation Elevation;
   }

   public static class Elevation extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "Elevation");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevationReference")
      @XmlElement(required = false)
      public Reference elevationReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevationValue")
      @XmlElement(required = false)
      public DirectPositionType elevationValue;
   }

   public static class A_6_externalReference extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "externalReference");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "ExternalReference")
      @XmlElement(required = false)
      public ExternalReference ExternalReference;
   }

   public static class ExternalReference extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "ExternalReference");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "informationSystem")
      @XmlElement(required = false)
      public URI informationSystem;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "informationSystemName")
      @XmlElement(required = false)
      public _informationSystemName informationSystemName;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "reference")
      @XmlElement(required = false)
      public String reference;
   }

   public static class _informationSystemName extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "informationSystemName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "PT_FreeText")
      @XmlElement(required = false)
      public PT_FreeText PT_FreeText;
   }

   public static class CharacterString extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "CharacterString");
   }

   public static class PT_FreeText extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "PT_FreeText");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @XmlElement(required = false)
      public java.util.List<_textGroup> textGroup = new java.util.ArrayList<_textGroup>();

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "textGroup")
      public void setTextGroup(final _textGroup obj)
      {
         if (obj != null)
         {
            textGroup.add(obj);
         }
      }

      java.util.List<_textGroup> getTextGroup()
      {
         return textGroup;
      }
   }

   public static class _textGroup extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "textGroup");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "LocalisedCharacterString")
      @XmlElement(required = false)
      public LocalisedCharacterString LocalisedCharacterString;
   }

   public static class A_7_heightAboveGround extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "heightAboveGround");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "HeightAboveGround")
      @XmlElement(required = false)
      public HeightAboveGround HeightAboveGround;
   }

   public static class HeightAboveGround extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "HeightAboveGround");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "heightReference")
      @XmlElement(required = false)
      public Reference heightReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "lowReference")
      @XmlElement(required = false)
      public Reference lowReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "status")
      @XmlElement(required = false)
      public Reference status;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "value")
      @XmlElement(required = false)
      public String value;
   }

   public static class A_8_name extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "name");
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
      public A_9_pronunciation pronunciation;
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

   public static class A_9_pronunciation extends Nillable
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
