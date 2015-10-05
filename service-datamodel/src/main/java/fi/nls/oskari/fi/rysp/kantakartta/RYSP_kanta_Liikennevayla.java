package fi.nls.oskari.fi.rysp.kantakartta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fi.rysp.kantakartta.util.RYSP_kanta_referenssipiste;
import fi.nls.oskari.fi.rysp.kantakartta.util.RYSP_kanta_siirtymasijainti;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

public class RYSP_kanta_Liikennevayla
{

   @JacksonXmlRootElement(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta")
   public static class Liikennevayla
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Liikennevayla");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @XmlElement(required = false)
      public java.util.List<_metatieto> metatieto = new java.util.ArrayList<_metatieto>();
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "tunnus")
      @XmlElement(required = false)
      public String tunnus;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kplkoodi")
      @XmlElement(required = false)
      public String kplkoodi;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "yksilointitieto")
      @XmlElement(required = false)
      public String yksilointitieto;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "sijaintiepavarmuus")
      @XmlElement(required = false)
      public String sijaintiepavarmuus;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "luontitapa")
      @XmlElement(required = false)
      public String luontitapa;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "alkuPvm")
      @XmlElement(required = false)
      public String alkuPvm;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "loppuPvm")
      @XmlElement(required = false)
      public String loppuPvm;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "labelit")
      @XmlElement(required = false)
      public Label labelit;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "sijainnit")
      @XmlElement(required = false)
      public _sijainnit sijainnit;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "pinta")
      @XmlElement(required = false)
      public String pinta;

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "metatieto")
      public void setMetatieto(final _metatieto obj)
      {
         if (obj != null)
         {
            metatieto.add(obj);
         }
      }

      java.util.List<_metatieto> getMetatieto()
      {
         return metatieto;
      }
   }

   public static class _metatieto
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "metatieto");
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "about")
      public String about;
   }

   public static class _labelit
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "labelit");
      @XmlElement(required = false)
      public java.util.List<Label> Label = new java.util.ArrayList<Label>();

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "Label")
      public void setLabel(final Label obj)
      {
         if (obj != null)
         {
            Label.add(obj);
         }
      }

      java.util.List<Label> getLabel()
      {
         return Label;
      }
   }

   public static class Label
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Label");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "ilmentymaElementinNimi")
      @XmlElement(required = false)
      public String ilmentymaElementinNimi;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kayttotarkoitus")
      @XmlElement(required = false)
      public String kayttotarkoitus;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "suunta")
      @XmlElement(required = false)
      public String suunta;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kohta")
      @XmlElement(required = false)
      public String kohta;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "siirtymasijainti")
      @XmlElement(required = false)
      //public GeometryProperty siirtymasijainti;
      public RYSP_kanta_siirtymasijainti siirtymasijainti;
   }

   public static class _sijainnit
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "sijainnit");
      @XmlElement(required = false)
      public java.util.List<Sijainti> Sijainti = new java.util.ArrayList<Sijainti>();
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml", localName = "Null")
      @XmlElement(required = false)
      public Null Null;

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "Sijainti")
      public void setSijainti(final Sijainti obj)
      {
         if (obj != null)
         {
            Sijainti.add(obj);
         }
      }

      java.util.List<Sijainti> getSijainti()
      {
         return Sijainti;
      }
   }

   public static class Sijainti
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Sijainti");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "keskilinja")
      @XmlElement(required = false)
      public GeometryProperty keskilinja;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "referenssipiste")
      @XmlElement(required = false)
      //public _referenssipiste referenssipiste;
      public RYSP_kanta_referenssipiste referenssipiste;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "reunaviiva")
      @XmlElement(required = false)
      public GeometryProperty reunaviiva;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "alue")
      @XmlElement(required = false)
      public GeometryProperty alue;
   }

   public static class _referenssipiste
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "referenssipiste");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml", localName = "Point")
      @XmlElement(required = false)
      public GeometryProperty Point;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "Suunta")
      @XmlElement(required = false)
      public Suunta Suunta;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "Siirtyma")
      @XmlElement(required = false)
      public Siirtyma Siirtyma;
   }

   public static class Suunta
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Suunta");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "suuntakulma")
      @XmlElement(required = false)
      public String suuntakulma;
   }

   public static class Siirtyma
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Siirtyma");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "siirtymasijainti")
      @XmlElement(required = false)
//      public GeometryProperty siirtymasijainti;
      public RYSP_kanta_siirtymasijainti siirtymasijainti;
   }

   public static class Null
   {
      public static final String NS = "http://www.opengis.net/gml";
      public static final QName QN = new QName(NS, "Null");
   }
}
