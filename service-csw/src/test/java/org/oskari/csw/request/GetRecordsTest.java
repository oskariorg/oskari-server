package org.oskari.csw.request;

import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.factory.CommonFactoryFinder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.*;

public class GetRecordsTest {

    private FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    @BeforeClass
    public static void setUp() {
        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);
    }

    @Test(expected = ServiceRuntimeException.class)
    public void testWithNoFilter() {
        org.oskari.csw.request.GetRecords.createRequest(null);
        fail("Should have thrown exception");
    }

    @Test
    public void testSimpleFilter() throws IOException, SAXException {
        // build filter
        Expression _property = filterFactory.property("myprop");
        Filter filter = filterFactory.equals(_property, filterFactory.literal("my value"));
        String request = org.oskari.csw.request.GetRecords.createRequest(filter);

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-simple.xml"));
        Diff xmlDiff = new Diff(request, expected);
        assertTrue("Should get expected simple request" + xmlDiff, xmlDiff.similar());
    }
}