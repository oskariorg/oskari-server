package org.oskari.announcements.actions;

import fi.nls.oskari.control.RestActionHandler;
import org.oskari.announcements.helpers.AnnouncementsParser;
import org.oskari.announcements.helpers.Announcement;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.log.AuditLog;
import fi.nls.oskari.util.JSONHelper;

@OskariActionRoute("Announcements")
public class AnnouncementsHandler extends RestActionHandler {
    private AnnouncementsService service = new AnnouncementsServiceMybatisImpl();

    // Handle get announcements
    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        try {
            JSONObject result = new JSONObject();
            if (params.getHttpParam("all", false) && params.getUser().isAdmin()) {
                result = service.getAdminAnnouncements();
            } else {
                result = service.getAnnouncements();
            }
            ResponseHelper.writeResponse(params, 200, result);
        
        } catch (Exception e) {
            throw new ActionException("Cannot get announcements", e);
        }
    }
    // Handle update announcements
    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        try {
            Announcement announcement = AnnouncementsParser.parseAnnouncementParams(params);
            final JSONObject result = new JSONObject();
            int updateId = service.updateAnnouncement(announcement);
            JSONHelper.putValue(result, "id", updateId);
            
            AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", announcement.getId())
            .withParam("title", announcement.getTitle())
            .withParam("content", announcement.getContent())
            .withParam("beginDate", announcement.getBeginDate())
            .withParam("endDate", announcement.getEndDate())
            .withParam("active", announcement.getActive());

            ResponseHelper.writeResponse(params, result);
        } catch (JSONException e) {
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
            Announcement announcement = AnnouncementsParser.parseAnnouncementParams(params);
            final JSONObject result = new JSONObject();
            int saveId = service.saveAnnouncement(announcement);
            JSONHelper.putValue(result, "id", saveId);

            AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", announcement.getId())
            .withParam("title", announcement.getTitle())
            .withParam("content", announcement.getContent())
            .withParam("beginDate", announcement.getBeginDate())
            .withParam("endDate", announcement.getEndDate())
            .withParam("active", announcement.getActive());

            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            throw new ActionException("Cannot save announcement", e);
        }
    }
}