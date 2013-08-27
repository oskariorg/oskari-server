package fi.nls.oskari.domain.map;

public class InspireTheme {

	private int id;
	
	private String nameFi;
	
	private String nameSv;
	
	private String nameEn;

    public String getName(final String language) {
        // first step of making it better
        // TODO: next step -> populate a map<lang, name> from db
        if ("sv".equals(language)) {
            return this.nameSv;
        } else if ("en".equals(language)) {
            return this.nameEn;
        } else {
            return this.nameFi;
        }
    }
    
	public String getNameFi() {
		return nameFi;
	}
	public void setNameFi(String nameFi) {
		this.nameFi = nameFi;
	}
	public String getNameSv() {
		return nameSv;
	}
	public void setNameSv(String nameSv) {
		this.nameSv = nameSv;
	}
	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}	
}
