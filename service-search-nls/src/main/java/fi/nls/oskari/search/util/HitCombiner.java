package fi.nls.oskari.search.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class HitCombiner {
    Map<String, ScoredSearchHit> combinedHits = new TreeMap<>();

    public void addHit(JSONObject hit, int scoreBoost) throws JSONException {
        String text = hit.getString("text");
        int score = hit.getInt("_score") + scoreBoost;
        ScoredSearchHit existingHit = combinedHits.get(text);
        if (existingHit == null || existingHit.score < score) {
            combinedHits.put(text, new ScoredSearchHit(text, score));
        }
    }

    public List<String> getSortedHits() {
        List<ScoredSearchHit> suggestions = new ArrayList<>(combinedHits.values());
        Collections.sort(suggestions, Collections.reverseOrder());
        List<String> resultList = new ArrayList<>();
        for (ScoredSearchHit hit : suggestions) {
            resultList.add(hit.text);
        }
        return resultList;
    }

}
