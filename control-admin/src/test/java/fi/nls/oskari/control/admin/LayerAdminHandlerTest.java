package fi.nls.oskari.control.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LayerAdminHandlerTest extends AbstractLayerAdminHandlerTest {

    private static final LayerAdminHandler handler = new LayerAdminHandler();

    @BeforeEach
    public void setup() throws Exception {
        setupMocks();
        handler.init();
    }

    @AfterEach
    public void tearDown() {
        tearDownMocks();
    }

    @Test
    // For fi.nls.oskari.service.OskariComponentManager, static mocking is already registered in the current thread
    public void testCleanupLayerReferences() throws Exception {
        List<OskariLayer> layers = handler.cleanupLayerReferences(METADATA_LAYER_ID);
        for (OskariLayer layer : layers) {
            assertNull(layer.getOptions().optJSONObject("timeseries"));
        }
    }
}
