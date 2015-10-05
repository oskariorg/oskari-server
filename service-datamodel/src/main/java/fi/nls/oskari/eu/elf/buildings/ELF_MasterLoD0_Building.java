package fi.nls.oskari.eu.elf.buildings;

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
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

public class ELF_MasterLoD0_Building
{

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0")
   public static class Building
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "Building");
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
      public A_1_dateOfConstruction dateOfConstruction;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfDemolition")
      @XmlElement(required = false)
      public A_2_dateOfDemolition dateOfDemolition;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfRenovation")
      @XmlElement(required = false)
      public A_3_dateOfRenovation dateOfRenovation;
      @XmlElement(required = false)
      public java.util.List<A_4_elevation> elevation = new java.util.ArrayList<A_4_elevation>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<A_5_externalReference> externalReference = new java.util.ArrayList<A_5_externalReference>();
      @XmlElement(required = false)
      public java.util.List<A_6_heightAboveGround> heightAboveGround = new java.util.ArrayList<A_6_heightAboveGround>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @XmlElement(required = false)
      public java.util.List<A_7_name> name = new java.util.ArrayList<A_7_name>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> buildingNature = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<A_9_currentUse> currentUse = new java.util.ArrayList<A_9_currentUse>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfDwellings")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfDwellings;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfBuildingUnits")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfBuildingUnits;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfFloorsAboveGround")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfFloorsAboveGround;
      @XmlElement(required = false)
      public java.util.List<A_10_parts> parts = new java.util.ArrayList<A_10_parts>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu-core2d/3.0rc3", localName = "geometry2D")
      @XmlElement(required = false)
      public _geometry2D geometry2D;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "heightBelowGround")
      @XmlElement(required = false)
      public NillableType<String> heightBelowGround;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "numberOfFloorsBelowGround")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfFloorsBelowGround;
      @XmlElement(required = false)
      public java.util.List<A_19_floorDistribution> floorDistribution = new java.util.ArrayList<A_19_floorDistribution>();
      @XmlElement(required = false)
      public java.util.List<A_20_floorDescription> floorDescription = new java.util.ArrayList<A_20_floorDescription>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> roofType = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> materialOfFacade = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> materialOfRoof = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> materialOfStructure = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "connectionToElectricity")
      @XmlElement(required = false)
      public NillableType<Boolean> connectionToElectricity;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "connectionToGas")
      @XmlElement(required = false)
      public NillableType<Boolean> connectionToGas;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "connectionToSewage")
      @XmlElement(required = false)
      public NillableType<Boolean> connectionToSewage;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "connectionToWater")
      @XmlElement(required = false)
      public NillableType<Boolean> connectionToWater;
      @XmlElement(required = false)
      public java.util.List<A_22_document> document = new java.util.ArrayList<A_22_document>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "energyPerformance")
      @XmlElement(required = false)
      public A_23_energyPerformance energyPerformance;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> heatingSource = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> heatingSystem = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<A_25_address> address = new java.util.ArrayList<A_25_address>();
      @XmlElement(required = false)
      public java.util.List<A_29_officialArea> officialArea = new java.util.ArrayList<A_29_officialArea>();
      @XmlElement(required = false)
      public java.util.List<A_30_officialValue> officialValue = new java.util.ArrayList<A_30_officialValue>();
      @XmlElement(required = false)
      public java.util.List<A_31_mixinaddress> mixinaddress = new java.util.ArrayList<A_31_mixinaddress>();
      @XmlElement(required = false)
      public java.util.List<_cadastralParcel> cadastralParcel = new java.util.ArrayList<_cadastralParcel>();

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevation")
      public void setElevation(final A_4_elevation obj)
      {
         if (obj != null)
         {
            elevation.add(obj);
         }
      }

      java.util.List<A_4_elevation> getElevation()
      {
         return elevation;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "externalReference")
      public void setExternalReference(final A_5_externalReference obj)
      {
         if (obj != null)
         {
            externalReference.add(obj);
         }
      }

      java.util.List<A_5_externalReference> getExternalReference()
      {
         return externalReference;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "heightAboveGround")
      public void setHeightAboveGround(final A_6_heightAboveGround obj)
      {
         if (obj != null)
         {
            heightAboveGround.add(obj);
         }
      }

      java.util.List<A_6_heightAboveGround> getHeightAboveGround()
      {
         return heightAboveGround;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "name")
      public void setName(final A_7_name obj)
      {
         if (obj != null)
         {
            name.add(obj);
         }
      }

      java.util.List<A_7_name> getName()
      {
         return name;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "buildingNature")
      public void setBuildingNature(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            buildingNature.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBuildingNature()
      {
         return buildingNature;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "currentUse")
      public void setCurrentUse(final A_9_currentUse obj)
      {
         if (obj != null)
         {
            currentUse.add(obj);
         }
      }

      java.util.List<A_9_currentUse> getCurrentUse()
      {
         return currentUse;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "parts")
      public void setParts(final A_10_parts obj)
      {
         if (obj != null)
         {
            parts.add(obj);
         }
      }

      java.util.List<A_10_parts> getParts()
      {
         return parts;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "floorDistribution")
      public void setFloorDistribution(final A_19_floorDistribution obj)
      {
         if (obj != null)
         {
            floorDistribution.add(obj);
         }
      }

      java.util.List<A_19_floorDistribution> getFloorDistribution()
      {
         return floorDistribution;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "floorDescription")
      public void setFloorDescription(final A_20_floorDescription obj)
      {
         if (obj != null)
         {
            floorDescription.add(obj);
         }
      }

      java.util.List<A_20_floorDescription> getFloorDescription()
      {
         return floorDescription;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "roofType")
      public void setRoofType(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            roofType.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getRoofType()
      {
         return roofType;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "materialOfFacade")
      public void setMaterialOfFacade(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            materialOfFacade.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getMaterialOfFacade()
      {
         return materialOfFacade;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "materialOfRoof")
      public void setMaterialOfRoof(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            materialOfRoof.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getMaterialOfRoof()
      {
         return materialOfRoof;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "materialOfStructure")
      public void setMaterialOfStructure(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            materialOfStructure.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getMaterialOfStructure()
      {
         return materialOfStructure;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "document")
      public void setDocument(final A_22_document obj)
      {
         if (obj != null)
         {
            document.add(obj);
         }
      }

      java.util.List<A_22_document> getDocument()
      {
         return document;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "heatingSource")
      public void setHeatingSource(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            heatingSource.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getHeatingSource()
      {
         return heatingSource;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "heatingSystem")
      public void setHeatingSystem(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            heatingSystem.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getHeatingSystem()
      {
         return heatingSystem;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "address")
      public void setAddress(final A_25_address obj)
      {
         if (obj != null)
         {
            address.add(obj);
         }
      }

      java.util.List<A_25_address> getAddress()
      {
         return address;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "officialArea")
      public void setOfficialArea(final A_29_officialArea obj)
      {
         if (obj != null)
         {
            officialArea.add(obj);
         }
      }

      java.util.List<A_29_officialArea> getOfficialArea()
      {
         return officialArea;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "officialValue")
      public void setOfficialValue(final A_30_officialValue obj)
      {
         if (obj != null)
         {
            officialValue.add(obj);
         }
      }

      java.util.List<A_30_officialValue> getOfficialValue()
      {
         return officialValue;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "mixinaddress")
      public void setMixinaddress(final A_31_mixinaddress obj)
      {
         if (obj != null)
         {
            mixinaddress.add(obj);
         }
      }

      java.util.List<A_31_mixinaddress> getMixinaddress()
      {
         return mixinaddress;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "cadastralParcel")
      public void setCadastralParcel(final _cadastralParcel obj)
      {
         if (obj != null)
         {
            cadastralParcel.add(obj);
         }
      }

      java.util.List<_cadastralParcel> getCadastralParcel()
      {
         return cadastralParcel;
      }
   }

   public static class A_1_dateOfConstruction extends Nillable
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

   public static class DateOfEvent
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

   public static class A_2_dateOfDemolition extends Nillable
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

   public static class A_3_dateOfRenovation extends Nillable
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

   public static class A_4_elevation extends Nillable
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

   public static class Elevation
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

   public static class A_5_externalReference extends Nillable
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

   public static class ExternalReference
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "ExternalReference");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "informationSystem")
      @XmlElement(required = false)
      public URI informationSystem;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "informationSystemName")
      @XmlElement(required = false)
      public List<PT_FreeText> informationSystemName;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "reference")
      @XmlElement(required = false)
      public String reference;
   }

   public static class _informationSystemName
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

   public static class CharacterString
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "CharacterString");
   }

   public static class PT_FreeText
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

   public static class _textGroup
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

   public static class A_6_heightAboveGround extends Nillable
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
      @XmlElement(required = false)
      public String value;
   }

   public static class A_7_name extends Nillable
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
      public A_8_pronunciation pronunciation;
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

   public static class A_8_pronunciation extends Nillable
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
      @XmlElement(required = false)
      public SpellingOfName SpellingOfName;
   }

   public static class SpellingOfName
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

   public static class A_9_currentUse extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "currentUse");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "CurrentUse")
      @XmlElement(required = false)
      public CurrentUse CurrentUse;
   }

   public static class CurrentUse
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "CurrentUse");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "currentUse")
      @XmlElement(required = false)
      public Reference currentUse;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "percentage")
      @XmlElement(required = false)
      public NillableType<BigInteger> percentage;
   }

   public static class A_10_parts extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "parts");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "BuildingPart")
      @XmlElement(required = false)
      public BuildingPart BuildingPart;
   }

   public static class BuildingPart
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "BuildingPart");
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
      public A_11_dateOfConstruction dateOfConstruction;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfDemolition")
      @XmlElement(required = false)
      public A_12_dateOfDemolition dateOfDemolition;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "dateOfRenovation")
      @XmlElement(required = false)
      public A_13_dateOfRenovation dateOfRenovation;
      @XmlElement(required = false)
      public java.util.List<A_14_elevation> elevation = new java.util.ArrayList<A_14_elevation>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<A_15_externalReference> externalReference = new java.util.ArrayList<A_15_externalReference>();
      @XmlElement(required = false)
      public java.util.List<A_16_heightAboveGround> heightAboveGround = new java.util.ArrayList<A_16_heightAboveGround>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @XmlElement(required = false)
      public java.util.List<A_17_name> name = new java.util.ArrayList<A_17_name>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> buildingNature = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<A_18_currentUse> currentUse = new java.util.ArrayList<A_18_currentUse>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfDwellings")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfDwellings;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfBuildingUnits")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfBuildingUnits;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfFloorsAboveGround")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfFloorsAboveGround;

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "elevation")
      public void setElevation(final A_14_elevation obj)
      {
         if (obj != null)
         {
            elevation.add(obj);
         }
      }

      java.util.List<A_14_elevation> getElevation()
      {
         return elevation;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "externalReference")
      public void setExternalReference(final A_15_externalReference obj)
      {
         if (obj != null)
         {
            externalReference.add(obj);
         }
      }

      java.util.List<A_15_externalReference> getExternalReference()
      {
         return externalReference;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "heightAboveGround")
      public void setHeightAboveGround(final A_16_heightAboveGround obj)
      {
         if (obj != null)
         {
            heightAboveGround.add(obj);
         }
      }

      java.util.List<A_16_heightAboveGround> getHeightAboveGround()
      {
         return heightAboveGround;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "name")
      public void setName(final A_17_name obj)
      {
         if (obj != null)
         {
            name.add(obj);
         }
      }

      java.util.List<A_17_name> getName()
      {
         return name;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "buildingNature")
      public void setBuildingNature(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            buildingNature.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBuildingNature()
      {
         return buildingNature;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "currentUse")
      public void setCurrentUse(final A_18_currentUse obj)
      {
         if (obj != null)
         {
            currentUse.add(obj);
         }
      }

      java.util.List<A_18_currentUse> getCurrentUse()
      {
         return currentUse;
      }
   }

   public static class A_11_dateOfConstruction extends Nillable
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

   public static class A_12_dateOfDemolition extends Nillable
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

   public static class A_13_dateOfRenovation extends Nillable
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

   public static class A_14_elevation extends Nillable
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

   public static class A_15_externalReference extends Nillable
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

   public static class A_16_heightAboveGround extends Nillable
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

   public static class A_17_name extends Nillable
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

   public static class A_18_currentUse extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "currentUse");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "CurrentUse")
      @XmlElement(required = false)
      public CurrentUse CurrentUse;
   }

   public static class _geometry2D
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu-core2d/3.0rc3";
      public static final QName QN = new QName(NS, "geometry2D");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "BuildingGeometry2D")
      @XmlElement(required = false)
      public BuildingGeometry2D BuildingGeometry2D;
   }

   public static class BuildingGeometry2D
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "BuildingGeometry2D");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "referenceGeometry")
      @XmlElement(required = false)
      public Boolean referenceGeometry;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "horizontalGeometryReference")
      @XmlElement(required = false)
      public Reference horizontalGeometryReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "verticalGeometryReference")
      @XmlElement(required = false)
      public Reference verticalGeometryReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "horizontalGeometryEstimatedAccuracy")
      @XmlElement(required = false)
      public NillableType<String> horizontalGeometryEstimatedAccuracy;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "verticalGeometryEstimatedAccuracy")
      @XmlElement(required = false)
      public NillableType<String> verticalGeometryEstimatedAccuracy;
   }

   public static class A_19_floorDistribution extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "floorDistribution");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "FloorRange")
      @XmlElement(required = false)
      public FloorRange FloorRange;
   }

   public static class FloorRange
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "FloorRange");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "lowestFloor")
      @XmlElement(required = false)
      public String lowestFloor;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "highestFloor")
      @XmlElement(required = false)
      public String highestFloor;
   }

   public static class A_20_floorDescription extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "floorDescription");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "FloorDescription")
      @XmlElement(required = false)
      public FloorDescription FloorDescription;
   }

   public static class FloorDescription
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "FloorDescription");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "areaOfOpenings")
      @XmlElement(required = false)
      public NillableType<String> areaOfOpenings;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> currentUse = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<A_21_document> document = new java.util.ArrayList<A_21_document>();
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "floorArea")
      @XmlElement(required = false)
      public NillableType<String> floorArea;
      @XmlElement(required = false)
      public java.util.List<_floorRange> floorRange = new java.util.ArrayList<_floorRange>();
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "height")
      @XmlElement(required = false)
      public NillableType<String> height;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "numberOfDwellings")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfDwellings;

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "currentUse")
      public void setCurrentUse(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            currentUse.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getCurrentUse()
      {
         return currentUse;
      }

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "document")
      public void setDocument(final A_21_document obj)
      {
         if (obj != null)
         {
            document.add(obj);
         }
      }

      java.util.List<A_21_document> getDocument()
      {
         return document;
      }

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "floorRange")
      public void setFloorRange(final _floorRange obj)
      {
         if (obj != null)
         {
            floorRange.add(obj);
         }
      }

      java.util.List<_floorRange> getFloorRange()
      {
         return floorRange;
      }
   }

   public static class A_21_document extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "document");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "Document")
      @XmlElement(required = false)
      public Document Document;
   }

   public static class Document
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "Document");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "date")
      @XmlElement(required = false)
      public NillableType<String> date;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "documentDescription")
      @XmlElement(required = false)
      public List<PT_FreeText> documentDescription;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "documentLink")
      @XmlElement(required = false)
      public URI documentLink;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "sourceStatus")
      @XmlElement(required = false)
      public Reference sourceStatus;
   }

   public static class _documentDescription extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "documentDescription");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "PT_FreeText")
      @XmlElement(required = false)
      public PT_FreeText PT_FreeText;
   }

   public static class _floorRange
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "floorRange");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "FloorRange")
      @XmlElement(required = false)
      public FloorRange FloorRange;
   }

   public static class A_22_document extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "document");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "Document")
      @XmlElement(required = false)
      public Document Document;
   }

   public static class A_23_energyPerformance extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "energyPerformance");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "EnergyPerformance")
      @XmlElement(required = false)
      public EnergyPerformance EnergyPerformance;
   }

   public static class EnergyPerformance
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "EnergyPerformance");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "energyPerformanceValue")
      @XmlElement(required = false)
      public Reference energyPerformanceValue;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "dateOfAssessment")
      @XmlElement(required = false)
      public NillableType<String> dateOfAssessment;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "assessmentMethod")
      @XmlElement(required = false)
      public A_24_assessmentMethod assessmentMethod;
   }

   public static class A_24_assessmentMethod extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "assessmentMethod");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "DocumentCitation")
      @XmlElement(required = false)
      public DocumentCitation DocumentCitation;
   }

   public static class DocumentCitation
   {
      public static final String NS = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3";
      public static final QName QN = new QName(NS, "DocumentCitation");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "name")
      @XmlElement(required = false)
      public String name;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "shortName")
      @XmlElement(required = false)
      public NillableType<String> shortName;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "date")
      @XmlElement(required = false)
      public Object date;
      @XmlElement(required = false)
      public java.util.List<java.net.URI> link = new java.util.ArrayList<java.net.URI>();
      @XmlElement(required = false)
      public java.util.List<java.lang.String> specificReference = new java.util.ArrayList<java.lang.String>();

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "link")
      public void setLink(final java.net.URI obj)
      {
         if (obj != null)
         {
            link.add(obj);
         }
      }

      java.util.List<java.net.URI> getLink()
      {
         return link;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "specificReference")
      public void setSpecificReference(final java.lang.String obj)
      {
         if (obj != null)
         {
            specificReference.add(obj);
         }
      }

      java.util.List<java.lang.String> getSpecificReference()
      {
         return specificReference;
      }
   }

   public static class A_25_address extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "address");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "AddressRepresentation")
      @XmlElement(required = false)
      public AddressRepresentation AddressRepresentation;
   }

   public static class AddressRepresentation
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "AddressRepresentation");
      @XmlElement(required = false)
      public java.util.List<_adminUnit> adminUnit = new java.util.ArrayList<_adminUnit>();
      @XmlElement(required = false)
      public java.util.List<java.lang.String> locatorDesignator = new java.util.ArrayList<java.lang.String>();
      @XmlElement(required = false)
      public java.util.List<_locatorName> locatorName = new java.util.ArrayList<_locatorName>();
      @XmlElement(required = false)
      public java.util.List<A_26_addressArea> addressArea = new java.util.ArrayList<A_26_addressArea>();
      @XmlElement(required = false)
      public java.util.List<A_27_postName> postName = new java.util.ArrayList<A_27_postName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "postCode")
      @XmlElement(required = false)
      public NillableType<String> postCode;
      @XmlElement(required = false)
      public java.util.List<A_28_thoroughfare> thoroughfare = new java.util.ArrayList<A_28_thoroughfare>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "addressFeature")
      @XmlElement(required = false)
      public Reference addressFeature;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "adminUnit")
      public void setAdminUnit(final _adminUnit obj)
      {
         if (obj != null)
         {
            adminUnit.add(obj);
         }
      }

      java.util.List<_adminUnit> getAdminUnit()
      {
         return adminUnit;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "locatorDesignator")
      public void setLocatorDesignator(final java.lang.String obj)
      {
         if (obj != null)
         {
            locatorDesignator.add(obj);
         }
      }

      java.util.List<java.lang.String> getLocatorDesignator()
      {
         return locatorDesignator;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "locatorName")
      public void setLocatorName(final _locatorName obj)
      {
         if (obj != null)
         {
            locatorName.add(obj);
         }
      }

      java.util.List<_locatorName> getLocatorName()
      {
         return locatorName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "addressArea")
      public void setAddressArea(final A_26_addressArea obj)
      {
         if (obj != null)
         {
            addressArea.add(obj);
         }
      }

      java.util.List<A_26_addressArea> getAddressArea()
      {
         return addressArea;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "postName")
      public void setPostName(final A_27_postName obj)
      {
         if (obj != null)
         {
            postName.add(obj);
         }
      }

      java.util.List<A_27_postName> getPostName()
      {
         return postName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "thoroughfare")
      public void setThoroughfare(final A_28_thoroughfare obj)
      {
         if (obj != null)
         {
            thoroughfare.add(obj);
         }
      }

      java.util.List<A_28_thoroughfare> getThoroughfare()
      {
         return thoroughfare;
      }
   }

   public static class _adminUnit
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "adminUnit");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = false)
      public GeographicalName GeographicalName;
   }

   public static class _locatorName
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "locatorName");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = false)
      public GeographicalName GeographicalName;
   }

   public static class A_26_addressArea extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "addressArea");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = false)
      public GeographicalName GeographicalName;
   }

   public static class A_27_postName extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "postName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = false)
      public GeographicalName GeographicalName;
   }

   public static class A_28_thoroughfare extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "thoroughfare");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = false)
      public GeographicalName GeographicalName;
   }

   public static class A_29_officialArea extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "officialArea");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "OfficialArea")
      @XmlElement(required = false)
      public OfficialArea OfficialArea;
   }

   public static class OfficialArea
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "OfficialArea");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "officialAreaReference")
      @XmlElement(required = false)
      public Reference officialAreaReference;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "value")
      @XmlElement(required = false)
      public String value;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "heightParameter")
      @XmlElement(required = false)
      public NillableType<String> heightParameter;
   }

   public static class A_30_officialValue extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "officialValue");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "OfficialValue")
      @XmlElement(required = false)
      public OfficialValue OfficialValue;
   }

   public static class OfficialValue
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "OfficialValue");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "currency")
      @XmlElement(required = false)
      public Reference currency;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "value")
      @XmlElement(required = false)
      public NillableType<BigInteger> value;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "valuationDate")
      @XmlElement(required = false)
      public NillableType<String> valuationDate;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "officialValueReference")
      @XmlElement(required = false)
      public Reference officialValueReference;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "referencePercentage")
      @XmlElement(required = false)
      public NillableType<BigInteger> referencePercentage;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "informationSystemName")
      @XmlElement(required = false)
      public List<PT_FreeText> informationSystemName;
   }

   public static class A_31_mixinaddress extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "mixinaddress");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "AddressRepresentation")
      @XmlElement(required = false)
      public AddressRepresentation AddressRepresentation;
   }

   public static class _cadastralParcel extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";
      public static final QName QN = new QName(NS, "cadastralParcel");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "CadastralParcel")
      @XmlElement(required = false)
      public CadastralParcel CadastralParcel;
   }

   public static class CadastralParcel
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0";
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
