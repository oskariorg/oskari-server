package fi.nls.oskari.domain.map.stats;

import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class StatsVisualizationTest {

    @BeforeClass
    public static void addLocales() throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(StatsVisualizationTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            String locales = PropertyUtil.getNecessary("oskari.locales");
            if (locales == null)
                fail("No darned locales");
        } catch (DuplicateException e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
    }

    @Test
    public void testIsValid() throws Exception {
        final StatsVisualization vis = new StatsVisualization();

        assertFalse("Empty visualization shouldn't be valid", vis.isValid());

        vis.setNameJSON("{}");
        assertFalse("Visualization name is NOT enough to make it valid", vis.isValid());

        vis.setColors("FFF");
        assertFalse("Visualization colors is NOT enough to make it valid", vis.isValid());

        vis.setClasses("324,235");
        assertFalse("Visualization classes is NOT enough to make it valid", vis.isValid());

        vis.setFilterproperty("property");
        assertFalse("Visualization filter property is NOT enough to make it valid", vis.isValid());

        vis.setLayername("layerName");
        assertTrue("Visualization layerName is the last needed property to make it valid", vis.isValid());

        vis.setClasses("324,235|325,74");
        assertFalse("Visualization non-matching classes/color group count should be invalid", vis.isValid());

        vis.setColors("FFF|234");
        assertTrue("Adding matching number of colors makes it valid", vis.isValid());

        vis.setClasses("");
        vis.setColors("");
        assertFalse("Visualization empty groups cause it to be invalid", vis.isValid());
    }

    @Test
    public void testGetClassGroups() throws Exception {

        final StatsVisualization vis = new StatsVisualization();

        vis.setClasses("");
        final String[] empty = vis.getClassGroups();
        assertEquals("Empty classes should return empty array", 0, empty.length);

        vis.setClasses("234,32676,457,235|3234,346,23");
        final String[] groupOf2 = vis.getClassGroups();
        assertEquals("Should return 2 groups", 2, groupOf2.length);
        assertEquals("First group match the one on given string", "234,32676,457,235", groupOf2[0]);
        assertEquals("Second group match the one on given string", "3234,346,23", groupOf2[1]);
    }

    @Test
    public void testGetGroupColors() throws Exception {
        final StatsVisualization vis = new StatsVisualization();

        vis.setColors("");
        final String[] empty = vis.getGroupColors();
        assertEquals("Empty colors should return empty array", 0, empty.length);

        vis.setColors("FFFFFF|CCCCCC|343434");
        final String[] groupOf3 = vis.getGroupColors();
        assertEquals("Should return 3 color groups", 3, groupOf3.length);
        assertEquals("First color should be FFFFFF", "FFFFFF", groupOf3[0]);
        assertEquals("Last color should be 343434", "343434", groupOf3[2]);
    }

    @Test
    public void testName() throws Exception {
        StatsVisualization vis = new StatsVisualization();
        vis.setNameJSON("{\"fi\" : \"test name fi\", \"sv\" : \"test name sv\"}");
        assertEquals("Name for finnish lang should match the one given in json string",
                vis.getName("fi"), "test name fi");
        assertEquals("Name for swedish lang should match the one given in json string",
                vis.getName("sv"), "test name sv");
        assertEquals("Name for undefined lang should be undefined",
                vis.getName("en"), "undefined");
    }

}
