package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.test.util.ResourceHelper;
import org.apache.axiom.om.OMElement;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 20.5.2014
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
public class WMTSCapabilitiesParserTest {
    private static final Logger log = LogFactory.getLogger(WMTSCapabilitiesParserTest.class);

    final String capabilitiesInput_NLS = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input.xml", this);
    final String capabilitiesInput_Tampere = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-tampere.xml", this);
    final String expectedJSON = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results.json", this);

    @Test
    public void sami() throws Exception {
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        log.debug(parser.parseCapabilitiesToJSON(capabilitiesInput_NLS));
    }
    @Test
    public void testParseCapabilitiesToJSON() throws Exception {
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();

        final JSONObject parsed = parser.parseCapabilitiesToJSON(capabilitiesInput_NLS);
        final JSONObject expected = JSONHelper.createJSONObject(expectedJSON);

        final boolean blnID = JSONHelper.isEqual(parsed.optJSONObject("serviceIdentification"), expected.optJSONObject("serviceIdentification"));
        //assertTrue("Service identification part should match", blnID);

        final boolean blnProvider = JSONHelper.isEqual(parsed.optJSONObject("serviceProvider"), expected.optJSONObject("serviceProvider"));
        //final boolean blnContents = JSONHelper.isEqual(parsed.optJSONObject("contents"), expected.optJSONObject("contents"));

        final boolean blnOpMetadata = JSONHelper.isEqual(parsed.optJSONObject("operationsMetadata"), expected.optJSONObject("operationsMetadata"));

        //assertTrue("Service provider part should match", blnProvider);

        System.out.println(parsed.toString(3));


    }
    /*
    @Test
    public void testParseJSON() throws Exception {
        OMElement elem = XmlHelper.parseXML("<test>testing</test>");
        log.debug(WMTSCapabilitiesParser.parseJSON(elem));
    }

    @Test
    public void testParseJSONasf() throws Exception {
        OMElement elem = XmlHelper.parseXML("<test><case>testing</case></test>");
        final JSONObject expected = JSONHelper.createJSONObject("{\"test\":{\"case\":\"testing\"}}");
        log.debug(WMTSCapabilitiesParser.parseJSON(elem));
        assertTrue("Expect XML to JSON result to be same as expected", JSONHelper.isEqual(expected, WMTSCapabilitiesParser.parseJSON(elem)));
    }
*/
}
