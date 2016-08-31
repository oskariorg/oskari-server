package fi.nls.oskari.search.village;

public class Village {
	private String kuntaNro;
	private String fi;
	private String sv;
	private String wgs84wkt;
	private String yleistetty_bbox;
	
	public String toString() {
		return "kuntaNro=" + kuntaNro + ", fi=" + fi + ", sv=" + sv 
		+ ", wgs84wkt=" + wgs84wkt + ", yleistetty_bbox=" + yleistetty_bbox;
	}
	
	public String getKuntaNro() {
		return kuntaNro;
	}
	public void setKuntaNro(String kuntaNro) {
		this.kuntaNro = kuntaNro;
	}
    // FIXME no hardcoded locales, add locale column to village table
    public String getName(String locale) {
        if ("sv".equals(locale)) {
            return sv;
        } else {
            return fi;
        }
    }

    public void setName(String locale, String name) {
        if ("fi".equals(locale)) {
            fi = name;
        } else if ("sv".equals(locale)) {
            sv = name;
        }
    }

    @Deprecated
	public String getNameFi() {
		return fi;
	}
    @Deprecated
	public void setNameFi(String nameFi) {
		this.fi = nameFi;
	}
    @Deprecated
	public String getNameSv() {
		return sv;
	}
    @Deprecated
	public void setNameSv(String nameSv) {
		this.sv = nameSv;
	}
	public String getWgs84wkt() {
		return wgs84wkt;
	}
	public void setWgs84wkt(String wgs84wkt) {
		this.wgs84wkt = wgs84wkt;
	}
	public String getYleistetty_bbox() {
		return yleistetty_bbox;
	}
	public void setYleistetty_bbox(String yleistettyBbox) {
		yleistetty_bbox = yleistettyBbox;
	}
}
