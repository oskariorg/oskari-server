package fi.nls.oskari.eu.elf.addresses;

import java.net.URI;
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
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.xml.util.Reference;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.gml.util.DirectPositionType;
import java.util.List;

public class ELF_MasterLoD0_Address
{

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0")
   public static class Address
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
      @XmlElement(required = true)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "alternativeIdentifier")
      @XmlElement(required = false)
      public NillableType<String> alternativeIdentifier;
      private java.util.List<GeographicPosition> position = new java.util.ArrayList<GeographicPosition>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "status")
      @XmlElement(required = false)
      public Reference status;
      private java.util.List<AddressLocator> locator = new java.util.ArrayList<AddressLocator>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<Calendar> validFrom;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "validTo")
      @XmlElement(required = false)
      public NillableType<Calendar> validTo;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> endLifespanVersion;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> parcel = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<A_1_component> component = new java.util.ArrayList<A_1_component>();
      private java.util.List<AbstractConstruction> building = new java.util.ArrayList<AbstractConstruction>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "parentAddress")
      @XmlElement(required = false)
      public Reference parentAddress;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "position")
      @XmlElement(required = true)
      public void setPosition(final java.util.List<GeographicPosition> list)
      {
         if (list != null)
         {
            position.addAll(list);
         }
         else
         {
            position.clear();
         }
      }

      @JsonGetter
      public java.util.List<GeographicPosition> getPosition()
      {
         return position;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "locator")
      @XmlElement(required = true)
      public void setLocator(final java.util.List<AddressLocator> list)
      {
         if (list != null)
         {
            locator.addAll(list);
         }
         else
         {
            locator.clear();
         }
      }

      @JsonGetter
      public java.util.List<AddressLocator> getLocator()
      {
         return locator;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "parcel")
      @XmlElement(required = false)
      public void setParcel(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            parcel.addAll(list);
         }
         else
         {
            parcel.clear();
         }
      }

      @JsonGetter
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> getParcel()
      {
         return parcel;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "component")
      @XmlElement(required = true)
      public void setComponent(final java.util.List<A_1_component> list)
      {
         if (list != null)
         {
            component.addAll(list);
         }
         else
         {
            component.clear();
         }
      }

      @JsonGetter
      public java.util.List<A_1_component> getComponent()
      {
         return component;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "building")
      @XmlElement(required = false)
      public void setBuilding(final java.util.List<AbstractConstruction> list)
      {
         if (list != null)
         {
            building.addAll(list);
         }
         else
         {
            building.clear();
         }
      }

      @JsonGetter
      public java.util.List<AbstractConstruction> getBuilding()
      {
         return building;
      }
   }

   public static class _position
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "position");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "GeographicPosition")
      @XmlElement(required = true)
      public GeographicPosition GeographicPosition;
   }

   public static class GeographicPosition
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "GeographicPosition");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "geometry")
      @XmlElement(required = true)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "specification")
      @XmlElement(required = false)
      public Reference specification;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "method")
      @XmlElement(required = false)
      public Reference method;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "default")
      @XmlElement(required = true)
      public Boolean _default;
   }

   public static class _locator
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "locator");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "AddressLocator")
      @XmlElement(required = true)
      public AddressLocator AddressLocator;
   }

   public static class AddressLocator
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "AddressLocator");
      private java.util.List<LocatorDesignator> designator = new java.util.ArrayList<LocatorDesignator>();
      private java.util.List<LocatorName> name = new java.util.ArrayList<LocatorName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "level")
      @XmlElement(required = true)
      public Reference level;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "withinScopeOf")
      @XmlElement(required = false)
      public Reference withinScopeOf;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "designator")
      @XmlElement(required = false)
      public void setDesignator(final java.util.List<LocatorDesignator> list)
      {
         if (list != null)
         {
            designator.addAll(list);
         }
         else
         {
            designator.clear();
         }
      }

      @JsonGetter
      public java.util.List<LocatorDesignator> getDesignator()
      {
         return designator;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "name")
      @XmlElement(required = false)
      public void setName(final java.util.List<LocatorName> list)
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
      public java.util.List<LocatorName> getName()
      {
         return name;
      }
   }

   public static class _designator
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "designator");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "LocatorDesignator")
      @XmlElement(required = true)
      public LocatorDesignator LocatorDesignator;
   }

   public static class LocatorDesignator
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "LocatorDesignator");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "designator")
      @XmlElement(required = true)
      public String designator;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "type")
      @XmlElement(required = true)
      public Reference type;
   }

   public static class _name
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "name");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "LocatorName")
      @XmlElement(required = true)
      public LocatorName LocatorName;
   }

   public static class LocatorName
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "LocatorName");
      private java.util.List<GeographicalName> name = new java.util.ArrayList<GeographicalName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "type")
      @XmlElement(required = true)
      public Reference type;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "name")
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
   }

   public static class A_1_component
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
      @XmlElement(required = true)
      public AbstractConstruction AbstractConstruction;
   }

   public static class AbstractConstruction
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
      public NillableType<Calendar> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "conditionOfConstruction")
      @XmlElement(required = false)
      public Reference conditionOfConstruction;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfConstruction")
      @XmlElement(required = false)
      public DateOfEvent dateOfConstruction;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfDemolition")
      @XmlElement(required = false)
      public DateOfEvent dateOfDemolition;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfRenovation")
      @XmlElement(required = false)
      public DateOfEvent dateOfRenovation;
      private java.util.List<Elevation> elevation = new java.util.ArrayList<Elevation>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> endLifespanVersion;
      private java.util.List<ExternalReference> externalReference = new java.util.ArrayList<ExternalReference>();
      private java.util.List<HeightAboveGround> heightAboveGround = new java.util.ArrayList<HeightAboveGround>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "inspireId")
      @XmlElement(required = true)
      public Identifier inspireId;
      private java.util.List<GeographicalName> name = new java.util.ArrayList<GeographicalName>();

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevation")
      @XmlElement(required = false)
      public void setElevation(final java.util.List<Elevation> list)
      {
         if (list != null)
         {
            elevation.addAll(list);
         }
         else
         {
            elevation.clear();
         }
      }

      @JsonGetter
      public java.util.List<Elevation> getElevation()
      {
         return elevation;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "externalReference")
      @XmlElement(required = false)
      public void setExternalReference(
            final java.util.List<ExternalReference> list)
      {
         if (list != null)
         {
            externalReference.addAll(list);
         }
         else
         {
            externalReference.clear();
         }
      }

      @JsonGetter
      public java.util.List<ExternalReference> getExternalReference()
      {
         return externalReference;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "heightAboveGround")
      @XmlElement(required = false)
      public void setHeightAboveGround(
            final java.util.List<HeightAboveGround> list)
      {
         if (list != null)
         {
            heightAboveGround.addAll(list);
         }
         else
         {
            heightAboveGround.clear();
         }
      }

      @JsonGetter
      public java.util.List<HeightAboveGround> getHeightAboveGround()
      {
         return heightAboveGround;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "name")
      @XmlElement(required = false)
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
      @XmlElement(required = true)
      public DateOfEvent DateOfEvent;
   }

   public static class DateOfEvent
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "DateOfEvent");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "anyPoint")
      @XmlElement(required = false)
      public NillableType<Calendar> anyPoint;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "beginning")
      @XmlElement(required = false)
      public NillableType<Calendar> beginning;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "end")
      @XmlElement(required = false)
      public NillableType<Calendar> end;
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
      public Elevation Elevation;
   }

   public static class Elevation
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "Elevation");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevationReference")
      @XmlElement(required = true)
      public Reference elevationReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevationValue")
      @XmlElement(required = true)
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
      @XmlElement(required = true)
      public ExternalReference ExternalReference;
   }

   public static class ExternalReference
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "ExternalReference");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "informationSystem")
      @XmlElement(required = true)
      public URI informationSystem;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "informationSystemName")
      @XmlElement(required = true)
      public List<PT_FreeText> informationSystemName;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "reference")
      @XmlElement(required = true)
      public String reference;
   }

   public static class _informationSystemName
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "informationSystemName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = true)
      public String CharacterString;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "PT_FreeText")
      @XmlElement(required = true)
      public PT_FreeText PT_FreeText;
   }

   public static class PT_FreeText
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "PT_FreeText");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      private java.util.List<LocalisedCharacterString> textGroup = new java.util.ArrayList<LocalisedCharacterString>();

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "textGroup")
      @XmlElement(required = true)
      public void setTextGroup(
            final java.util.List<LocalisedCharacterString> list)
      {
         if (list != null)
         {
            textGroup.addAll(list);
         }
         else
         {
            textGroup.clear();
         }
      }

      @JsonGetter
      public java.util.List<LocalisedCharacterString> getTextGroup()
      {
         return textGroup;
      }
   }

   public static class _textGroup
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "textGroup");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "LocalisedCharacterString")
      @XmlElement(required = true)
      public String LocalisedCharacterString;
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
      @XmlElement(required = true)
      public HeightAboveGround HeightAboveGround;
   }

   public static class HeightAboveGround
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
      @XmlElement(required = true)
      public Double value;
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

   public static class A_9_pronunciation extends Nillable
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
}
