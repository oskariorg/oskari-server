package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import fi.nls.test.util.TestHelper;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SotkaIndicatorsParserTest {
    private static String testResponse = ResourceHelper.readStringResource("SotkaIndicators.json",
            SotkaIndicatorsParserTest.class);
    
    @BeforeClass
    public static void init() throws NamingException, IllegalArgumentException,
        IllegalAccessException {
        PropertyUtil.loadProperties("/oskari-ext.properties");
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testParseIndicators() throws Exception {
        org.junit.Assume.assumeTrue(TestHelper.canDoHttp());
        SotkaIndicatorsParser parser = new SotkaIndicatorsParser();
        Map<String, Long> layerMap = new HashMap<>();
        layerMap.put("kunta", 9l);
        layerMap.put("maakunta", 10l);
        layerMap.put("maa", 11l);
        List<SotkaIndicator> parsedObject = parser.parse(testResponse, layerMap, true);
        assertTrue("The parsed object did not match the expected first objects.",
                parsedObject.toString().startsWith(
                "[{pluginId: fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin, id: 4, "
                + "localizedName: {fi=Mielenterveyden häiriöihin sairaalahoitoa saaneet 0 - 17-vuotiaat / "
                + "1 000 vastaavanikäistä, sv=0 - 17-åringar som vårdats på sjukhus för psykiska störningar / "
                + "1 000 i samma åldrar, en=Hospital care for mental disorders, recipients aged 0-17 per 1000 "
                + "persons of the same age}, localizedSource: {fi=Terveyden ja hyvinvoinnin laitos (THL), "
                + "sv=Institutet för hälsa och välfärd (THL), en=Institute for Health and Welfare (THL)}, "
                + "layers: [{id: 9, valueType: FLOAT}, {id: 10, valueType: FLOAT}, "
                + "{id: 11, valueType: FLOAT}], "
                + "selectors: {[{ id: sex, value: null, allowedValues: [male, female, total]}]}},"));
        assertEquals(2434, parsedObject.size());
        assertEquals("245", parsedObject.get(40).getId());
        assertEquals(3, parsedObject.get(40).getLayers().size());
        assertEquals(IndicatorValueType.FLOAT, parsedObject.get(40).getLayers().get(2).getIndicatorValueType());
        assertEquals(11, parsedObject.get(40).getLayers().get(2).getOskariLayerId());
        assertEquals("{fi=Syöpäindeksi, ikävakioitu, sv=Cancerindex, åldersstandardiserat, en=Cancer index, age-standardised}",
                parsedObject.get(40).getLocalizedName().toString());
        assertEquals("{fi=Terveyden ja hyvinvoinnin laitos (THL), sv=Institutet för hälsa och välfärd (THL), " +
                "en=Institute for Health and Welfare (THL)}",
                parsedObject.get(40).getLocalizedSource().toString());
        // Note that the selectors are empty here, because this indicator has no allowed values for "sex".
        assertEquals("{[{ id: year, value: null, allowedValues: [2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011]}]}", parsedObject.get(40).getSelectors().toString());
    }
}
