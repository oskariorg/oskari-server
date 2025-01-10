package fi.nls.oskari.control.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fi.nls.oskari.domain.map.OskariLayer;

public class LayerAdminHelperTest {

    @Test
    public void testNullOptions() {
        int layerId = 1;

        OskariLayer mock = Mockito.mock(OskariLayer.class);

        Assertions.assertFalse(LayerAdminHelper.isReferencedByTimeseriesMetadata(layerId, mock));
    }

    @Test
    public void testEmptyOptions() {
        int layerId = 1;

        JSONObject options = new JSONObject();

        OskariLayer mock = Mockito.mock(OskariLayer.class);
        Mockito.when(mock.getOptions()).thenReturn(options);

        Assertions.assertFalse(LayerAdminHelper.isReferencedByTimeseriesMetadata(layerId, mock));
    }

    @Test
    public void testLayerIsEmptyString() throws JSONException {
        int layerId = 1;

        String json = ""
                + "{"
                + "  'timeseries': {"
                + "    'metadata': {"
                + "      'layer': ''"
                + "    },"
                + "    'ui': 'range'"
                + "  }"
                + "}";
        json = json.replace('\'', '"');
        JSONObject options = new JSONObject(json);

        OskariLayer mock = Mockito.mock(OskariLayer.class);
        Mockito.when(mock.getOptions()).thenReturn(options);

        Assertions.assertFalse(LayerAdminHelper.isReferencedByTimeseriesMetadata(layerId, mock));
    }

    @Test
    public void testValidOptionsButDifferentLayerId() throws JSONException {
        int layerId = 1;

        String json = ""
                + "{"
                + "  'timeseries': {"
                + "    'metadata': {"
                + "      'layer': 5"
                + "    },"
                + "    'ui': 'range'"
                + "  }"
                + "}";
        json = json.replace('\'', '"');
        JSONObject options = new JSONObject(json);

        OskariLayer mock = Mockito.mock(OskariLayer.class);
        Mockito.when(mock.getOptions()).thenReturn(options);

        Assertions.assertFalse(LayerAdminHelper.isReferencedByTimeseriesMetadata(layerId, mock));
    }

    @Test
    public void testHappyCase() throws JSONException {
        int layerId = 5;

        String json = ""
                + "{"
                + "  'timeseries': {"
                + "    'metadata': {"
                + "      'layer': 5"
                + "    },"
                + "    'ui': 'range'"
                + "  }"
                + "}";
        json = json.replace('\'', '"');
        JSONObject options = new JSONObject(json);

        OskariLayer mock = Mockito.mock(OskariLayer.class);
        Mockito.when(mock.getOptions()).thenReturn(options);

        Assertions.assertTrue(LayerAdminHelper.isReferencedByTimeseriesMetadata(layerId, mock));
    }

}
