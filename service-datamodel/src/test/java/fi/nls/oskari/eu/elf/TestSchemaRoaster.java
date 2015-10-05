package fi.nls.oskari.eu.elf;

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

    @Ignore("Requires Web Service")
    @Test
    public void testELFTnRoRoadLink() throws MalformedURLException, IOException {

        final String url = "http://elf-wfs.maanmittauslaitos.fi/elf-wfs/services/elf-lod1rdtn?"
                + "SERVICE=WFS&VERSION=2.0.0&"
                + "REQUEST=DescribeFeatureType&"
                + "TYPENAME=elf_lod1rtn:RoadLink&"
                + "NAMESPACES=xmlns(elf_lod1rtn,http%3A%2F%2Fwww.locationframework.eu%2Fschemas%2FRoadTransportNetwork%2FMasterLoD1%2F1.0)";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);

        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "RoadLink";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "roadtransportnetwork";
        final String classname = "ELF_TNRO_RoadLink";

        final String targetNS = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD0Building_WFS() throws MalformedURLException,
            IOException {

        final String url = "http://elf-wfs.maanmittauslaitos.fi/elf-wfs/services/elf-lod0bu?service=WFS&request=DescribeFeatureType&TYPENAMES=elf-lod0bu:Building&version=2.0.0&NAMESPACES=xmlns(elf-lod0bu,http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0)";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);

        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "Building";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "buildings";
        final String classname = "ELF_MasterLoD0_Building_nls_fi_wfs";

        final String targetNS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";

        roaster.roastSchema(packageName, subPackage, feature, classname,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD0Building() throws MalformedURLException, IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_Buildings.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);

        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "Building";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "buildings";
        final String classname = "ELF_MasterLoD0_Building";

        final String targetNS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD0Address() throws MalformedURLException, IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_Addresses.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "Address";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "addresses";
        final String classname = "ELF_MasterLoD0_Address";

        final String targetNS = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD0CadastralParcels() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_CadastralParcels.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "CadastralParcel";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "cadastralparcels";
        final String classname = "ELF_MasterLoD0_CadastralParcel";

        final String targetNS = "http://www.locationframework.eu/schemas/CadastralParcels/MasterLoD0/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1AdministrativeUnit() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_AdministrativeUnits.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "AdministrativeUnit";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "administrativeunits";
        final String classname = "ELF_MasterLoD1_AdministrativeUnit";

        final String targetNS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1AdministrativeBoundary()
            throws MalformedURLException, IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_AdministrativeUnits.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);
        logger.debug(url);

        final String feature = "AdministrativeBoundary";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "administrativeunits";
        final String classname = "ELF_MasterLoD1_AdministrativeBoundary";

        final String targetNS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1GeographicalNames() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_GeographicalNames.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "NamedPlace";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "geographicalnames";
        final String classname = "ELF_MasterLoD1_NamedPlace";

        final String targetNS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1AirTransportNetwork() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_AirTransportNetwork.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "AerodromeArea";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "airtransportnetwork";
        final String classname = "ELF_MasterLoD1_AerodromeArea";

        final String targetNS = "http://www.locationframework.eu/schemas/AirTransportNetwork/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1Elevation() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_Elevation.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "ElevationGridCoverage";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "elevation";
        final String classname = "ELF_MasterLoD1_ElevationGridCoverage";

        final String targetNS = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1WatercourseLink() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_HydroNetwork.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "WatercourseLink";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "hydronetwork";
        final String classname = "ELF_MasterLoD1_WatercourseLink";

        final String targetNS = "http://www.locationframework.eu/schemas/HydroNetwork/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Schema failure - http://schemas.opengis.net/gml/3.3/extdBasicTypes.xsd does not exist - extdBaseTypes.xsd does however ")
    @Test
    public void testElfLoD1Crossing() throws MalformedURLException, IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_HydroWaters.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Crossing";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "hydrowaters";
        final String classname = "ELF_MasterLoD1_Crossing";

        final String targetNS = "http://www.locationframework.eu/schemas/HydroWaters/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Schema failure - http://schemas.opengis.net/gml/3.3/extdBasicTypes.xsd does not exist - extdBaseTypes.xsd does however ")
    @Test
    public void testElfLoD1DamOrWeir() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_HydroWaters.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "DamOrWeir";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "hydrowaters";
        final String classname = "ELF_MasterLoD1_DamOrWeir";

        final String targetNS = "http://www.locationframework.eu/schemas/HydroWaters/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1LandCoverUnit() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_LandCover.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "LandCoverUnit";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "landcover";
        final String classname = "ELF_MasterLoD1_LandCoverUnit";

        final String targetNS = "http://www.locationframework.eu/schemas/LandCover/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1ProtectedSite() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_ProtectedSites.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "ProtectedSite";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "protectedsites";
        final String classname = "ELF_MasterLoD1_ProtectedSite";

        final String targetNS = "http://www.locationframework.eu/schemas/ProtectedSites/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1RailwayLink() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_RailwayTransportNetwork.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "RailwayLink";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "railwaytransportnetwork";
        final String classname = "ELF_MasterLoD1_RailwayLink";

        final String targetNS = "http://www.locationframework.eu/schemas/RailwayTransportNetwork/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }
    
    @Ignore("Requires Web Service")
    @Test
    public void testElfLoD1WaterwayLink() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_WaterTransportNetwork.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "WaterwayLink";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "watertransportnetwork";
        final String classname = "ELF_MasterLoD1_WaterwayLink";

        final String targetNS = "http://www.locationframework.eu/schemas/WaterTransportNetwork/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Schema failure - http://schemas.opengis.net/gml/3.3/extdBasicTypes.xsd does not exist - extdBaseTypes.xsd does however ")
    @Test
    public void testElfLoD1Coastline() throws MalformedURLException,
            IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_SeaRegions.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Coastline";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "searegions";
        final String classname = "ELF_MasterLoD1_Coastline";

        final String targetNS = "http://www.locationframework.eu/schemas/SeaRegions/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

    @Ignore("Schema failure - http://schemas.opengis.net/gml/3.3/extdBasicTypes.xsd does not exist - extdBaseTypes.xsd does however ")
    @Test
    public void testElfLoD1Sea() throws MalformedURLException, IOException {

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_SeaRegions.xsd";
        final SchemaRoaster roaster = new SchemaRoaster();
        setupProxy(roaster);
        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Sea";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "searegions";
        final String classname = "ELF_MasterLoD1_Sea";

        final String targetNS = "http://www.locationframework.eu/schemas/SeaRegions/MasterLoD1/1.0";

        roaster.roastSchema(packageName, subPackage, classname, feature,
                targetNS, url);

    }

}
