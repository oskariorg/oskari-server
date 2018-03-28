package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.pxweb.PxwebConfig;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.PxTableItem;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 28.3.2018.
 */
public class PxwebIndicatorsParserTest {

    private PxwebIndicatorsParser getParser() {
        JSONObject json = ResourceHelper.readJSONResource("config.json", this);
        PxwebConfig config = new PxwebConfig(json, 1);
        return new PxwebIndicatorsParser(config);
    }

    @Test
    public void testParse() throws Exception {
        System.setProperty("https.proxyHost", "wwwp.nls.fi");
        System.setProperty("https.proxyPort", "800");
        DatasourceLayer layer = new DatasourceLayer();
        PxwebIndicatorsParser parser = getParser();
        List<StatisticalIndicator> indicators = parser.parse(Collections.singletonList(layer));
        System.out.println(indicators.size());
        PxTableItem table = parser.getPxTable(null);
        List<StatisticalIndicator> indicators2 = parser.readPxTableAsIndicators(table);
        System.out.println(indicators2.size());
    }
}