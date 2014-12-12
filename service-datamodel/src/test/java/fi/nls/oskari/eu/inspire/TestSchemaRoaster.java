package fi.nls.oskari.eu.inspire;

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

    @Ignore("Schema failure")
    @Test
    public void testInspireTnRoRoadLink() throws MalformedURLException,
            IOException {
        final String url = "http://www.ign.es/wfs-inspire/transportes-btn100?"
                + "SERVICE=WFS&VERSION=2.0.0&REQUEST=DescribeFeatureType&"
                + "OUTPUTFORMAT=application%2Fgml%2Bxml%3B+version%3D3.2&"
                + "TYPENAME=tn-ro:RoadLink&"
                + "NAMESPACES=xmlns(tn-ro,urn%3Ax-inspire%3Aspecification%3Agmlas%3ARoadTransportNetwork%3A3.0)";
        final SchemaRoaster roaster = new SchemaRoaster();
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "RoadLink";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "roadtransportnetwork";
        final String classname = "INSPIRE_tnro_RoadLink";

        final String targetNS = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
}
