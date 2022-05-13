package org.oskari.announcements.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.oskari.announcements.model.Announcement;


public class AnnouncementsHelper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    public static Announcement readJSON(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Announcement.class);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't parse announcement from: " + json, ex);
        }
    }

    public static String writeJSON(Announcement announcement) {
        try {
            return OBJECT_MAPPER.writeValueAsString(announcement);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't write announcement to JSON", ex);
        }
    }
}
