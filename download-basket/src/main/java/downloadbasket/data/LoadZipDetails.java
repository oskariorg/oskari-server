package downloadbasket.data;

/**
 * Created by markokuo on 6.10.2015.
 */
public class LoadZipDetails {
    private String GetFeatureInfoRequest = null;
    private String WFSUrl = null;
    private String TemporaryDirectory = null;
    private String UserEmail = null;
    private String Language = null;
    private boolean DownloadNormalWay = true;

    public LoadZipDetails(){}

    public String getGetFeatureInfoRequest() {
        return GetFeatureInfoRequest;
    }
    public void setGetFeatureInfoRequest(String getFeatureInfoRequest) {
        GetFeatureInfoRequest = getFeatureInfoRequest;
    }
    public String getWFSUrl() {
        return WFSUrl;
    }
    public void setWFSUrl(final String wFSUrl) {
        WFSUrl = wFSUrl;
    }
    public String getTemporaryDirectory() {
        return TemporaryDirectory;
    }
    public void setTemporaryDirectory(final String temporaryDirectory) {
        TemporaryDirectory = temporaryDirectory;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public boolean isDownloadNormalWay() {
        return DownloadNormalWay;
    }

    public void setDownloadNormalWay(boolean downloadNormalWay) {
        DownloadNormalWay = downloadNormalWay;
    }
}
