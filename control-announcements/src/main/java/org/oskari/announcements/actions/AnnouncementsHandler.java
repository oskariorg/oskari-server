package org.oskari.announcements.actions;

import fi.nls.oskari.control.RestActionHandler;
import org.json.JSONArray;
import org.oskari.announcements.helpers.AnnouncementsHelper;
import org.oskari.announcements.model.Announcement;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;
import org.oskari.log.AuditLog;
import fi.nls.oskari.util.JSONHelper;

import java.util.List;

@OskariActionRoute("Announcements")
public class AnnouncementsHandler extends RestActionHandler {
    private AnnouncementsService service = new AnnouncementsServiceMybatisImpl();

    // Handle get announcements
    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        try {
            List<Announcement> announcements;
            if (params.getUser().isAdmin()) {
                announcements = service.getAnnouncements();
            } else {
                announcements = service.getActiveAnnouncements();
            }
            ResponseHelper.writeResponse(params, AnnouncementsHelper.writeJSON(announcements));
        } catch (Exception e) {
            throw new ActionException("Cannot get announcements", e);
        }
    }
    // Handle update announcements
    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        try {
            Announcement announcement = AnnouncementsHelper.readJSON(params.getPayLoad());
            int updateId = service.updateAnnouncement(announcement);
            JSONObject result = JSONHelper.createJSONObject("id", updateId);

            AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", announcement.getId())
            .withParam("locale", announcement.getLocale())
            .withParam("beginDate", announcement.getBeginDate())
            .withParam("endDate", announcement.getEndDate())
            .withParam("options", announcement.getOptions());

            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            throw new ActionException("Cannot update announcement", e);
        }
    }
    // Handle delete announcements
    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        int id = params.getRequiredParamInt("id");
        try {
            int deleteId = service.deleteAnnouncement(id);
            final JSONObject result = new JSONObject();
            JSONHelper.putValue(result, "id", deleteId);
            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            throw new ActionException("Cannot delete announcement", e);
        }
    }
    // Handle save announcements
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();

        try {
            Announcement announcement = AnnouncementsHelper.readJSON(params.getPayLoad());
            int saveId = service.saveAnnouncement(announcement);
            JSONObject result = JSONHelper.createJSONObject("id", saveId);

            AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", announcement.getId())
            .withParam("locale", announcement.getLocale())
            .withParam("beginDate", announcement.getBeginDate())
            .withParam("endDate", announcement.getEndDate())
            .withParam("options", announcement.getOptions());

            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            throw new ActionException("Cannot save announcement", e);
        }
    }
}