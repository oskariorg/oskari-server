package fi.nls.oskari.eu.elf.railwaytransportnetwork;

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
import java.net.URI;

/**
 * 
- URL http://elfserver.kartverket.no/schemas/elf1.0/LoD1_RailwayTransportNetwork.xsd
- timestamp Wed Dec 17 11:02:02 EET 2014
 */
public class ELF_MasterLoD1_RailwayLink
{

   public static final String TIMESTAMP = "Wed Dec 17 11:02:02 EET 2014";
   public static final String SCHEMASOURCE = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_RailwayTransportNetwork.xsd";

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/RailwayTransportNetwork/MasterLoD1/1.0")
   public static class RailwayLink extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/RailwayTransportNetwork/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "RailwayLink");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> inNetwork = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "centrelineGeometry")
      @XmlElement(required = false)
      public GeometryProperty centrelineGeometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "fictitious")
      @XmlElement(required = false)
      public Boolean fictitious;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "endNode")
      @XmlElement(required = false)
      public Reference endNode;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "startNode")
      @XmlElement(required = false)
      public Reference startNode;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0", localName = "geographicalName")
      @XmlElement(required = false)
      public A_1_geographicalName geographicalName;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<String> validFrom;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0", localName = "validTo")
      @XmlElement(required = false)
      public NillableType<String> validTo;

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:Network:3.2", localName = "inNetwork")
      public void setInNetwork(final fi.nls.oskari.fe.xml.util.Reference obj)
      {
         if (obj != null)
         {
            inNetwork.add(obj);
         }
      }

      java.util.List<fi.nls.oskari.fe.xml.util.Reference> getInNetwork()
      {
         return inNetwork;
      }
   }

   public static class A_1_geographicalName extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0";
      public static final QName QN = new QName(NS, "geographicalName");
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
      public A_2_pronunciation pronunciation;
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

   public static class A_2_pronunciation extends Nillable
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
