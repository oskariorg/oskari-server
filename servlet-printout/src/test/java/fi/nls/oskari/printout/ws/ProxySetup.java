package fi.nls.oskari.printout.ws;

public class ProxySetup {

    public ProxySetup() {
        /**
         * proxy settings
         */
        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        System.setProperty("http.nonProxyHosts",
                "*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi|karttamoottori.maanmittauslaitos.fi");
    }
}
