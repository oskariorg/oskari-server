package fi.mml.portti.domain.permissions;

public class TermsOfUseLicense  implements Comparable<TermsOfUseUser> {

	
	int id;
	String name;
	String licenseUrl;
	
	
	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}


	public String getLicenseUrl() {
		return licenseUrl;
	}


	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}






	@Override
	public int compareTo(TermsOfUseUser arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
