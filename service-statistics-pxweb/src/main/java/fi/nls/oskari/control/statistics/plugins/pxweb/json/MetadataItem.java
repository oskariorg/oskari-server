package fi.nls.oskari.control.statistics.plugins.pxweb.json;

import java.util.Set;

/*
    {
        "code": "M495",
        "name": "Konsernin lainakanta euroa/asukas",
        "source": "Kuntatalous",
        "lyhenne": "kta",
        "labels": ["Julkinen talous"],
        "prio": 29,
        "updated": "4.11.2016",
        "nextUpdate": "2.6.2017",
        "timerange": {
            "start": "2008",
            "end": "2014"
        },
        "desc": "",
        "decimalCount": 1,
        "valueType": "suhde",
        "regionsets": ["Kunta","Maakunta","Seutukunta"]
    }
 */
public class MetadataItem {

    public String code;
    public String name;
    public String desc;
    public String source;
    public String valueType;

    public Set<String> labels;
    public Set<String> regionsets;
    public int prio;
    public int decimalCount;
    public String updated;
    public String nextUpdate;

    public Timerange timerange;

    public static class Timerange {
        public String start;
        public String end;
    }
}
