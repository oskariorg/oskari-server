package fi.nls.oskari.search.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class HitCombiner {
    Map<String, ScoredSearchHit> combinedHits = new HashMap<>();

    public void addHit(JSONObject hit, boolean isExact) throws JSONException {
        String text = hit.getString("text");
        int score = hit.getInt("_score");
        ScoredSearchHit existingHit = combinedHits.get(text);
        ScoredSearchHit incoming = new ScoredSearchHit(text, score, isExact);
        if (existingHit == null || incoming.compareTo(existingHit) > 0) {
            combinedHits.put(text, incoming);
        }
    }

    public List<String> getSortedHits() {
        List<ScoredSearchHit> suggestions = new ArrayList<>(combinedHits.values());
        Collections.sort(suggestions, Collections.reverseOrder());
        List<String> resultList = new ArrayList<>(suggestions.size());
        for (ScoredSearchHit hit : suggestions) {
            resultList.add(hit.text);
        }
        return resultList;
    }

}
