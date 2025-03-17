package fi.nls.oskari.routing;

import fi.nls.oskari.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static fi.nls.oskari.routing.PlanConnectionRequestHelper.AVAILABLE_TRANSFER_MODES;
import static fi.nls.oskari.routing.PlanConnectionRequestHelper.AVAILABLE_TRANSIT_MODES;

public class PlanConnectionRequestHelperTest {

    private static final String CAR = "CAR";
    private static final String BICYCLE = "BICYCLE";

    private static final String WALK = "WALK";

    private static final String TRANSIT = "TRANSIT";

    private static final String RAIL = "RAIL";

    private static final String TRANSIT_AND_WALK = TRANSIT + "," + WALK;
    private static final String TRANSIT_AND_BICYCLE = TRANSIT + "," + BICYCLE;

    private static final String TRANSIT_AND_CAR = TRANSIT + "," + CAR;

    private static final String CAR_AND_BICYCLE = CAR + "," + BICYCLE;
    private static final String WALK_AND_BICYCLE = WALK + "," + BICYCLE;
    private static final String WALK_AND_CAR = WALK + "," + CAR;
    private static final String DIRECT_ONLY_TRUE = "directOnly: true";
    private static final String DIRECT_ONLY_FALSE = "directOnly: false";
    private static final String TRANSFER_BY_CAR = PlanConnectionRequestHelper.JSON_CONTENT_TRANSFER  + ": [" + CAR  + "]";
    private static final String ACCESS_BY_CAR = PlanConnectionRequestHelper.JSON_CONTENT_ACCESS  + ": [" + CAR  + "]";
    private static final String EGRESS_BY_CAR = PlanConnectionRequestHelper.JSON_CONTENT_EGRESS  + ": [" + CAR  + "]";

    private static final String TRANSFER_BY_BICYCLE = PlanConnectionRequestHelper.JSON_CONTENT_TRANSFER  + ": [" + BICYCLE  + "]";
    private static final String ACCESS_BY_BICYCLE = PlanConnectionRequestHelper.JSON_CONTENT_ACCESS  + ": [" + BICYCLE  + "]";
    private static final String EGRESS_BY_BICYCLE = PlanConnectionRequestHelper.JSON_CONTENT_EGRESS  + ": [" + BICYCLE  + "]";

    private static final String TRANSFER_BY_WALK = PlanConnectionRequestHelper.JSON_CONTENT_TRANSFER  + ": [" + WALK  + "]";
    private static final String ACCESS_BY_WALK = PlanConnectionRequestHelper.JSON_CONTENT_ACCESS  + ": [" + WALK  + "]";
    private static final String EGRESS_BY_WALK = PlanConnectionRequestHelper.JSON_CONTENT_EGRESS  + ": [" + WALK  + "]";


    @Test
    public void testCarOnlyNoPublicTransportation() throws ServiceException {

        String transitmodes = PlanConnectionRequestHelper.getTransitModes(CAR);

        Assertions.assertTrue(transitmodes.contains(TRANSFER_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(ACCESS_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(EGRESS_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_TRUE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode ->
            Assertions.assertFalse(transitmodes.contains(mode)));

    }

    @Test
    public void testBicyleOnlyNoPublicTransportation() throws ServiceException {

        String transitmodes = PlanConnectionRequestHelper.getTransitModes(BICYCLE);
        String[] transitModesSplit = transitmodes.split(",");

        Assertions.assertTrue(transitmodes.contains(TRANSFER_BY_BICYCLE));
        Assertions.assertTrue(transitmodes.contains(ACCESS_BY_BICYCLE));
        Assertions.assertTrue(transitmodes.contains(EGRESS_BY_BICYCLE));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_TRUE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode ->
            Assertions.assertFalse(transitmodes.contains(mode)));
    }

    @Test
    public void testWalkOnlyNoPublicTransportation() throws ServiceException {

        String transitmodes = PlanConnectionRequestHelper.getTransitModes(WALK);
        String[] transitModesSplit = transitmodes.split(",");

        Assertions.assertTrue(transitmodes.contains(TRANSFER_BY_WALK));
        Assertions.assertTrue(transitmodes.contains(ACCESS_BY_WALK));
        Assertions.assertTrue(transitmodes.contains(EGRESS_BY_WALK));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_TRUE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode ->
            Assertions.assertFalse(transitmodes.contains(mode)));
    }

    @Test
    public void testWalkAndPublicTransportation() throws ServiceException {

        String transitmodes = PlanConnectionRequestHelper.getTransitModes(TRANSIT_AND_WALK);
        String[] transitModesSplit = transitmodes.split(",");

        Assertions.assertTrue(transitmodes.contains(TRANSFER_BY_WALK));
        Assertions.assertTrue(transitmodes.contains(ACCESS_BY_WALK));
        Assertions.assertTrue(transitmodes.contains(EGRESS_BY_WALK));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_FALSE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode ->
            Assertions.assertTrue(transitmodes.contains(mode)));
    }

