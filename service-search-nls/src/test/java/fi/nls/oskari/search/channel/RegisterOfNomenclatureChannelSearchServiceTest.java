package fi.nls.oskari.search.channel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.mml.portti.service.search.IllegalSearchCriteriaException;

public class RegisterOfNomenclatureChannelSearchServiceTest {

    @Test
    public void whenTransformingTo3067_thenReturnsSameArrayAndCoordinatesAreTheSame() throws IllegalSearchCriteriaException {
        String[] original = { "358230.013", "6771862.09" };
        String[] transformed = RegisterOfNomenclatureChannelSearchService.transform(original, "EPSG:3067");
        assertTrue(original == transformed);
        assertArrayEquals(original, transformed);
    }

    @Test
    public void whenTransformingTo3857_thenReturnsSameArrayAndCoordinatesAreTransformed() throws IllegalSearchCriteriaException {
        String[] original = { "358230.013", "6771862.09" };
        String[] expected = { "2713266.6267826376", "8638678.714143617" }; 
        String[] transformed = RegisterOfNomenclatureChannelSearchService.transform(original, "EPSG:3857");
        assertTrue(original == transformed);
        assertArrayEquals(expected, transformed);
    }

}
