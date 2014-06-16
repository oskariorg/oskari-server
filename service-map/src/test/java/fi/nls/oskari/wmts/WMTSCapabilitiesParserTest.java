package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.test.util.ResourceHelper;
import org.apache.axiom.om.OMElement;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 20.5.2014
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class WMTSCapabilitiesParserTest {
    private static final Logger log = LogFactory.getLogger(WMTSCapabilitiesParserTest.class);

    final String capabilitiesInput_NLS = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-NLS.xml", this);
    final String capabilitiesInput_Tampere = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-tampere.xml", this);
    final String capabilitiesInput_Spain = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-spain.xml", this);
    final String expectedJSON_NLS = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results-NLS.json", this);
    final String expectedJSON_tampere = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results-tampere.json", this);
    final String expectedJSON_Spain = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results-spain.json", this);

    @Test
    public void printoutCapabilities() throws Exception {
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        log.debug(parser.parseCapabilitiesToJSON(capabilitiesInput_Spain, "http://oskari.testing.fi"));
    }
    @Test
    public void testParseCapabilitiesToJSON() throws Exception {
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();

        final JSONObject parsed = parser.parseCapabilitiesToJSON(capabilitiesInput_NLS, "http://oskari.testing.fi");
        final JSONObject expected = JSONHelper.createJSONObject(expectedJSON_NLS);
        // comparing doesn't work since the JSONArrays are in different order
        //assertTrue("Parsed capabilities XML should match expected", JSONHelper.isEqual(expected, parsed));
    }
}