    @Test
    public void testBicycleAndPublicTransportation() throws ServiceException {

        String transitmodes = PlanConnectionRequestHelper.getTransitModes(TRANSIT_AND_BICYCLE);

        Assertions.assertTrue(transitmodes.contains(TRANSFER_BY_BICYCLE));
        Assertions.assertTrue(transitmodes.contains(ACCESS_BY_BICYCLE));
        Assertions.assertTrue(transitmodes.contains(EGRESS_BY_BICYCLE));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_FALSE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode ->
            Assertions.assertTrue(transitmodes.contains(mode)));
    }

    @Test
    public void testCarAndPublicTransportation() throws ServiceException {

        String transitmodes = PlanConnectionRequestHelper.getTransitModes(TRANSIT_AND_CAR);

        Assertions.assertTrue(transitmodes.contains(TRANSFER_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(ACCESS_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(EGRESS_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_FALSE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode -> Assertions.assertTrue(transitmodes.contains(mode)));
    }

    @Test
    public void testTransitOnlyReturnsAllAvailableTransitModes() throws ServiceException {
        String transitmodes = PlanConnectionRequestHelper.getTransitModes(TRANSIT);
        Assertions.assertFalse(transitmodes.contains(ACCESS_BY_CAR));
        Assertions.assertFalse(transitmodes.contains(EGRESS_BY_CAR));
        Assertions.assertTrue(transitmodes.contains(DIRECT_ONLY_FALSE));

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode ->
            Assertions.assertTrue(transitmodes.contains(mode)));

        // Does not work for CAR -> theres CABLE_CAR and CARPOOL.
        // Maybe figure out a smarter negative test later
        // Arrays.stream(AVAILABLE_TRANSFER_MODES).forEach(transferMode ->
        //    Assertions.assertFalse(transitmodes.contains("\"" + transferMode + "\"")));
    }

    @Test
    public void testRailOnlyReturnsRailAndNoOtherTransits() throws ServiceException {
        String transitmodes = PlanConnectionRequestHelper.getTransitModes(RAIL);

        Arrays.stream(AVAILABLE_TRANSIT_MODES).forEach(mode -> {
            if (!mode.equals(RAIL)) {
                Assertions.assertFalse(transitmodes.contains(mode));
            } else {
                Assertions.assertTrue(transitmodes.contains(mode));
            }
        });

        Arrays.stream(AVAILABLE_TRANSFER_MODES).forEach(transferMode ->
            Assertions.assertFalse(transitmodes.contains(transferMode)));

    }

    @Test
    public void testCarAndBicycleThrows() {
        Assertions.assertThrows(ServiceException.class, () -> PlanConnectionRequestHelper.getTransitModes(CAR_AND_BICYCLE));
    }

    @Test
    public void testWalkAndBicycleThrows() {
        Assertions.assertThrows(ServiceException.class, () -> PlanConnectionRequestHelper.getTransitModes(WALK_AND_BICYCLE));
    }

    @Test
    public void testWalkAndCarThrows() {
        Assertions.assertThrows(ServiceException.class, () -> PlanConnectionRequestHelper.getTransitModes(WALK_AND_CAR));
    }
}