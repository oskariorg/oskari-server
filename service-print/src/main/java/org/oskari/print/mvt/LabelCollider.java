package org.oskari.print.mvt;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides which symbols are drawn, with the greedy placement of Christensen,
 * Marks and Shieber (1995): symbols are taken in priority order, and one is
 * placed if it doesn't overlap a symbol placed before it.
 *
 * Symbols of a later style layer are drawn on top, so they go first. Within a
 * layer, the first feature goes first.
 */
class LabelCollider {

    private final List<Rectangle2D> placed = new ArrayList<>();

    private LabelCollider() {}

    /**
     * @param candidates in style order
     * @return the candidates to draw, in the order they were given
     */
    public static List<LabelCandidate> place(List<LabelCandidate> candidates) {
        LabelCollider collider = new LabelCollider();
        Set<LabelCandidate> drawn = new HashSet<>();
        for (List<LabelCandidate> symbol : byPriority(toSymbols(candidates))) {
            List<LabelCandidate> parts = collider.fit(symbol);
            parts.forEach(collider::reserve);
            drawn.addAll(parts);
        }
        return candidates.stream().filter(drawn::contains).collect(Collectors.toList());
    }

    /**
     * @return each symbol as its parts: an icon and its label, or either alone
     */
    private static List<List<LabelCandidate>> toSymbols(List<LabelCandidate> candidates) {
        List<List<LabelCandidate>> symbols = new ArrayList<>();
        for (LabelCandidate candidate : candidates) {
            LabelCandidate paired = candidate.getPairedWith();
            if (paired == null) {
                symbols.add(List.of(candidate));
            } else if (candidate.isIcon()) {
                symbols.add(List.of(candidate, paired));
            }
            // A paired label comes along with its icon
        }
        return symbols;
    }

    private static List<List<LabelCandidate>> byPriority(List<List<LabelCandidate>> symbols) {
        Map<MVTStyleLayer, Integer> layerIndex = new HashMap<>();
        for (List<LabelCandidate> symbol : symbols) {
            layerIndex.putIfAbsent(symbol.get(0).getStyleLayer(), layerIndex.size());
        }
        List<List<LabelCandidate>> sorted = new ArrayList<>(symbols);
        // The sort is stable, features keep their order within a layer
        sorted.sort(Comparator.comparing(
                (List<LabelCandidate> symbol) -> layerIndex.get(symbol.get(0).getStyleLayer()))
                .reversed());
        return sorted;
    }

    /**
     * @return the parts of the symbol to draw: the ones that fit, or none when a
     *         part that doesn't fit is not optional
     */
    private List<LabelCandidate> fit(List<LabelCandidate> symbol) {
        List<LabelCandidate> fitting = new ArrayList<>();
        for (LabelCandidate part : symbol) {
            if (fits(part)) {
                fitting.add(part);
            } else if (!part.isOptional()) {
                return List.of();
            }
        }
        return fitting;
    }

    private boolean fits(LabelCandidate candidate) {
        return candidate.isAllowOverlap() || !overlapsPlaced(candidate.getBounds());
    }

    private void reserve(LabelCandidate candidate) {
        if (!candidate.isIgnorePlacement()) {
            placed.add(candidate.getBounds());
        }
    }

    private boolean overlapsPlaced(Rectangle2D bounds) {
        for (Rectangle2D other : placed) {
            if (bounds.intersects(other)) {
                return true;
            }
        }
        return false;
    }
}
