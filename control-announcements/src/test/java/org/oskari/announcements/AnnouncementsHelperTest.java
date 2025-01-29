package org.oskari.announcements;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.announcements.helpers.AnnouncementsHelper;
import org.oskari.announcements.model.Announcement;

import java.util.Collections;

public class AnnouncementsHelperTest {
    private static final String JSON = "{"
            + " 'beginDate': '2022-05-16T22:00:00Z',"
            + " 'endDate': '2022-05-16T23:00:00Z',"
            + " 'options': {"
            + "    'showAsPopup': true"
            + " },"
            + " 'id': 15,"
            + " 'locale': {"
            + "     'en': {"
            + "         'title': 'Title',"
            + "         'link': 'http://oskari.org'"
            +       "}"
            + " }"
            + "}";
    @Test
    public void testParsing() throws JSONException {
        JSONObject json = new JSONObject(JSON.replace('\'', '"'));
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json);

        Announcement announcement = AnnouncementsHelper.readJSON(JSON.replace('\'', '"'));
        String arrayStr = AnnouncementsHelper.writeJSON(Collections.singletonList(announcement));

        Assertions.assertTrue(JSONHelper.isEqual(jsonArray, new JSONArray(arrayStr)));
    }
}
