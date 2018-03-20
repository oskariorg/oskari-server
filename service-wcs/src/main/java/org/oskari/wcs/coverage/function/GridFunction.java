package org.oskari.wcs.coverage.function;

public class GridFunction {

    public static final SequenceRule DEFAULT_SEQUENCE_RULE = new SequenceRule(
            SequenceRule.Rule.Linear, SequenceRule.getDefaultAxisOrder());

    private final SequenceRule sequenceRule;
    private final int[] startPoint;

    public GridFunction(SequenceRule sequenceRule, int[] startPoint) {
        this.sequenceRule = sequenceRule;
        this.startPoint = startPoint;
    }

    public SequenceRule getSequenceRule() {
        return sequenceRule;
    }

    public int[] getStartPoint() {
        return startPoint;
    }

}
