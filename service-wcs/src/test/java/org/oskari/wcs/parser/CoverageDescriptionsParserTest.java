package org.oskari.wcs.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.coverage.RectifiedGridCoverage;
import org.oskari.wcs.coverage.function.GridFunction;
import org.oskari.wcs.coverage.function.SequenceRule;
import org.oskari.wcs.gml.Envelope;
import org.oskari.wcs.gml.GridEnvelope;
import org.oskari.wcs.gml.Point;
import org.oskari.wcs.gml.RectifiedGrid;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CoverageDescriptionsParserTest {

    @Test
    public void checkCoverageDescriptions() throws IOException, ParserConfigurationException,
            SAXException {
        Document doc;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(
                "coveragedescriptions.xml")) {
            doc = XML.readDocument(in);
        }

        List<CoverageDescription> coverageDescriptions = CoverageDescriptionsParser.parse(doc);
        Assertions.assertEquals(2, coverageDescriptions.size());

        CoverageDescription description = coverageDescriptions.get(0);
        Assertions.assertEquals("korkeusmalli_10m__korkeusmalli_10m", description.getCoverageId());
        Assertions.assertEquals("image/tiff", description.getNativeFormat());
        Envelope boundedBy = description.getBoundedBy();
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", boundedBy.getSrsName());
        Assertions.assertEquals(2, boundedBy.getSrsDimension());
        Assertions.assertArrayEquals(new String[] { "E", "N" }, boundedBy.getAxisLabels());
        Assertions.assertArrayEquals(new String[] { "m", "m" }, boundedBy.getUomLabels());
        Assertions.assertEquals(44000.0, boundedBy.getLowerCorner()[0], 0);
        Assertions.assertEquals(6594000.0, boundedBy.getLowerCorner()[1], 0);
        Assertions.assertEquals(740000.0, boundedBy.getUpperCorner()[0], 0);
        Assertions.assertEquals(7782000.0, boundedBy.getUpperCorner()[1], 0);
        Assertions.assertTrue(description instanceof RectifiedGridCoverage);

        RectifiedGridCoverage rectGridCov = (RectifiedGridCoverage) description;

        GridFunction gridFunction = rectGridCov.getGridFunction();
        SequenceRule sequenceRule = gridFunction.getSequenceRule();
        Assertions.assertEquals(SequenceRule.Rule.Linear, sequenceRule.getRule());
        Assertions.assertArrayEquals(new int[] { +1, +2 }, sequenceRule.getAxisOrder());
        Assertions.assertArrayEquals(new int[] { 0, 0 }, gridFunction.getStartPoint());

        RectifiedGrid domainSet = rectGridCov.getDomainSet();
        Assertions.assertEquals(2, domainSet.getDimension());
        GridEnvelope limits = domainSet.getLimits();
        Assertions.assertArrayEquals(new int[] { 0, 0 }, limits.getLow());
        Assertions.assertArrayEquals(new int[] { 69599, 118799 }, limits.getHigh());
        Assertions.assertArrayEquals(new String[] { "i", "j" }, domainSet.getAxes());
        Point origin = domainSet.getOrigin();
        Assertions.assertEquals(2, origin.getDimension());
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", origin.getSrsName());
        Assertions.assertArrayEquals(new double[] { 44005.0, 7781995.0 }, origin.getPos(), 0);
        Point[] offsetVectors = domainSet.getOffsetVectors();
        Assertions.assertEquals(2, offsetVectors.length);
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", offsetVectors[0].getSrsName());
        Assertions.assertArrayEquals(new double[] { 10.0, 0.0 }, offsetVectors[0].getPos(), 0);
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", offsetVectors[1].getSrsName());
        Assertions.assertArrayEquals(new double[] { 0.0, -10.0 }, offsetVectors[1].getPos(), 0);

        description = coverageDescriptions.get(1);
        Assertions.assertEquals("korkeusmalli_10m__korkeusmalli_10m_hila_256m", description.getCoverageId());
        Assertions.assertEquals("image/tiff", description.getNativeFormat());
        boundedBy = description.getBoundedBy();
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", boundedBy.getSrsName());
        Assertions.assertEquals(2, boundedBy.getSrsDimension());
        Assertions.assertArrayEquals(new String[] { "E", "N" }, boundedBy.getAxisLabels());
        Assertions.assertArrayEquals(new String[] { "m", "m" }, boundedBy.getUomLabels());
        Assertions.assertEquals(-76000.0, boundedBy.getLowerCorner()[0], 0);
        Assertions.assertEquals(6570000.0, boundedBy.getLowerCorner()[1], 0);
        Assertions.assertEquals(884000.0, boundedBy.getUpperCorner()[0], 0);
        Assertions.assertEquals(7818000.0, boundedBy.getUpperCorner()[1], 0);

        rectGridCov = (RectifiedGridCoverage) description;

        gridFunction = rectGridCov.getGridFunction();
        sequenceRule = gridFunction.getSequenceRule();
        Assertions.assertEquals(SequenceRule.Rule.Linear, sequenceRule.getRule());
        Assertions.assertArrayEquals(new int[] { +1, +2 }, sequenceRule.getAxisOrder());
        Assertions.assertArrayEquals(new int[] { 0, 0 }, gridFunction.getStartPoint());

        domainSet = rectGridCov.getDomainSet();
        Assertions.assertEquals(2, domainSet.getDimension());
        limits = domainSet.getLimits();
        Assertions.assertArrayEquals(new int[] { 0, 0 }, limits.getLow());
        Assertions.assertArrayEquals(new int[] { 3749, 4874 }, limits.getHigh());
        Assertions.assertArrayEquals(new String[] { "i", "j" }, domainSet.getAxes());
        origin = domainSet.getOrigin();
        Assertions.assertEquals(2, origin.getDimension());
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", origin.getSrsName());
        Assertions.assertArrayEquals(new double[] { -75872.0, 7817872.0 }, origin.getPos(), 0);
        offsetVectors = domainSet.getOffsetVectors();
        Assertions.assertEquals(2, offsetVectors.length);
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", offsetVectors[0].getSrsName());
        Assertions.assertArrayEquals(new double[] { 256.0, 0.0 }, offsetVectors[0].getPos(), 0);
        Assertions.assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", offsetVectors[1].getSrsName());
        Assertions.assertArrayEquals(new double[] { 0.0, -256.0 }, offsetVectors[1].getPos(), 0);
    }

}
