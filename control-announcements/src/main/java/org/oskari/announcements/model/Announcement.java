package org.oskari.announcements.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public class Announcement  {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private int id;
    private OffsetDateTime beginDate;
    private OffsetDateTime endDate;
    private JSONObject locale;
    private JSONObject options;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OffsetDateTime getBeginDate() {
        return beginDate;
    }

    @JsonGetter("beginDate")
    public String getFormattedBeginDate () {
        return beginDate.format(FORMATTER);
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

    @JsonGetter("endDate")
    public String getFormattedEndDate () {
        return endDate.format(FORMATTER);
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    @JsonSetter("endDate")
    public void setEndDate(String endDate) {
        this.endDate = OffsetDateTime.parse(endDate, FORMATTER);
    }

    public JSONObject getLocale() {
        return locale;
    }

    @JsonGetter("locale")
    public Map <String, Object> getLocaleMap() {
        return JSONHelper.getObjectAsMap(locale);
    }

    public void setLocale(JSONObject locale) {
        this.locale = locale;
    }
    @JsonSetter("locale")
    public void setLocaleMap(Map<String, Object> locale) {
        this.locale = new JSONObject(locale);
    }

    public JSONObject getOptions() {
        return options;
    }

    @JsonGetter("options")
    public Map getOptionsMap() {
        return JSONHelper.getObjectAsMap(options);
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    @JsonSetter("options")
    public void setOptionsMap(Map<String, Object> options) {
        this.options = new JSONObject(options);
    }
}
