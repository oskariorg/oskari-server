package fi.nls.oskari.fe.datamodel;

import java.util.Map;

public class TestHelper {

    protected void setupProxy(final SchemaRoaster roaster) {

        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        System.setProperty("http.nonProxyHosts",
                "*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi|*.maanmittauslaitos.fi");

        final Map<String, String> defaultResolvers = roaster
                .getDefaultResolvers();
        defaultResolvers.put("http://www.locationframework.eu/ELF10/",
                "http://elfserver.kartverket.no/schemas/elf1.0/");
        defaultResolvers.put("http://www.locationframework.eu/ELF/",
                "http://elfserver.kartverket.no/schemas/elf1.0/");

    }
}
