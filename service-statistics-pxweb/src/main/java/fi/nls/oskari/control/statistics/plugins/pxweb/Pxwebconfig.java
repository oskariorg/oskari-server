package fi.nls.oskari.control.statistics.plugins.pxweb;


public class Pxwebconfig {

    private String url;

    public String getUrl() {
        if(url == null) {
            // default
            return "http://pxweb.hel.ninja/PXWeb/pxweb/en/hri/hri";
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
