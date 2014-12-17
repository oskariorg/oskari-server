package fi.nls.oskari.eu.inspire.hydrophysicalwaters;

import java.net.URI;
import fi.nls.oskari.fe.gml.util.CodeType;
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
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.gml.util.GeometryProperty;

/**
 * 
- URL http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd
- timestamp Wed Dec 17 12:45:38 EET 2014
 */
public class INSPIRE_hyp_LandWaterBoundary
{

   public static final String TIMESTAMP = "Wed Dec 17 12:45:38 EET 2014";
   public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd";

   @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0")
   public static class LandWaterBoundary extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";
      public static final QName QN = new QName(NS, "LandWaterBoundary");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
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
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "origin")
      @XmlElement(required = false)
      public NillableType<String> origin;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0", localName = "waterLevelCategory")
      @XmlElement(required = false)
      public NillableType<String> waterLevelCategory;
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
}
