package fi.nls.oskari.eu.elf.buildings;

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
import fi.nls.oskari.fe.xml.util.Reference;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.gml.util.DirectPositionType;
import java.util.List;
import java.net.URI;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.GeometryProperty;

public class ELF_MasterLoD0_Building_nls_fi_wfs
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
      public NillableType<Calendar> beginLifespanVersion;
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
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> buildingNature = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<CurrentUse> currentUse = new java.util.ArrayList<CurrentUse>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfDwellings")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfDwellings;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfBuildingUnits")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfBuildingUnits;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "numberOfFloorsAboveGround")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfFloorsAboveGround;
      private java.util.List<A_10_parts> parts = new java.util.ArrayList<A_10_parts>();
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu-core2d/3.0rc3", localName = "geometry2D")
      @XmlElement(required = true)
      public _geometry2D geometry2D;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "heightBelowGround")
      @XmlElement(required = false)
      public NillableType<Double> heightBelowGround;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "numberOfFloorsBelowGround")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfFloorsBelowGround;
      private java.util.List<FloorRange> floorDistribution = new java.util.ArrayList<FloorRange>();
      private java.util.List<FloorDescription> floorDescription = new java.util.ArrayList<FloorDescription>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> roofType = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> materialOfFacade = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> materialOfRoof = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> materialOfStructure = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
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
      private java.util.List<Document> document = new java.util.ArrayList<Document>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "energyPerformance")
      @XmlElement(required = false)
      public A_23_energyPerformance energyPerformance;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> heatingSource = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> heatingSystem = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<AddressRepresentation> address = new java.util.ArrayList<AddressRepresentation>();
      private java.util.List<OfficialArea> officialArea = new java.util.ArrayList<OfficialArea>();
      private java.util.List<OfficialValue> officialValue = new java.util.ArrayList<OfficialValue>();
      private java.util.List<AddressRepresentation> mixinaddress = new java.util.ArrayList<AddressRepresentation>();
      private java.util.List<CadastralParcel> cadastralParcel = new java.util.ArrayList<CadastralParcel>();

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
      public List<Elevation> getElevation()
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
      public List<ExternalReference> getExternalReference()
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
      public List<HeightAboveGround> getHeightAboveGround()
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
      public List<GeographicalName> getName()
      {
         return name;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "buildingNature")
      @XmlElement(required = false)
      public void setBuildingNature(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            buildingNature.addAll(list);
         }
         else
         {
            buildingNature.clear();
         }
      }

      @JsonGetter
      public List<Reference> getBuildingNature()
      {
         return buildingNature;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "currentUse")
      @XmlElement(required = false)
      public void setCurrentUse(final java.util.List<CurrentUse> list)
      {
         if (list != null)
         {
            currentUse.addAll(list);
         }
         else
         {
            currentUse.clear();
         }
      }

      @JsonGetter
      public List<CurrentUse> getCurrentUse()
      {
         return currentUse;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "parts")
      @XmlElement(required = false)
      public void setParts(final java.util.List<A_10_parts> list)
      {
         if (list != null)
         {
            parts.addAll(list);
         }
         else
         {
            parts.clear();
         }
      }

      @JsonGetter
      public List<A_10_parts> getParts()
      {
         return parts;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "floorDistribution")
      @XmlElement(required = false)
      public void setFloorDistribution(final java.util.List<FloorRange> list)
      {
         if (list != null)
         {
            floorDistribution.addAll(list);
         }
         else
         {
            floorDistribution.clear();
         }
      }

      @JsonGetter
      public List<FloorRange> getFloorDistribution()
      {
         return floorDistribution;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "floorDescription")
      @XmlElement(required = false)
      public void setFloorDescription(
            final java.util.List<FloorDescription> list)
      {
         if (list != null)
         {
            floorDescription.addAll(list);
         }
         else
         {
            floorDescription.clear();
         }
      }

      @JsonGetter
      public List<FloorDescription> getFloorDescription()
      {
         return floorDescription;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "roofType")
      @XmlElement(required = false)
      public void setRoofType(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            roofType.addAll(list);
         }
         else
         {
            roofType.clear();
         }
      }

      @JsonGetter
      public List<Reference> getRoofType()
      {
         return roofType;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "materialOfFacade")
      @XmlElement(required = false)
      public void setMaterialOfFacade(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            materialOfFacade.addAll(list);
         }
         else
         {
            materialOfFacade.clear();
         }
      }

      @JsonGetter
      public List<Reference> getMaterialOfFacade()
      {
         return materialOfFacade;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "materialOfRoof")
      @XmlElement(required = false)
      public void setMaterialOfRoof(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            materialOfRoof.addAll(list);
         }
         else
         {
            materialOfRoof.clear();
         }
      }

      @JsonGetter
      public List<Reference> getMaterialOfRoof()
      {
         return materialOfRoof;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "materialOfStructure")
      @XmlElement(required = false)
      public void setMaterialOfStructure(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            materialOfStructure.addAll(list);
         }
         else
         {
            materialOfStructure.clear();
         }
      }

      @JsonGetter
      public List<Reference> getMaterialOfStructure()
      {
         return materialOfStructure;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "document")
      @XmlElement(required = false)
      public void setDocument(final java.util.List<Document> list)
      {
         if (list != null)
         {
            document.addAll(list);
         }
         else
         {
            document.clear();
         }
      }

      @JsonGetter
      public List<Document> getDocument()
      {
         return document;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "heatingSource")
      @XmlElement(required = false)
      public void setHeatingSource(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            heatingSource.addAll(list);
         }
         else
         {
            heatingSource.clear();
         }
      }

      @JsonGetter
      public List<Reference> getHeatingSource()
      {
         return heatingSource;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "heatingSystem")
      @XmlElement(required = false)
      public void setHeatingSystem(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            heatingSystem.addAll(list);
         }
         else
         {
            heatingSystem.clear();
         }
      }

      @JsonGetter
      public List<Reference> getHeatingSystem()
      {
         return heatingSystem;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "address")
      @XmlElement(required = false)
      public void setAddress(final java.util.List<AddressRepresentation> list)
      {
         if (list != null)
         {
            address.addAll(list);
         }
         else
         {
            address.clear();
         }
      }

      @JsonGetter
      public List<AddressRepresentation> getAddress()
      {
         return address;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "officialArea")
      @XmlElement(required = false)
      public void setOfficialArea(final java.util.List<OfficialArea> list)
      {
         if (list != null)
         {
            officialArea.addAll(list);
         }
         else
         {
            officialArea.clear();
         }
      }

      @JsonGetter
      public List<OfficialArea> getOfficialArea()
      {
         return officialArea;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "officialValue")
      @XmlElement(required = false)
      public void setOfficialValue(final java.util.List<OfficialValue> list)
      {
         if (list != null)
         {
            officialValue.addAll(list);
         }
         else
         {
            officialValue.clear();
         }
      }

      @JsonGetter
      public List<OfficialValue> getOfficialValue()
      {
         return officialValue;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "mixinaddress")
      @XmlElement(required = false)
      public void setMixinaddress(
            final java.util.List<AddressRepresentation> list)
      {
         if (list != null)
         {
            mixinaddress.addAll(list);
         }
         else
         {
            mixinaddress.clear();
         }
      }

      @JsonGetter
      public List<AddressRepresentation> getMixinaddress()
      {
         return mixinaddress;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0", localName = "cadastralParcel")
      @XmlElement(required = false)
      public void setCadastralParcel(
            final java.util.List<CadastralParcel> list)
      {
         if (list != null)
         {
            cadastralParcel.addAll(list);
         }
         else
         {
            cadastralParcel.clear();
         }
      }

      @JsonGetter
      public List<CadastralParcel> getCadastralParcel()
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

   public static class A_2_dateOfDemolition extends Nillable
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

   public static class A_3_dateOfRenovation extends Nillable
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

   public static class A_4_elevation extends Nillable
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

   public static class A_5_externalReference extends Nillable
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
      public _informationSystemName informationSystemName;
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
      private java.util.List<_textGroup> textGroup = new java.util.ArrayList<_textGroup>();

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "textGroup")
      @XmlElement(required = true)
      public void setTextGroup(final java.util.List<_textGroup> list)
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
      public List<_textGroup> getTextGroup()
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

   public static class A_6_heightAboveGround extends Nillable
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

   public static class A_7_name extends Nillable
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
      public A_8_pronunciation pronunciation;
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
      public List<SpellingOfName> getSpelling()
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

   public static class A_9_currentUse extends Nillable
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "currentUse");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "CurrentUse")
      @XmlElement(required = true)
      public CurrentUse CurrentUse;
   }

   public static class CurrentUse
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "CurrentUse");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "currentUse")
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      public NillableType<Calendar> beginLifespanVersion;
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
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> buildingNature = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<CurrentUse> currentUse = new java.util.ArrayList<CurrentUse>();
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
      public List<Elevation> getElevation()
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
      public List<ExternalReference> getExternalReference()
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
      public List<HeightAboveGround> getHeightAboveGround()
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
      public List<GeographicalName> getName()
      {
         return name;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "buildingNature")
      @XmlElement(required = false)
      public void setBuildingNature(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            buildingNature.addAll(list);
         }
         else
         {
            buildingNature.clear();
         }
      }

      @JsonGetter
      public List<Reference> getBuildingNature()
      {
         return buildingNature;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "currentUse")
      @XmlElement(required = false)
      public void setCurrentUse(final java.util.List<CurrentUse> list)
      {
         if (list != null)
         {
            currentUse.addAll(list);
         }
         else
         {
            currentUse.clear();
         }
      }

      @JsonGetter
      public List<CurrentUse> getCurrentUse()
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
      public CurrentUse CurrentUse;
   }

   public static class _geometry2D
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu-core2d/3.0rc3";
      public static final QName QN = new QName(NS, "geometry2D");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "BuildingGeometry2D")
      @XmlElement(required = true)
      public BuildingGeometry2D BuildingGeometry2D;
   }

   public static class BuildingGeometry2D
   {
      public static final String NS = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3";
      public static final QName QN = new QName(NS, "BuildingGeometry2D");
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "geometry")
      @XmlElement(required = true)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "referenceGeometry")
      @XmlElement(required = true)
      public Boolean referenceGeometry;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "horizontalGeometryReference")
      @XmlElement(required = true)
      public Reference horizontalGeometryReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "verticalGeometryReference")
      @XmlElement(required = false)
      public Reference verticalGeometryReference;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "horizontalGeometryEstimatedAccuracy")
      @XmlElement(required = false)
      public NillableType<Double> horizontalGeometryEstimatedAccuracy;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3", localName = "verticalGeometryEstimatedAccuracy")
      @XmlElement(required = false)
      public NillableType<Double> verticalGeometryEstimatedAccuracy;
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
      @XmlElement(required = true)
      public FloorRange FloorRange;
   }

   public static class FloorRange
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "FloorRange");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "lowestFloor")
      @XmlElement(required = true)
      public Double lowestFloor;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "highestFloor")
      @XmlElement(required = true)
      public Double highestFloor;
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
      @XmlElement(required = true)
      public FloorDescription FloorDescription;
   }

   public static class FloorDescription
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "FloorDescription");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "areaOfOpenings")
      @XmlElement(required = false)
      public NillableType<Double> areaOfOpenings;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> currentUse = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      private java.util.List<Document> document = new java.util.ArrayList<Document>();
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "floorArea")
      @XmlElement(required = false)
      public NillableType<Double> floorArea;
      private java.util.List<FloorRange> floorRange = new java.util.ArrayList<FloorRange>();
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "height")
      @XmlElement(required = false)
      public NillableType<Double> height;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "numberOfDwellings")
      @XmlElement(required = false)
      public NillableType<BigInteger> numberOfDwellings;

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "currentUse")
      @XmlElement(required = false)
      public void setCurrentUse(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            currentUse.addAll(list);
         }
         else
         {
            currentUse.clear();
         }
      }

      @JsonGetter
      public List<Reference> getCurrentUse()
      {
         return currentUse;
      }

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "document")
      @XmlElement(required = false)
      public void setDocument(final java.util.List<Document> list)
      {
         if (list != null)
         {
            document.addAll(list);
         }
         else
         {
            document.clear();
         }
      }

      @JsonGetter
      public List<Document> getDocument()
      {
         return document;
      }

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "floorRange")
      @XmlElement(required = true)
      public void setFloorRange(final java.util.List<FloorRange> list)
      {
         if (list != null)
         {
            floorRange.addAll(list);
         }
         else
         {
            floorRange.clear();
         }
      }

      @JsonGetter
      public List<FloorRange> getFloorRange()
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
      @XmlElement(required = true)
      public Document Document;
   }

   public static class Document
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "Document");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "date")
      @XmlElement(required = false)
      public NillableType<Calendar> date;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "documentDescription")
      @XmlElement(required = false)
      public _documentDescription documentDescription;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "documentLink")
      @XmlElement(required = true)
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
      @XmlElement(required = true)
      public String CharacterString;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "PT_FreeText")
      @XmlElement(required = true)
      public PT_FreeText PT_FreeText;
   }

   public static class _floorRange
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "floorRange");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "FloorRange")
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
      public EnergyPerformance EnergyPerformance;
   }

   public static class EnergyPerformance
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3";
      public static final QName QN = new QName(NS, "EnergyPerformance");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "energyPerformanceValue")
      @XmlElement(required = true)
      public Reference energyPerformanceValue;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "dateOfAssessment")
      @XmlElement(required = false)
      public NillableType<Calendar> dateOfAssessment;
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
      @XmlElement(required = true)
      public DocumentCitation DocumentCitation;
   }

   public static class DocumentCitation
   {
      public static final String NS = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3";
      public static final QName QN = new QName(NS, "DocumentCitation");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "name")
      @XmlElement(required = true)
      public String name;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "shortName")
      @XmlElement(required = false)
      public NillableType<String> shortName;
      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "date")
      @XmlElement(required = false)
      public Object date;
      private java.util.List<java.net.URI> link = new java.util.ArrayList<java.net.URI>();
      private java.util.List<java.lang.String> specificReference = new java.util.ArrayList<java.lang.String>();

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "link")
      @XmlElement(required = false)
      public void setLink(final java.util.List<java.net.URI> list)
      {
         if (list != null)
         {
            link.addAll(list);
         }
         else
         {
            link.clear();
         }
      }

      @JsonGetter
      public List<URI> getLink()
      {
         return link;
      }

      @JacksonXmlProperty(namespace = "http://inspire.ec.europa.eu/schemas/base2/1.0rc3", localName = "specificReference")
      @XmlElement(required = false)
      public void setSpecificReference(
            final java.util.List<java.lang.String> list)
      {
         if (list != null)
         {
            specificReference.addAll(list);
         }
         else
         {
            specificReference.clear();
         }
      }

      @JsonGetter
      public List<String> getSpecificReference()
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
      @XmlElement(required = true)
      public AddressRepresentation AddressRepresentation;
   }

   public static class AddressRepresentation
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "AddressRepresentation");
      private java.util.List<GeographicalName> adminUnit = new java.util.ArrayList<GeographicalName>();
      private java.util.List<java.lang.String> locatorDesignator = new java.util.ArrayList<java.lang.String>();
      private java.util.List<GeographicalName> locatorName = new java.util.ArrayList<GeographicalName>();
      private java.util.List<GeographicalName> addressArea = new java.util.ArrayList<GeographicalName>();
      private java.util.List<GeographicalName> postName = new java.util.ArrayList<GeographicalName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "postCode")
      @XmlElement(required = false)
      public NillableType<String> postCode;
      private java.util.List<GeographicalName> thoroughfare = new java.util.ArrayList<GeographicalName>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "addressFeature")
      @XmlElement(required = false)
      public Reference addressFeature;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "adminUnit")
      @XmlElement(required = true)
      public void setAdminUnit(final java.util.List<GeographicalName> list)
      {
         if (list != null)
         {
            adminUnit.addAll(list);
         }
         else
         {
            adminUnit.clear();
         }
      }

      @JsonGetter
      public List<GeographicalName> getAdminUnit()
      {
         return adminUnit;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "locatorDesignator")
      @XmlElement(required = false)
      public void setLocatorDesignator(
            final java.util.List<java.lang.String> list)
      {
         if (list != null)
         {
            locatorDesignator.addAll(list);
         }
         else
         {
            locatorDesignator.clear();
         }
      }

      @JsonGetter
      public List<String> getLocatorDesignator()
      {
         return locatorDesignator;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "locatorName")
      @XmlElement(required = false)
      public void setLocatorName(final java.util.List<GeographicalName> list)
      {
         if (list != null)
         {
            locatorName.addAll(list);
         }
         else
         {
            locatorName.clear();
         }
      }

      @JsonGetter
      public List<GeographicalName> getLocatorName()
      {
         return locatorName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "addressArea")
      @XmlElement(required = false)
      public void setAddressArea(final java.util.List<GeographicalName> list)
      {
         if (list != null)
         {
            addressArea.addAll(list);
         }
         else
         {
            addressArea.clear();
         }
      }

      @JsonGetter
      public List<GeographicalName> getAddressArea()
      {
         return addressArea;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "postName")
      @XmlElement(required = false)
      public void setPostName(final java.util.List<GeographicalName> list)
      {
         if (list != null)
         {
            postName.addAll(list);
         }
         else
         {
            postName.clear();
         }
      }

      @JsonGetter
      public List<GeographicalName> getPostName()
      {
         return postName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Addresses:3.0", localName = "thoroughfare")
      @XmlElement(required = false)
      public void setThoroughfare(final java.util.List<GeographicalName> list)
      {
         if (list != null)
         {
            thoroughfare.addAll(list);
         }
         else
         {
            thoroughfare.clear();
         }
      }

      @JsonGetter
      public List<GeographicalName> getThoroughfare()
      {
         return thoroughfare;
      }
   }

   public static class _adminUnit
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "adminUnit");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = true)
      public GeographicalName GeographicalName;
   }

   public static class _locatorName
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:Addresses:3.0";
      public static final QName QN = new QName(NS, "locatorName");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
      public Double value;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "heightParameter")
      @XmlElement(required = false)
      public NillableType<Double> heightParameter;
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
      @XmlElement(required = true)
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
      public NillableType<Calendar> valuationDate;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "officialValueReference")
      @XmlElement(required = false)
      public Reference officialValueReference;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "referencePercentage")
      @XmlElement(required = false)
      public NillableType<BigInteger> referencePercentage;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/bu-ext/3.0rc3", localName = "informationSystemName")
      @XmlElement(required = false)
      public _informationSystemName informationSystemName;
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
      @XmlElement(required = true)
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
      @XmlElement(required = true)
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
      public NillableType<Double> areaValue;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<Calendar> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "geometry")
      @XmlElement(required = true)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "inspireId")
      @XmlElement(required = true)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "label")
      @XmlElement(required = true)
      public String label;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "nationalCadastralReference")
      @XmlElement(required = true)
      public String nationalCadastralReference;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "referencePoint")
      @XmlElement(required = false)
      public _referencePoint referencePoint;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<Calendar> validFrom;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "validTo")
      @XmlElement(required = false)
      public NillableType<Calendar> validTo;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "zoning")
      @XmlElement(required = false)
      public Reference zoning;
      private java.util.List<fi.nls.oskari.fe.xml.util.Reference> basicPropertyUnit = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "administrativeUnit")
      @XmlElement(required = false)
      public Reference administrativeUnit;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "basicPropertyUnit")
      @XmlElement(required = false)
      public void setBasicPropertyUnit(
            final java.util.List<fi.nls.oskari.fe.xml.util.Reference> list)
      {
         if (list != null)
         {
            basicPropertyUnit.addAll(list);
         }
         else
         {
            basicPropertyUnit.clear();
         }
      }

      @JsonGetter
      public List<Reference> getBasicPropertyUnit()
      {
         return basicPropertyUnit;
      }
   }

   public static class _referencePoint extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0";
      public static final QName QN = new QName(NS, "referencePoint");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "Point")
      @XmlElement(required = true)
      public GeometryProperty Point;
   }
}
