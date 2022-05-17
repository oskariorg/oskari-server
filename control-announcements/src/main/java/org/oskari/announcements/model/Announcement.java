package org.oskari.announcements.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class Announcement  {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private int id;
    private OffsetDateTime beginDate;
    private OffsetDateTime endDate;
    private String locale;
    private String options;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OffsetDateTime getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(OffsetDateTime beginDate) {
        this.beginDate = beginDate;
    }

    @JsonSetter("beginDate")
    public void setBeginDate(String beginDate) {
        this.beginDate = OffsetDateTime.parse(beginDate, FORMATTER);
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    @JsonSetter("endDate")
    public void setEndDate(String endDate) {
        this.endDate = OffsetDateTime.parse(endDate, FORMATTER);
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    @JsonIgnore
    public JSONObject asJSON() {
        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, "id", getId());
        JSONHelper.putValue(response, "beginDate", getBeginDate().format(FORMATTER));
        JSONHelper.putValue(response, "endDate", getEndDate().format(FORMATTER));
        JSONHelper.putValue(response, "locale", JSONHelper.createJSONObject(getLocale()));
        JSONHelper.putValue(response, "options", JSONHelper.createJSONObject(getOptions()));
        return response;
    }
}
