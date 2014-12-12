package fi.nls.oskari.eu.elf;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import fi.nls.oskari.fe.datamodel.SchemaRoaster;
import fi.nls.oskari.fe.datamodel.TestHelper;

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

    // @Ignore("Requires Web Service")
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

}
