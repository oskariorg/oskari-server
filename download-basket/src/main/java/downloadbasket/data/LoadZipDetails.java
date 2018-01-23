package downloadbasket.data;

public class LoadZipDetails {
	private String getFeatureInfoRequest = null;
	private String wfsUrl = null;
	private String temporaryDirectory = null;
	private String userEmail = null;
	private String language = null;
	private boolean downloadNormalWay = true;

	public LoadZipDetails() {
	}

	public String getGetFeatureInfoRequest() {
		return this.getFeatureInfoRequest;
	}

	public void setGetFeatureInfoRequest(String getFeatureInfoRequest) {
		this.getFeatureInfoRequest = getFeatureInfoRequest;
	}

	public String getWFSUrl() {
		return this.wfsUrl;
	}

	public void setWFSUrl(final String wfsUrl) {
		this.wfsUrl = wfsUrl;
	}

	public String getTemporaryDirectory() {
		return this.temporaryDirectory;
	}

	public void setTemporaryDirectory(final String temporaryDirectory) {
		this.temporaryDirectory = temporaryDirectory;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public boolean isDownloadNormalWay() {
		return downloadNormalWay;
	}

	public void setDownloadNormalWay(boolean downloadNormalWay) {
		downloadNormalWay = downloadNormalWay;
	}
}