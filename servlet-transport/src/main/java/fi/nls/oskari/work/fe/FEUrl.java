package fi.nls.oskari.work.fe;

public class FEUrl {
    protected boolean proxy;
    protected String url;

    public FEUrl(String url, boolean proxy) {
        this.url = url;
        this.proxy = proxy;
    }
}