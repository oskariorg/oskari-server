package fi.nls.oskari.search.util;

class ScoredSearchHit implements Comparable<ScoredSearchHit> {
    public final String text;
    public final int score;
    public final Boolean isExact;

    public ScoredSearchHit(String t, int s, boolean e) {
        text = t;
        score = s;
        isExact = e;
    }

    @Override
    public int compareTo(ScoredSearchHit other) {
        int comparison = this.isExact.compareTo(other.isExact);
        if (comparison == 0) {
            return score - other.score;
        }
        return comparison;
    }
}
