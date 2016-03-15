package fi.nls.oskari.eu.inspire;

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

    @Ignore("Schema failure")
    @Test
    public void testInspireTnRoWFSRoadLink() throws MalformedURLException,
            IOException {
        final String url = "http://www.ign.es/wfs-inspire/transportes-btn100?"
                + "SERVICE=WFS&VERSION=2.0.0&REQUEST=DescribeFeatureType&"
                + "OUTPUTFORMAT=application%2Fgml%2Bxml%3B+version%3D3.2&"
                + "TYPENAME=tn-ro:RoadLink&"
                + "NAMESPACES=xmlns(tn-ro,urn%3Ax-inspire%3Aspecification%3Agmlas%3ARoadTransportNetwork%3A3.0)";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
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

    @Ignore("Requires web service")
    @Test
    public void testInspireGnNamedPlace_cuzk_cz() throws MalformedURLException,
            IOException {
        final String url = "http://services.cuzk.cz/xsd/inspire/specification/3.0rc3/GeographicalNames.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "NamedPlace";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "geographicalnames";
        final String classname = "INSPIRE_gn_NamedPlace";

        final String targetNS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    
    //
    @Ignore("Requires web service")
    public void testInspireTnRoRoadLink() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/tn-ro/3.0/RoadTransportNetwork.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "RoadLink";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "roadtransportnetwork";
        final String classname = "INSPIRE_TN_RoadLink";

        final String targetNS = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireGnNamedPlace() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/gn/3.0/GeographicalNames.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "NamedPlace";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "geographicalnames";
        final String classname = "INSPIRE_gn_NamedPlace";

        final String targetNS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireAdAddress() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/ad/3.0/Addresses.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "Address";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "addresses";
        final String classname = "INSPIRE_ad_Address";

        final String targetNS = "urn:x-inspire:specification:gmlas:Addresses:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    
    @Ignore("Requires web service")
    @Test
    public void testInspireHyPStandingWater() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "StandingWater";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "hydrophysicalwaters";
        final String classname = "INSPIRE_hyp_StandingWater";

        final String targetNS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires web service")
    @Test
    public void testInspireHyPWatercourse() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "Watercourse";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "hydrophysicalwaters";
        final String classname = "INSPIRE_hyp_Watercourse";

        final String targetNS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireHyPLandWaterBoundary() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/hy-p/3.0/HydroPhysicalWaters.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "LandWaterBoundary";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "hydrophysicalwaters";
        final String classname = "INSPIRE_hyp_LandWaterBoundary";

        final String targetNS = "urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireCPCadastralParcel() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/cp/3.0/CadastralParcels.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "CadastralParcel";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "cadastralparcels";
        final String classname = "INSPIRE_cp_CadastralParcel";

        final String targetNS = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireCPCadastralBoundary() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/cp/3.0/CadastralParcels.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "CadastralBoundary";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "cadastralparcels";
        final String classname = "INSPIRE_cp_CadastralBoundary";

        final String targetNS = "urn:x-inspire:specification:gmlas:CadastralParcels:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireAUAdministrativeBoundary() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/au/3.0/AdministrativeUnits.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "AdministrativeBoundary";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "administrativeunits";
        final String classname = "INSPIRE_au_AdministrativeBoundary";

        final String targetNS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires web service")
    @Test
    public void testInspireAUAdministrativeUnit() throws MalformedURLException,
            IOException {
        final String url = "http://inspire.ec.europa.eu/schemas/au/3.0/AdministrativeUnits.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        final String feature = "AdministrativeUnit";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "administrativeunits";
        final String classname = "INSPIRE_au_AdministrativeUnit";

        final String targetNS = "urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
}


