package fi.nls.oskari.search.util;

class ScoredSearchHit implements Comparable<ScoredSearchHit> {
    public final String text;
    public final int score;

    public ScoredSearchHit(String t, int s) {
        text = t;
        score = s;
    }

    @Override
    public int compareTo(ScoredSearchHit other) {
        return score - other.score;
    }
}
