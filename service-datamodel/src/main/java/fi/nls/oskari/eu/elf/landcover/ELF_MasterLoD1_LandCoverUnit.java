package fi.nls.oskari.eu.elf.landcover;

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
import java.math.BigInteger;

/**
 * 
- URL http://elfserver.kartverket.no/schemas/elf1.0/LoD1_LandCover.xsd
- timestamp Wed Dec 17 10:39:58 EET 2014
 */
public class ELF_MasterLoD1_LandCoverUnit
{

   public static final String TIMESTAMP = "Wed Dec 17 10:39:58 EET 2014";
   public static final String SCHEMASOURCE = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_LandCover.xsd";

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/LandCover/MasterLoD1/1.0")
   public static class LandCoverUnit extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/LandCover/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "LandCoverUnit");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @XmlElement(required = false)
      public java.util.List<_landCoverObservation> landCoverObservation = new java.util.ArrayList<_landCoverObservation>();

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "landCoverObservation")
      public void setLandCoverObservation(final _landCoverObservation obj)
      {
         if (obj != null)
         {
            landCoverObservation.add(obj);
         }
      }

      java.util.List<_landCoverObservation> getLandCoverObservation()
      {
         return landCoverObservation;
      }
   }

   public static class _landCoverObservation extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0";
      public static final QName QN = new QName(NS, "landCoverObservation");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "LandCoverObservation")
      @XmlElement(required = false)
      public LandCoverObservation LandCoverObservation;
   }

   public static class LandCoverObservation extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0";
      public static final QName QN = new QName(NS, "LandCoverObservation");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "class")
      @XmlElement(required = false)
      public Reference _class;
      @XmlElement(required = false)
      public java.util.List<A_1_mosaic> mosaic = new java.util.ArrayList<A_1_mosaic>();
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "observationDate")
      @XmlElement(required = false)
      public NillableType<String> observationDate;

      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "mosaic")
      public void setMosaic(final A_1_mosaic obj)
      {
         if (obj != null)
         {
            mosaic.add(obj);
         }
      }

      java.util.List<A_1_mosaic> getMosaic()
      {
         return mosaic;
      }
   }

   public static class A_1_mosaic extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0";
      public static final QName QN = new QName(NS, "mosaic");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "LandCoverValue")
      @XmlElement(required = false)
      public LandCoverValue LandCoverValue;
   }

   public static class LandCoverValue extends Nillable
   {
      public static final String NS = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0";
      public static final QName QN = new QName(NS, "LandCoverValue");
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "class")
      @XmlElement(required = false)
      public Reference _class;
      @JacksonXmlProperty(namespace = "http://inspire.jrc.ec.europa.eu/schemas/lcv/3.0", localName = "coveredPercentage")
      @XmlElement(required = false)
      public NillableType<BigInteger> coveredPercentage;
   }
}
