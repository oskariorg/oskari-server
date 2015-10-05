package fi.nls.oskari.eu.inspire.hydrophysicalwaters;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.isotc211.gmd.LocalisedCharacterString;
import fi.nls.oskari.isotc211.gmd.MD_Resolution;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.net.URI;

/**
 * 
- URL http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd
- timestamp Wed Dec 17 12:43:06 EET 2014
 */
public class INSPIRE_hyp_Watercourse
{

   public static final String TIMESTAMP = "Wed Dec 17 12:43:06 EET 2014";
   public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd";

   @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0")
   public static class Watercourse extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "Watercourse");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @XmlElement(required = false)
      public java.util.List<A_1_geographicalName> geographicalName = new java.util.ArrayList<A_1_geographicalName>();
      @XmlElement(required = false)
      public java.util.List<A_3_hydroId> hydroId = new java.util.ArrayList<A_3_hydroId>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> relatedHydroObject = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public _inspireId inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "levelOfDetail")
      @XmlElement(required = false)
      public A_4_levelOfDetail levelOfDetail;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "localType")
      @XmlElement(required = false)
      public _localType localType;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "origin")
      @XmlElement(required = false)
      public NillableType<String> origin;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "persistence")
      @XmlElement(required = false)
      public NillableType<String> persistence;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "tidal")
      @XmlElement(required = false)
      public NillableType<Boolean> tidal;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> bank = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> drainsBasin = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> neighbour = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "condition")
      @XmlElement(required = false)
      public NillableType<String> condition;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "delineationKnown")
      @XmlElement(required = false)
      public NillableType<Boolean> delineationKnown;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "length")
      @XmlElement(required = false)
      public NillableType<String> length;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "level")
      @XmlElement(required = false)
      public NillableType<String> level;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "streamOrder")
      @XmlElement(required = false)
      public A_5_streamOrder streamOrder;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "width")
      @XmlElement(required = false)
      public A_6_width width;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "geographicalName")
      public void setGeographicalName(final A_1_geographicalName obj)
      {
         if (obj != null)
         {
            geographicalName.add(obj);
         }
      }

      java.util.List<A_1_geographicalName> getGeographicalName()
      {
         return geographicalName;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "hydroId")
      public void setHydroId(final A_3_hydroId obj)
      {
         if (obj != null)
         {
            hydroId.add(obj);
         }
      }

      java.util.List<A_3_hydroId> getHydroId()
      {
         return hydroId;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "relatedHydroObject")
      public void setRelatedHydroObject(
            final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            relatedHydroObject.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getRelatedHydroObject()
      {
         return relatedHydroObject;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "bank")
      public void setBank(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            bank.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getBank()
      {
         return bank;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "drainsBasin")
      public void setDrainsBasin(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            drainsBasin.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getDrainsBasin()
      {
         return drainsBasin;
      }

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "neighbour")
      public void setNeighbour(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            neighbour.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getNeighbour()
      {
         return neighbour;
      }
   }

   public static class A_1_geographicalName extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "geographicalName");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
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
      public NillableType<String> nativeness;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "nameStatus")
      @XmlElement(required = false)
      public NillableType<String> nameStatus;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "sourceOfName")
      @XmlElement(required = false)
      public NillableType<String> sourceOfName;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "pronunciation")
      @XmlElement(required = false)
      public A_2_pronunciation pronunciation;
      @XmlElement(required = false)
      public java.util.List<_spelling> spelling = new java.util.ArrayList<_spelling>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalGender")
      @XmlElement(required = false)
      public NillableType<String> grammaticalGender;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "grammaticalNumber")
      @XmlElement(required = false)
      public NillableType<String> grammaticalNumber;

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

   public static class A_2_pronunciation extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
      public static final QName QN = new QName(NS, "pronunciation");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
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

   public static class A_3_hydroId extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "hydroId");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroBase:3.0", localName = "HydroIdentifier")
      @XmlElement(required = false)
      public HydroIdentifier HydroIdentifier;
   }

   public static class HydroIdentifier extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroBase:3.0";
      public static final QName QN = new QName(NS, "HydroIdentifier");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroBase:3.0", localName = "classificationScheme")
      @XmlElement(required = false)
      public String classificationScheme;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroBase:3.0", localName = "localId")
      @XmlElement(required = false)
      public String localId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroBase:3.0", localName = "namespace")
      @XmlElement(required = false)
      public String namespace;
   }

   public static class _inspireId extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
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

   public static class A_4_levelOfDetail extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "levelOfDetail");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "MD_Resolution")
      @XmlElement(required = false)
      public MD_Resolution MD_Resolution;
   }

   public static class _localType extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "localType");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "LocalisedCharacterString")
      @XmlElement(required = false)
      public LocalisedCharacterString LocalisedCharacterString;
   }

   public static class A_5_streamOrder extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "streamOrder");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "HydroOrderCode")
      @XmlElement(required = false)
      public HydroOrderCode HydroOrderCode;
   }

   public static class HydroOrderCode extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "HydroOrderCode");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "order")
      @XmlElement(required = false)
      public String order;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "orderScheme")
      @XmlElement(required = false)
      public String orderScheme;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "scope")
      @XmlElement(required = false)
      public String scope;
   }

   public static class A_6_width extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "width");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "WidthRange")
      @XmlElement(required = false)
      public WidthRange WidthRange;
   }

   public static class WidthRange extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "WidthRange");
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "lower")
      @XmlElement(required = false)
      public String lower;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "upper")
      @XmlElement(required = false)
      public String upper;
   }
}
