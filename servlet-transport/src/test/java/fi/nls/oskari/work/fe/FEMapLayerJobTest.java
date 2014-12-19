package fi.nls.oskari.work.fe;

import java.io.IOException;

import org.junit.BeforeClass;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class FEMapLayerJobTest {
    protected static final Logger log = LogFactory
            .getLogger(FEMapLayerJobTest.class);


    protected static void setupProxy() {

        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        System.setProperty("http.nonProxyHosts",
                "*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi");

    }

    @BeforeClass
    public static void setUp() throws IOException {
        setupProxy();

    }


}
