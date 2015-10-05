package fi.nls.oskari.fi.rysp.kantakartta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fi.rysp.kantakartta.util.RYSP_kanta_referenssipiste;
import fi.nls.oskari.fi.rysp.kantakartta.util.RYSP_kanta_siirtymasijainti;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.util.Date;

/**
 * 
- URL http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd
- timestamp Fri Dec 19 08:47:51 EET 2014
 */
public class RYSP_kanta_Rakennus
{

   public static final String TIMESTAMP = "Fri Dec 19 08:47:51 EET 2014";
   public static final String SCHEMASOURCE = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";

   @JacksonXmlRootElement(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta")
   public static class Rakennus extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Rakennus");
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
      public _labelit labelit;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "sijainnit")
      @XmlElement(required = false)
      public _sijainnit sijainnit;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "tila")
      @XmlElement(required = false)
      public String tila;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "rakennustunnus")
      @XmlElement(required = false)
      public String rakennustunnus;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kottovuosi")
      @XmlElement(required = false)
      public Date kottovuosi;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "rakennuksenKayttotarkoitus")
      @XmlElement(required = false)
      public String rakennuksenKayttotarkoitus;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kayttotarkoitusmerkinta")
      @XmlElement(required = false)
      public String kayttotarkoitusmerkinta;
      @XmlElement(required = false)
      public java.util.List<java.lang.String> julkisivumateriaali = new java.util.ArrayList<java.lang.String>();
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kerrosluku")
      @XmlElement(required = false)
      public String kerrosluku;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "korkeusasema")
      @XmlElement(required = false)
      public String korkeusasema;
      @XmlElement(required = false)
      public java.util.List<_osoite> osoite = new java.util.ArrayList<_osoite>();
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "rakennuksenosat")
      @XmlElement(required = false)
      public A_1_rakennuksenosat rakennuksenosat;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "kayttotarkoituskoodi")
      @XmlElement(required = false)
      public String kayttotarkoituskoodi;

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

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "julkisivumateriaali")
      public void setJulkisivumateriaali(final java.lang.String obj)
      {
         if (obj != null)
         {
            julkisivumateriaali.add(obj);
         }
      }

      java.util.List<java.lang.String> getJulkisivumateriaali()
      {
         return julkisivumateriaali;
      }

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "osoite")
      public void setOsoite(final _osoite obj)
      {
         if (obj != null)
         {
            osoite.add(obj);
         }
      }

      java.util.List<_osoite> getOsoite()
      {
         return osoite;
      }
   }

   public static class _metatieto extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "metatieto");
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "about")
      public String about;
   }

   public static class _labelit extends Nillable
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

   public static class Label extends Nillable
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
      public RYSP_kanta_siirtymasijainti siirtymasijainti;
   }

   public static class _sijainnit extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "sijainnit");
      @XmlElement(required = false)
      public java.util.List<Sijainti> Sijainti = new java.util.ArrayList<Sijainti>();
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml", localName = "Null")
      @XmlElement(required = false)
      public fi.nls.oskari.fe.gml.util.Null Null;

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

   public static class Sijainti extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Sijainti");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "keskilinja")
      @XmlElement(required = false)
      public GeometryProperty keskilinja;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "referenssipiste")
      @XmlElement(required = false)
      public RYSP_kanta_referenssipiste referenssipiste;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "reunaviiva")
      @XmlElement(required = false)
      public GeometryProperty reunaviiva;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "alue")
      @XmlElement(required = false)
      public GeometryProperty alue;
   }

   /*public static class _referenssipiste extends Nillable
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

   public static class Suunta extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Suunta");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "suuntakulma")
      @XmlElement(required = false)
      public String suuntakulma;
   }

   public static class Siirtyma extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "Siirtyma");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "siirtymasijainti")
      @XmlElement(required = false)
      public GeometryProperty siirtymasijainti;
   }
   */

   public static class _osoite extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "osoite");
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "kunta")
      @XmlElement(required = false)
      public String kunta;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "osoitenimi")
      @XmlElement(required = false)
      public _osoitenimi osoitenimi;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "osoitenumero")
      @XmlElement(required = false)
      public Integer osoitenumero;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "osoitenumero2")
      @XmlElement(required = false)
      public Integer osoitenumero2;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "jakokirjain")
      @XmlElement(required = false)
      public String jakokirjain;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "jakokirjain2")
      @XmlElement(required = false)
      public String jakokirjain2;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "porras")
      @XmlElement(required = false)
      public String porras;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "huoneisto")
      @XmlElement(required = false)
      public Integer huoneisto;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "postinumero")
      @XmlElement(required = false)
      public String postinumero;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "postitoimipaikannimi")
      @XmlElement(required = false)
      public String postitoimipaikannimi;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "jarjestysnumero")
      @XmlElement(required = false)
      public Integer jarjestysnumero;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "pistesijainti")
      @XmlElement(required = false)
      public GeometryProperty pistesijainti;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "aluesijainti")
      @XmlElement(required = false)
      public GeometryProperty aluesijainti;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "viivasijainti")
      @XmlElement(required = false)
      public GeometryProperty viivasijainti;
   }

   public static class _osoitenimi extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/yhteiset";
      public static final QName QN = new QName(NS, "osoitenimi");
      @XmlElement(required = false)
      public java.util.List<java.lang.String> teksti = new java.util.ArrayList<java.lang.String>();

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/yhteiset", localName = "teksti")
      public void setTeksti(final java.lang.String obj)
      {
         if (obj != null)
         {
            teksti.add(obj);
         }
      }

      java.util.List<java.lang.String> getTeksti()
      {
         return teksti;
      }
   }
   
   public static class A_1_featureMember extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "rakennuksenosat");
      @XmlElement(required = false)
      public java.util.List<RakennuksenOsa> RakennuksenOsa = new java.util.ArrayList<RakennuksenOsa>();

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "RakennuksenOsa")
      public void setRakennuksenOsa(final RakennuksenOsa obj)
      {
         if (obj != null)
         {
            RakennuksenOsa.add(obj);
         }
      }

      java.util.List<RakennuksenOsa> getRakennuksenOsa()
      {
         return RakennuksenOsa;
      }
   }


   public static class A_1_rakennuksenosat extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "rakennuksenosat");
      @XmlElement(required = false)
      public java.util.List<A_1_featureMember> featureMember = new java.util.ArrayList<A_1_featureMember>();

      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "featureMember")
      public void setFeatureMember(final A_1_featureMember obj)
      {
         if (obj != null)
         {
             featureMember.add(obj);
         }
      }

      java.util.List<A_1_featureMember> getFeatureMember()
      {
         return featureMember;
      }
   }

   public static class RakennuksenOsa extends Nillable
   {
      public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
      public static final QName QN = new QName(NS, "RakennuksenOsa");
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
      public _labelit labelit;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "sijainnit")
      @XmlElement(required = false)
      public _sijainnit sijainnit;
      @JacksonXmlProperty(namespace = "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName = "tyyppi")
      @XmlElement(required = false)
      public String tyyppi;

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
}
