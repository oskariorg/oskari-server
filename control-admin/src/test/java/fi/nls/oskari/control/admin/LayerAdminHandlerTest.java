package fi.nls.oskari.control.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OskariComponentManager.class})
public class LayerAdminHandlerTest extends AbstractLayerAdminHandlerTest {

    private static final LayerAdminHandler handler = new LayerAdminHandler();

    @BeforeClass
    public static void setup() throws Exception {
        setupMocks();
        handler.init();
    }

    @AfterClass
    public static void tearDown() {
        tearDownMocks();
    }

    @Test
    public void testCleanupLayerReferences() throws Exception {
        List<OskariLayer> layers = handler.cleanupLayerReferences(METADATA_LAYER_ID);
        for (OskariLayer layer : layers) {
            assertNull(layer.getOptions().optJSONObject("timeseries"));
        }
    }
}
