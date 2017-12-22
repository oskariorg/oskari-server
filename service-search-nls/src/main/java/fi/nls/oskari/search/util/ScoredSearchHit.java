package fi.nls.oskari.search.util;

class ScoredSearchHit implements Comparable<ScoredSearchHit> {
    public final String text;
    public final int score;
    public final boolean isExact;

    public ScoredSearchHit(String t, int s, boolean e) {
        text = t;
        score = s;
        isExact = e;
    }

    @Override
    public int compareTo(ScoredSearchHit other) {
        int comparison = Boolean.compare(isExact, other.isExact);
        if (comparison == 0) {
            return Integer.compare(score, other.score);
        }
        return comparison;
    }
}
