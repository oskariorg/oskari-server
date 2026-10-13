package org.oskari.print.mvt;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.JSONHelper;

public class LabelColliderTest {

    private static final MVTStyleLayer LOWER = layer("lower");
    private static final MVTStyleLayer UPPER = layer("upper");

    private static MVTStyleLayer layer(String id) {
        return MVTStyleLayer.parse(JSONHelper.createJSONObject(
                "{\"id\": \"" + id + "\", \"type\": \"symbol\","
                + "\"layout\": {\"text-field\": \"{name}\"}}"), 14);
    }

    private static LabelCandidate at(MVTStyleLayer layer, double x, double y) {
        return label(layer, x, y, false, false, false);
    }

    private static LabelCandidate label(MVTStyleLayer layer, double x, double y,
            boolean allowOverlap, boolean ignorePlacement, boolean optional) {
        return LabelCandidate.text(layer, Collections.emptyList(), 10,
                new Rectangle2D.Double(x, y, 10, 10), allowOverlap, ignorePlacement, optional);
    }

    private static LabelCandidate icon(MVTStyleLayer layer, double x, double y) {
        return LabelCandidate.icon(layer, "marker", x, y, 10, 10,
                new Rectangle2D.Double(x, y, 10, 10), false, false, false);
    }

    @Test
    public void theLaterStyleLayerWins() {
        LabelCandidate first = at(LOWER, 0, 0);
        LabelCandidate second = at(LOWER, 5, 5);
        // Overlaps the first only
        LabelCandidate later = at(UPPER, -6, -6);
        Assertions.assertEquals(List.of(first), LabelCollider.place(List.of(first, second)),
                "within a layer the first feature claims the space");
        Assertions.assertEquals(List.of(second, later), LabelCollider.place(List.of(first, second, later)),
                "the later layer claims the space, and the feature it pushes out blocks nothing");
    }

    @Test
    public void allowOverlapIsDrawnAndStillBlocksUnlikeIgnorePlacement() {
        LabelCandidate first = at(LOWER, 0, 0);
        LabelCandidate overlapping = label(LOWER, 5, 5, true, false, false);
        // Clear of the first, but not of the one allowed to overlap
        LabelCandidate blocked = at(LOWER, 12, 12);
        List<LabelCandidate> drawn = LabelCollider.place(List.of(first, overlapping, blocked));
        Assertions.assertEquals(List.of(first, overlapping), drawn,
                "the overlapping one is drawn and keeps the next one out");

        drawn = LabelCollider.place(List.of(label(LOWER, 0, 0, false, true, false), at(LOWER, 1, 1)));
        Assertions.assertEquals(2, drawn.size(),
                "a label that ignores placement doesn't block the next one");
    }

    @Test
    public void anIconAndItsLabelAreDroppedTogether() {
        LabelCandidate icon = icon(LOWER, 50, 50);
        // The label lands on the blocker, so the symbol doesn't fit
        LabelCandidate label = at(LOWER, 2, 2);
        LabelCandidate.pair(icon, label);
        LabelCandidate blocker = at(UPPER, 0, 0);

        List<LabelCandidate> drawn = LabelCollider.place(List.of(icon, label, blocker));
        Assertions.assertEquals(List.of(blocker), drawn,
                "a symbol whose label doesn't fit isn't drawn at all");
    }

    @Test
    public void anOptionalLabelIsDroppedOnItsOwn() {
        LabelCandidate icon = icon(LOWER, 50, 50);
        LabelCandidate label = label(LOWER, 2, 2, false, false, true);
        LabelCandidate.pair(icon, label);
        LabelCandidate blocker = at(UPPER, 0, 0);

        List<LabelCandidate> drawn = LabelCollider.place(List.of(icon, label, blocker));
        Assertions.assertEquals(List.of(icon, blocker), drawn,
                "the icon stays without its optional label, drawn in style order");
    }
}
