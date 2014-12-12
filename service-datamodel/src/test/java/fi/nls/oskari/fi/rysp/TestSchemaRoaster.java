package fi.nls.oskari.fi.rysp;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fi.nls.oskari.fe.datamodel.SchemaRoaster;
import fi.nls.oskari.fe.datamodel.TestHelper;

public class TestSchemaRoaster extends TestHelper {
    static final Logger logger = Logger.getLogger(TestSchemaRoaster.class);

    @BeforeClass
    public static void setUp() throws IOException {
        setupProxy();

    }

    @Ignore("Requires Web Service")
    @Test
    public void testRYSPKantakarttaLiikennevayla()
            throws MalformedURLException, IOException {

        final String url = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();

        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "Liikennevayla";
        final String packageName = "fi.nls.oskari.fi.rysp.";
        final String subPackage = "kantakartta";
        final String classname = "RYSP_kanta_Liikennevayla";

        final String targetNS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Incomplete")
    @Test
    public void testRYSPRakennusvalvontaRakennusvalvonta()
            throws MalformedURLException, IOException {

        final String url = "http://www.paikkatietopalvelu.fi/gml/rakennusvalvonta/2.1.6/rakennusvalvonta.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "Rakennusvalvonta";
        final String packageName = "fi.nls.oskari.fi.rysp.";
        final String subPackage = "rakennusvalvonta";
        final String classname = "RYSP_rakennusvalvonta_Rakennusvalvonta";

        final String targetNS = "http://www.paikkatietopalvelu.fi/gml/rakennusvalvonta";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testRYSPKantakarttaRakennus() throws MalformedURLException,
            IOException {

        final String url = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";

        final SchemaRoaster roaster = new SchemaRoaster();
        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "Rakennus";
        final String packageName = "fi.nls.oskari.fi.rysp.";
        final String subPackage = "kantakartta";
        final String classname = "RYSP_kanta_Rakennus";

        final String targetNS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

}
