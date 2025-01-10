package fi.nls.oskari.control;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by SMAKINEN on 13.8.2015.
 */
public class ActionControlTest {

    @AfterEach
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
        Assertions.assertTrue(ActionControl.hasAction("dummy") && ActionControl.hasAction("dummy2"), "Should have dummy actions");
        Assertions.assertFalse(ActionControl.hasAction("blacklisted1") || ActionControl.hasAction("blacklisted32"), "Shouldn't have blacklisted actions");

    }
    @Test
    public void testWhiteList()
            throws Exception {
        PropertyUtil.addProperty(ActionControl.PROPERTY_WHITELIST, "white1, white32");
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("white1", new DummyActionHandler());
        ActionControl.addAction("white32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler());
        Assertions.assertFalse(ActionControl.hasAction("dummy") || ActionControl.hasAction("dummy2"), "Shouldn't have dummy actions");
        Assertions.assertTrue(ActionControl.hasAction("white1") && ActionControl.hasAction("white32"), "Should have whitelisted actions");

    }
    @Test
    public void testWithNoRestrictions()
            throws Exception {
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("white1", new DummyActionHandler());
        ActionControl.addAction("white32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler());
        Assertions.assertTrue(ActionControl.hasAction("dummy")
                && ActionControl.hasAction("white1")
                && ActionControl.hasAction("white32")
                && ActionControl.hasAction("dummy2"), "Should have all actions");

    }

    @Test
    public void testWhiteListOverride()
            throws Exception {
        PropertyUtil.addProperty(ActionControl.PROPERTY_WHITELIST, "white1, white32");
        ActionControl.addAction("dummy", new DummyActionHandler());
        ActionControl.addAction("white1", new DummyActionHandler());
        ActionControl.addAction("white32", new DummyActionHandler());
        ActionControl.addAction("dummy2", new DummyActionHandler(), true);
        Assertions.assertFalse(ActionControl.hasAction("dummy"), "Shouldn't have dummy action");
        Assertions.assertTrue(ActionControl.hasAction("white1") && ActionControl.hasAction("white32") && ActionControl.hasAction("dummy2"), "Should have whitelisted and forced actions");

    }
}