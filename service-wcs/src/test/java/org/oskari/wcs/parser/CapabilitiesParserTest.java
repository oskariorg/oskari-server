package org.oskari.wcs.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.capabilities.BoundingBox;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.capabilities.Contents;
import org.oskari.wcs.capabilities.CoverageSummary;
import org.oskari.wcs.capabilities.Operation;
import org.oskari.wcs.capabilities.OperationsMetadata;
import org.oskari.wcs.capabilities.ServiceIdentification;
import org.oskari.wcs.capabilities.ServiceMetadata;
import org.oskari.wcs.extension.CRS;
import org.oskari.wcs.extension.Interpolation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CapabilitiesParserTest {

    @Test
    public void checkCapabilities() throws IOException, ParserConfigurationException, SAXException {
        Document doc;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("capabilities.xml")) {
            doc = XML.readDocument(in);
        }

        Capabilities capabilities = CapabilitiesParser.parse(doc);
        assertEquals("10163", capabilities.getUpdateSequence());
        checkServiceIdentification(capabilities.getServiceIdentification());
        checkOperationsMetadata(capabilities.getOperationsMetadata());
        checkServiceMetadata(capabilities.getServiceMetadata());
        checkContents(capabilities.getContents());
    }

    private void checkServiceIdentification(ServiceIdentification sid) {
        assertEquals("Web Coverage Service", sid.getTitle());
        assertEquals("urn:ogc:service:wcs", sid.getServiceType());
        String[] expectedVersions = { "2.0.1", "1.1.1", "1.1.0" };
        assertArrayEquals(expectedVersions, sid.getServiceTypeVersion().toArray());
        String[] expectedProfiles = {
                "http://www.opengis.net/spec/WCS/2.0/conf/core",
                "http://www.opengis.net/spec/WCS_protocol-binding_get-kvp/1.0.1",
                "http://www.opengis.net/spec/WCS_protocol-binding_post-xml/1.0",
                "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs-gridded-coverage",
                "http://www.opengis.net/spec/WCS_geotiff-coverages/1.0/conf/geotiff-coverage",
                "http://www.opengis.net/spec/GMLCOV/1.0/conf/gml-coverage",
                "http://www.opengis.net/spec/GMLCOV/1.0/conf/special-format",
                "http://www.opengis.net/spec/GMLCOV/1.0/conf/multipart",
                "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling",
                "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs",
                "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation",
                "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation-per-axis",
                "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/nearest-neighbor",
                "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/linear",
                "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/cubic",
                "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting" };
        assertArrayEquals(expectedProfiles, sid.getProfile().toArray());
    }

    private void checkOperationsMetadata(OperationsMetadata opMetadata) {
        List<Operation> operations = opMetadata.getOperation();
        assertNotNull(operations);
        assertEquals(3, operations.size());
        assertEquals("GetCapabilities", operations.get(0).getName());
        assertEquals("DescribeCoverage", operations.get(1).getName());
        assertEquals("GetCoverage", operations.get(2).getName());
        for (int i = 0; i < 3; i++) {
            assertEquals("http://avoindata.maanmittauslaitos.fi:80/geoserver/wcs?",
                    operations.get(i).getGet().get());
            assertEquals("http://avoindata.maanmittauslaitos.fi:80/geoserver/wcs?",
                    operations.get(i).getPost().get());
        }
    }

    private void checkServiceMetadata(ServiceMetadata sMetadata) {
        String[] expectedFormatSupported = { "application/gml+xml", "application/gtopo30",
                "application/x-gzip", "image/jpeg", "image/png", "image/tiff", "text/plain" };
        assertArrayEquals(expectedFormatSupported, sMetadata.getSupportedFormats().toArray());

        String[] expectedCrsSupported = { "http://www.opengis.net/def/crs/EPSG/0/3067",
                "http://www.opengis.net/def/crs/EPSG/0/4258",
                "http://www.opengis.net/def/crs/EPSG/0/4326" };
        assertArrayEquals(
                expectedCrsSupported,
                sMetadata.getExtensions("crsSupported").toArray());
        assertArrayEquals(expectedCrsSupported,
                sMetadata.getExtensions(CRS.EXT_ELEMENT).toArray());

        String[] expectedInterpolationSupported = {
                "http://www.opengis.net/def/interpolation/OGC/1/nearest-neighbor",
                "http://www.opengis.net/def/interpolation/OGC/1/linear",
                "http://www.opengis.net/def/interpolation/OGC/1/cubic" };
        assertArrayEquals(
                expectedInterpolationSupported,
                sMetadata.getExtensions("interpolationSupported").toArray());
        assertArrayEquals(expectedInterpolationSupported,
                sMetadata.getExtensions(Interpolation.EXTENSION_ELEMENT_LOCALNAME).toArray());
    }

    private void checkContents(Contents contents) {
        List<CoverageSummary> coverageSummaries = contents.getCoverageSummary();
        assertEquals(3, coverageSummaries.size());

        CoverageSummary cov = coverageSummaries.get(0);
        assertEquals("asdi__Hillshade", cov.getCoverageId());
        assertEquals("RectifiedGridCoverage", cov.getCoverageSubType());
        BoundingBox wgs84 = cov.getWgs84BoundingBox();
        assertEquals(425360.0, wgs84.getLowerCornerLon(), 0);
        assertEquals(-2746000.0, wgs84.getLowerCornerLat(), 0);
        assertEquals(968000.0, wgs84.getUpperCornerLon(), 0);
        assertEquals(-2102960.0, wgs84.getUpperCornerLat(), 0);
        List<BoundingBox> otherBbox = cov.getBoundingBoxes();
        assertEquals(1, otherBbox.size());
        BoundingBox other = otherBbox.get(0);
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/EPSG:404000", other.getCrs());
        assertEquals(425360.0, other.getLowerCornerLon(), 0);
        assertEquals(-2746000.0, other.getLowerCornerLat(), 0);
        assertEquals(968000.0, other.getUpperCornerLon(), 0);
        assertEquals(-2102960.0, other.getUpperCornerLat(), 0);

        cov = coverageSummaries.get(1);
        assertEquals("korkeusmalli_10m__korkeusmalli_10m", cov.getCoverageId());
        assertEquals("RectifiedGridCoverage", cov.getCoverageSubType());
        wgs84 = cov.getWgs84BoundingBox();
        assertEquals(14.765799487957857, wgs84.getLowerCornerLon(), 1e-10);
        assertEquals(55.23283167914538, wgs84.getLowerCornerLat(), 1e-10);
        assertEquals(33.67434762088146, wgs84.getUpperCornerLon(), 1e-10);
        assertEquals(74.14137981206898, wgs84.getUpperCornerLat(), 1e-10);
        otherBbox = cov.getBoundingBoxes();
        assertEquals(1, otherBbox.size());
        other = otherBbox.get(0);
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/EPSG:3067", other.getCrs());
        assertEquals(44000.0, other.getLowerCornerLon(), 0);
        assertEquals(6594000.0, other.getLowerCornerLat(), 0);
        assertEquals(740000.0, other.getUpperCornerLon(), 0);
        assertEquals(7782000.0, other.getUpperCornerLat(), 0);

        cov = coverageSummaries.get(2);
        assertEquals("korkeusmalli_10m__korkeusmalli_10m_hila_256m", cov.getCoverageId());
        assertEquals("RectifiedGridCoverage", cov.getCoverageSubType());
        wgs84 = cov.getWgs84BoundingBox();
        assertEquals(11.902467665213978, wgs84.getLowerCornerLon(), 1e-10);
        assertEquals(58.880397252757405, wgs84.getLowerCornerLat(), 1e-10);
        assertEquals(37.1881682939402, wgs84.getUpperCornerLon(), 1e-10);
        assertEquals(70.45932721811317, wgs84.getUpperCornerLat(), 1e-10);
        otherBbox = cov.getBoundingBoxes();
        assertEquals(1, otherBbox.size());
        other = otherBbox.get(0);
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/EPSG:3067", other.getCrs());
        assertEquals(-76000.0, other.getLowerCornerLon(), 0);
        assertEquals(6570000.0, other.getLowerCornerLat(), 0);
        assertEquals(884000.0, other.getUpperCornerLon(), 0);
        assertEquals(7818000.0, other.getUpperCornerLat(), 0);
    }

}
