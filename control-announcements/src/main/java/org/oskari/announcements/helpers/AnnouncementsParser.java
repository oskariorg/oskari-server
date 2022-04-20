package org.oskari.announcements.helpers;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.Map;
/**
 * Announcements parser
 */
public class AnnouncementsParser {
    public static final String KEY_ID = "id";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_BEGIN_DATE = "begin_date";
    public static final String KEY_END_DATE = "end_date";
    public static final String KEY_ACTIVE = "active";

    /**
     * Parse announcement params from json
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static Announcement parseAnnouncementParams (ActionParameters params) throws JSONException, ActionParamsException {
        JSONObject jsonParams =  params.getPayLoadJSON();
        Announcement announcement = new Announcement();

        if (jsonParams.has(KEY_ID)) {
            announcement.setId(jsonParams.getInt(KEY_ID));
        }

        if (jsonParams.has(KEY_LOCALE)) {
            announcement.setLocale(jsonParams.getString(KEY_LOCALE));
        }
        
        if (jsonParams.has(KEY_BEGIN_DATE)) {
            announcement.setBeginDate(jsonParams.getString(KEY_BEGIN_DATE));
        }
        
        if (jsonParams.has(KEY_END_DATE)) {
            announcement.setEndDate(jsonParams.getString(KEY_END_DATE));
        }

        if (jsonParams.has(KEY_ACTIVE)) {
            announcement.setActive(jsonParams.getBoolean(KEY_ACTIVE));
        }
        
        return announcement;
    }

    public JSONObject parseAnnouncementsMap(final List<Map<String,Object>> list) throws JSONException {
        final JSONArray results = new JSONArray();
        for(Map<String, Object> map : list) {
            results.put(mapData(map));
        }
        JSONObject data = new JSONObject();
        data.put("data", results);
        return data;
    }

    public JSONObject mapData(Map<String, Object> data) throws JSONException {
        if(data == null) {
            return null;
        }

        JSONObject obj = new JSONObject();

        obj.put(KEY_ID, data.get("id"));
        obj.put(KEY_LOCALE, JSONHelper.createJSONObject((String) data.get("locale")));
        obj.put(KEY_BEGIN_DATE, data.get("begin_date"));
        obj.put(KEY_END_DATE, data.get("end_date"));
        obj.put(KEY_ACTIVE, data.get("active"));

        return obj;
    }
}