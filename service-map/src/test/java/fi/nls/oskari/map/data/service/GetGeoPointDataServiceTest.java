package fi.nls.oskari.map.data.service;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 24.11.2016.
 */
public class GetGeoPointDataServiceTest {

    @Test
    public void testTransformResponse()
            throws IOException {
        final String xslt = IOHelper.readString(this.getClass().getResourceAsStream("iceland.xsl"));
        final String xml = IOHelper.readString(this.getClass().getResourceAsStream("iceland.xml"));
        GetGeoPointDataService service = new GetGeoPointDataService();
        assertNotNull(xslt);
        assertNotNull(xml);
        String response = service.transformResponse(xslt, xml);
        JSONObject json = JSONHelper.createJSONObject(response);
        assertNotNull(json);
        JSONObject parsed = json.optJSONObject("parsed");
        assertEquals("Should have html as Mynd", parsed.optString("Mynd"), "<a target='_blank' href='http://this-is-not-relevant'>link</a>");
        assertNotNull(parsed.optString("Staður"));

    }
}