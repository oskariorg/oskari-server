package fi.nls.oskari.eu.inspire.cadastralparcels;

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
- URL http://inspire.ec.europa.eu/schemas/cp/3.0/CadastralParcels.xsd
- timestamp Wed Dec 17 13:05:36 EET 2014
 */
public class INSPIRE_cp_CadastralBoundary
{

   public static final String TIMESTAMP = "Wed Dec 17 13:05:36 EET 2014";
   public static final String SCHEMASOURCE = "http://inspire.ec.europa.eu/schemas/cp/3.0/CadastralParcels.xsd";

   @JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0")
   public static class CadastralBoundary extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0";
      public static final QName QN = new QName(NS, "CadastralBoundary");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "estimatedAccuracy")
      @XmlElement(required = false)
      public NillableType<String> estimatedAccuracy;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "geometry")
      @XmlElement(required = false)
      public GeometryProperty geometry;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "inspireId")
      @XmlElement(required = false)
      public _inspireId inspireId;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "validFrom")
      @XmlElement(required = false)
      public NillableType<String> validFrom;
      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "validTo")
      @XmlElement(required = false)
      public NillableType<String> validTo;
      @XmlElement(required = false)
      public java.util.List<fi.nls.oskari.fe.xml.util.Reference> parcel = new java.util.ArrayList<fi.nls.oskari.fe.xml.util.Reference>();

      @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0", localName = "parcel")
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
   }

   public static class _inspireId extends Nillable
   {
      public static final String NS = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0";
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
