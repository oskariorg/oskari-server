package fi.nls.oskari.control;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by SMAKINEN on 13.8.2015.
 */
public class ActionControlTest {

    @After
    public void teardown() throws Exception {
        PropertyUtil.clearProperties();
        ActionControl.teardown();
    }

    @Test
    public void testBlackList()
            throws Exception {
        PropertyUtil.addProperty(ActionControl.PROPERTY_BLACKLIST, "blacklisted1, blacklisted32");
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("blacklisted1", new DummyActionHandler());
        ActionControl.addAction("blacklisted32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler());
        assertTrue("Should have dummy actions", ActionControl.hasAction("dummy") && ActionControl.hasAction("dummy2"));
        assertFalse("Shouldn't have blacklisted actions", ActionControl.hasAction("blacklisted1") || ActionControl.hasAction("blacklisted32"));

    }
    @Test
    public void testWhiteList()
            throws Exception {
        PropertyUtil.addProperty(ActionControl.PROPERTY_WHITELIST, "white1, white32");
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("white1", new DummyActionHandler());
        ActionControl.addAction("white32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler());
        assertFalse("Shouldn't have dummy actions", ActionControl.hasAction("dummy") || ActionControl.hasAction("dummy2"));
        assertTrue("Should have whitelisted actions", ActionControl.hasAction("white1") && ActionControl.hasAction("white32"));

    }
    @Test
    public void testWithNoRestrictions()
            throws Exception {
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("white1", new DummyActionHandler());
        ActionControl.addAction("white32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler());
        assertTrue("Should have all actions", ActionControl.hasAction("dummy")
                && ActionControl.hasAction("white1")
                && ActionControl.hasAction("white32")
                && ActionControl.hasAction("dummy2"));

    }

    @Test
    public void testWhiteListOverride()
            throws Exception {
        PropertyUtil.addProperty(ActionControl.PROPERTY_WHITELIST, "white1, white32");
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("white1", new DummyActionHandler());
        ActionControl.addAction("white32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler(), true);
        assertFalse("Shouldn't have dummy action", ActionControl.hasAction("dummy"));
        assertTrue("Should have whitelisted and forced actions", ActionControl.hasAction("white1") && ActionControl.hasAction("white32") && ActionControl.hasAction("dummy2"));

    }
}