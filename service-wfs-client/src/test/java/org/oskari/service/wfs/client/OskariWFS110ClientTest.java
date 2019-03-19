package org.oskari.service.wfs.client;

import static org.junit.Assert.assertEquals;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

public class OskariWFS110ClientTest {

    @Test
    public void testGetFilter() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();
        Filter f = ff.equals(ff.property("bar"), ff.literal("foo"));
        String actual = OskariWFS110Client.getFilter(f);
        String expected = "<ogc:Filter xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\">"
                + "<ogc:PropertyIsEqualTo>"
                + "<ogc:PropertyName>bar</ogc:PropertyName>"
                + "<ogc:Literal>foo</ogc:Literal>"
                + "</ogc:PropertyIsEqualTo>"
                + "</ogc:Filter>";
        assertEquals(expected, actual);
    }

}
