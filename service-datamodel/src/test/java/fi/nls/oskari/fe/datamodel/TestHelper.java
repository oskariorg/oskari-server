package fi.nls.oskari.fe.datamodel;

public class TestHelper {

    protected static void setupProxy() {

        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        System.setProperty("http.nonProxyHosts",
                "*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi|*.maanmittauslaitos.fi");

    }
}
