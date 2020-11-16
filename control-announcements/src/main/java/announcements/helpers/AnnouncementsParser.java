package java.announcements.helpers;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import java.announcements.helpers.AnnouncementParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Search helper
 */
public class AnnouncementsParser {
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_BEGIN_DATE = "begin_date";
    public static final String KEY_END_DATE = "end_date";
    public static final String KEY_ACTIVE = "active";

    /**
     * Parse search params from json
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static List<AnnouncementParams> parseAnnouncement (ActionParameters params) throws JSONException, ActionParamsException {
        
        JSONObject jsonParams =  params.getHttpParamAsJSON("params");

        List<AnnouncementParams> AnnouncementParams = new ArrayList<>();

        if (jsonParams.has(KEY_ID)) {
            AnnouncementParams id = new AnnouncementParams(KEY_ID, null, jsonParams.getInt(KEY_ID));
            AnnouncementParams.add(id);
        }

        if (jsonParams.has(KEY_TITLE)) {
            AnnouncementParams title = new AnnouncementParams(KEY_TITLE, null, jsonParams.getString(KEY_TITLE));
            AnnouncementParams.add(title);
        }

        if (jsonParams.has(KEY_CONTENT)) {
            AnnouncementParams content = new AnnouncementParams(KEY_CONTENT, null, jsonParams.getString(KEY_CONTENT));
            AnnouncementParams.add(content);
        }
        
        if (jsonParams.has(KEY_BEGIN_DATE)) {
            AnnouncementParams beginDate = new AnnouncementParams(KEY_BEGIN_DATE, null, jsonParams.getString(KEY_BEGIN_DATE));
            AnnouncementParams.add(beginDate);
        }
        
        if (jsonParams.has(KEY_END_DATE)) {
            AnnouncementParams endDate = new AnnouncementParams(KEY_END_DATE, null, jsonParams.getString(KEY_END_DATE));
            AnnouncementParams.add(endDate);
        }

        if (jsonParams.has(KEY_ACTIVE)) {
            AnnouncementParams active = new AnnouncementParams(KEY_ACTIVE, null, jsonParams.getString(KEY_ACTIVE));
            AnnouncementParams.add(active);
        }

        return AnnouncementParams;

    }
}