package fi.nls.oskari.view.modifier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ViewModifierManagerTest {

    @AfterEach
    public void teardown() throws Exception {
        ViewModifierManager.teardown();
    }
    @Test
    public void testBlackList()
            throws Exception {
        String handlerId = "testing";
        DummyBundleHandler bundleHandler = new DummyBundleHandler();
        Assertions.assertInstanceOf(ViewModifier.class, bundleHandler, "DummyBundleHandler should be ViewModifier");

        DummyParamHandler paramHandler = new DummyParamHandler();
        Assertions.assertInstanceOf(ViewModifier.class, paramHandler, "DummyParamHandler should be ViewModifier");

        // add both with same name
        ViewModifierManager.addModifier(handlerId, paramHandler);
        ViewModifierManager.addModifier(handlerId, bundleHandler);

        Map<String, ParamHandler> paramResult = ViewModifierManager.getModifiersOfType(ParamHandler.class);
        Assertions.assertEquals(1, paramResult.size(), "Expect only one result for ParamHandlers");
        Assertions.assertEquals(handlerId, paramResult.keySet().stream().findFirst().get(), "Check handler id match");
        Assertions.assertEquals(paramHandler, paramResult.get(handlerId), "Check we got the same thing back");

        Map<String, ParamHandler> bundleResult = ViewModifierManager.getModifiersOfType(ViewModifier.class);
        Assertions.assertEquals(1, bundleResult.size(), "Expect only one result for ParamHandlers");
        Assertions.assertEquals(handlerId, bundleResult.keySet().stream().findFirst().get(), "Check handler id match");
        Assertions.assertEquals(bundleHandler, bundleResult.get(handlerId), "Check we get back the last ViewModifier that was added, but still just one");

    }

}
