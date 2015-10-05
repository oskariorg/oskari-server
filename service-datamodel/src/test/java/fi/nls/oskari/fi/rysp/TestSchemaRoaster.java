package fi.nls.oskari.fi.rysp;

import fi.nls.oskari.fe.datamodel.SchemaRoaster;
import fi.nls.oskari.fe.datamodel.TestHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;

public class TestSchemaRoaster extends TestHelper {
    static final Logger logger = Logger.getLogger(TestSchemaRoaster.class);

    @Ignore("Unfinished - ATM requires some manual tuning for Jackson mappers plus receives some non-schema data from Service")
    @Test
    public void testRYSPKantakarttaLiikennevayla()
            throws MalformedURLException, IOException {

        final String url = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
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

    @Ignore("will not roast")
    @Test
    public void testRYSPRakennusvalvontaRakennusvalvonta()
            throws MalformedURLException, IOException {

        final String url = "http://www.paikkatietopalvelu.fi/gml/rakennusvalvonta/2.1.6/rakennusvalvonta.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);

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

    @Ignore("Unfinished - ATM requires some manual tuning for Jackson mappers plus receives some non-schema data from Service")
    @Test
    public void testRYSPKantakarttaRakennus() throws MalformedURLException,
            IOException {
        
       

        final String url = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
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
