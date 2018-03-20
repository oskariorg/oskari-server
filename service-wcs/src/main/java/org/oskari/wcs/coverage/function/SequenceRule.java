package org.oskari.wcs.coverage.function;

public class SequenceRule {

    public enum Rule {
        Linear, Boustrophedonic, Cantor_diagonal, Spiral, Morton, Hilbert
    }

    private final Rule rule;
    private final int[] axisOrder;

    public static int[] getDefaultAxisOrder() {
        return new int[] { 1, 2 };
    }

    public SequenceRule(Rule rule, int[] axisOrder) {
        this.rule = rule;
        this.axisOrder = axisOrder;
    }

    public Rule getRule() {
        return rule;
    }

    public int[] getAxisOrder() {
        return axisOrder;
    }

}
