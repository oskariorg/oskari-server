package org.oskari.service.wfs.client;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;

import java.util.Arrays;

public class OskariWFS110ClientTest {

    @Test
    public void testGetFilter() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();
        Filter f = ff.equals(ff.property("bar"), ff.literal("foo"));
        String actual = OskariWFS110Client.getFilter(f);
        String expected = "<ogc:Filter xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\">"
                + "<ogc:PropertyIsEqualTo matchCase=\"true\">"
                + "<ogc:PropertyName>bar</ogc:PropertyName>"
                + "<ogc:Literal>foo</ogc:Literal>"
                + "</ogc:PropertyIsEqualTo>"
                + "</ogc:Filter>";
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGetFilterWitbBBox() throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();
        Filter f = ff.equals(ff.property("bar"), ff.literal("foo"));

        Filter bboxFilter = ff.bbox("geometry",
                382056, 6670472,
                388896, 6674196,
                CRS.toSRS(CRS.decode("EPSG:3067")));

        String actual = OskariWFS110Client.getFilter(ff.and(Arrays.asList(f, bboxFilter)));
        String expected = "<ogc:Filter xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\">" +
                "<ogc:And><ogc:PropertyIsEqualTo matchCase=\"true\"><ogc:PropertyName>bar</ogc:PropertyName><ogc:Literal>foo</ogc:Literal></ogc:PropertyIsEqualTo>" +
                "<ogc:BBOX><ogc:PropertyName>geometry</ogc:PropertyName><gml:Envelope srsDimension=\"2\" srsName=\"urn:x-ogc:def:crs:EPSG:3067\">" +
                "<gml:lowerCorner>382056 6670472</gml:lowerCorner><gml:upperCorner>388896 6674196</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:And></ogc:Filter>";
        Assertions.assertEquals(expected, actual);
    }

}
