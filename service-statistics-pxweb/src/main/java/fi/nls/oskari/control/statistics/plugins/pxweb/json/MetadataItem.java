package fi.nls.oskari.control.statistics.plugins.pxweb.json;

import java.util.Map;

/*
{
    "code": "M408",
    "desc": {
      "fi": "Taajama-aste tarkoittaa taajamissa asuvien osuutta väestöstä, jonka sijainti tunnetaan. Taajamaksi määritellään kaikki vähintään 200 asukkaan rakennusryhmät, joissa rakennusten välinen etäisyys ei yleensä ole 200 metriä suurempi"
    },
    "source": {
      "fi": "Väestörakenne"
    },
    "valueType": "percentage",
    "decimalCount": 1,
    "timerange": {
      "start": "1987",
      "end": "2015"
    },
    "updated": "1.4.2016",
    "nextUpdate": "29.3.2017"
}
 */
public class MetadataItem {

    public String code;
    public Map<String,String> name;
    public Map<String,String> desc;
    public Map<String,String> source;
    public String valueType;

    public int decimalCount;
    public String updated;
    public String nextUpdate;

    public Timerange timerange;

    public static class Timerange {
        public String start;
        public String end;
    }

    public String getName(String lang) {
        if (name == null) {
            return null;
        }
        return name.get(lang);
    }
    public String getDesc(String lang) {
        if (desc == null) {
            return null;
        }
        return desc.get(lang);
    }
    public String getSource(String lang) {
        if (source == null) {
            return null;
        }
        return source.get(lang);
    }
}
