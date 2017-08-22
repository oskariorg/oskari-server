package downloadbasket.data;

public class ErrorReportDetails {
	private String xmlRequest = null;
	private String wfsUrl = null;
	private String errorFileLocation = null;
	private String userEmail = null;
	private String language = null;

	public String getXmlRequest() {
		return xmlRequest;
	}

	public void setXmlRequest(String xmlRequest) {
		this.xmlRequest = xmlRequest;
	}

	public String getWfsUrl() {
		return wfsUrl;
	}

	public void setWfsUrl(String wfsUrl) {
		this.wfsUrl = wfsUrl;
	}

	public String getErrorFileLocation() {
		return errorFileLocation;
	}

	public void setErrorFileLocation(final String errorFileLocation) {
		this.errorFileLocation = errorFileLocation;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
