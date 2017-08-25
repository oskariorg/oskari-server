package downloadbasket.data;

public class LoadZipDetails {
	private String getFeatureInfoRequest = null;
	private String wFSUrl = null;
	private String temporaryDirectory = null;
	private String userEmail = null;
	private String language = null;
	private boolean downloadNormalWay = true;

	public LoadZipDetails() {
	}

	public String getGetFeatureInfoRequest() {
		return getFeatureInfoRequest;
	}

	public void setGetFeatureInfoRequest(String getFeatureInfoRequest) {
		getFeatureInfoRequest = getFeatureInfoRequest;
	}

	public String getWFSUrl() {
		return wFSUrl;
	}

	public void setWFSUrl(final String wFSUrl) {
		wFSUrl = wFSUrl;
	}

	public String getTemporaryDirectory() {
		return temporaryDirectory;
	}

	public void setTemporaryDirectory(final String temporaryDirectory) {
		temporaryDirectory = temporaryDirectory;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		userEmail = userEmail;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		language = language;
	}

	public boolean isDownloadNormalWay() {
		return downloadNormalWay;
	}

	public void setDownloadNormalWay(boolean downloadNormalWay) {
		downloadNormalWay = downloadNormalWay;
	}
}
