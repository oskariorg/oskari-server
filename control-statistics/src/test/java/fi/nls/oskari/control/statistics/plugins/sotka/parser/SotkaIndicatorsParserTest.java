package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
public class SotkaIndicatorsParserTest {
    private static String testResponse = ResourceHelper.readStringResource("SotkaIndicators.json",
            SotkaIndicatorsParserTest.class);
    
    @Test
    public void testParseIndicators() throws Exception {
        SotkaIndicatorsParser parser = new SotkaIndicatorsParser();
        List<SotkaIndicator> parsedObject = parser.parse(testResponse);
        assertTrue("The parsed object did not match the expected first objects.",
                parsedObject.toString().startsWith(
                "[{id: 4, localizedName: {fi=Mielenterveyden häiriöihin sairaalahoitoa saaneet 0 - 17-vuotiaat " +
                "/ 1 000 vastaavanikäistä, sv=0 - 17-åringar som vårdats på sjukhus för psykiska störningar / 1 000 i " +
                "samma åldrar, en=Hospital care for mental disorders, recipients aged 0-17 per 1000 persons of the same " +
                "age}, localizedSource: {fi=Terveyden ja hyvinvoinnin laitos (THL), sv=" +
                "Institutet för hälsa och välfärd (THL), en=Institute for Health and Welfare (THL)}, " +
                "layers: [{id: Kunta, valueType: FLOAT}, {id: Maakunta, valueType: FLOAT}, " +
                "{id: Erva, valueType: FLOAT}, {id: Aluehallintovirasto, valueType: FLOAT}, " +
                "{id: Sairaanhoitopiiri, valueType: FLOAT}, {id: Maa, valueType: FLOAT}, " +
                "{id: Suuralue, valueType: FLOAT}, {id: Seutukunta, valueType: FLOAT}, " +
                "{id: Nuts1, valueType: FLOAT}], selectors: {[{ id: sex, value: null, allowedValues: " +
                "[male, female, total]}]}},"));
        assertEquals(2434, parsedObject.size());
        assertEquals("245", parsedObject.get(40).getId());
        assertEquals(6, parsedObject.get(40).getLayers().size());
        assertEquals(IndicatorValueType.FLOAT, parsedObject.get(40).getLayers().get(5).getIndicatorValueType());
        assertEquals("Maa", parsedObject.get(40).getLayers().get(5).getOskariMapLayerId());
        assertEquals("{fi=Syöpäindeksi, ikävakioitu, sv=Cancerindex, åldersstandardiserat, en=Cancer index, age-standardised}",
                parsedObject.get(40).getLocalizedName().toString());
        assertEquals("{fi=Terveyden ja hyvinvoinnin laitos (THL), sv=Institutet för hälsa och välfärd (THL), " +
                "en=Institute for Health and Welfare (THL)}",
                parsedObject.get(40).getLocalizedSource().toString());
        // Note that the selectors are empty here, because this indicator has no allowed values for "sex".
        assertEquals("{[]}", parsedObject.get(40).getSelectors().toString());
    }
}
