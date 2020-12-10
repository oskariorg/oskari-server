package org.oskari.announcements.actions;

import fi.nls.oskari.control.RestActionHandler;
import org.oskari.announcements.helpers.AnnouncementsParser;
import org.oskari.announcements.actions.AnnouncementsService;
import org.oskari.announcements.actions.AnnouncementsServiceMybatisImpl;
import org.oskari.announcements.helpers.Announcement;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.GetLayerKeywords;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.admin.LayerCapabilitiesHelper;
import org.oskari.admin.MapLayerGroupsHelper;
import org.oskari.admin.MapLayerPermissionsHelper;
import org.oskari.maplayer.admin.LayerValidator;
import org.oskari.maplayer.model.MapLayer;
import org.oskari.maplayer.model.MapLayerAdminOutput;
import org.oskari.log.AuditLog;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.util.*;

@OskariActionRoute("Announcements")
public class AnnouncementsHandler extends RestActionHandler {
    private static final Logger LOG = LogFactory.getLogger(AnnouncementsHandler.class);
    private AnnouncementsService service = new AnnouncementsServiceMybatisImpl();
    private AnnouncementsParser parser = new AnnouncementsParser();

    // Handle get announcements
    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        try {
            JSONObject result = new JSONObject();
            System.out.println("GET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (params.getUser().isAdmin()) {
                
                System.out.println("ADMINNNNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                result = service.getAdminAnnouncements();
            } else {
                System.out.println("EIADMINNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                result = service.getAnnouncements();
            }
            System.out.println(result);
            ResponseHelper.writeResponse(params, 200, result);
        
        } catch (Exception e) {
            LOG.error("Error", e);
        }
    }
    // Handle update announcements
    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        try {
            Announcement announcement = parser.parseAnnouncementParams(params);
            int result = service.updateAnnouncement(announcement);
            
            AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", announcement.getId())
            .withParam("title", announcement.getTitle())
            .withParam("content", announcement.getContent())
            .withParam("beginDate", announcement.getBeginDate())
            .withParam("endDate", announcement.getEndDate())
            .withParam("active", announcement.getActive());

            ResponseHelper.writeResponse(params, result);
        } catch (JSONException e) {
            LOG.error("Error updating announcement", e);
            throw new ActionException("Cannot update announcement");
        }
    }
    // Handle delete announcements
    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        final int id = params.getRequiredParamInt("id");

        try {
            int result = service.deleteAnnouncement(id);
            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            LOG.error("Error with deleting announcement", e);
            throw new ActionException("Cannot delete announcement");
        }
    }
    // Handle save announcements
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        System.out.println("=============================PARAMS SAVE====================================");
        System.out.println(params);

        try {
            Announcement announcement = parser.parseAnnouncementParams(params);
            System.out.println("=============================ANNOUNCEMENT====================================");
            System.out.println(announcement);
            int result = service.saveAnnouncement(announcement);

            AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", announcement.getId())
            .withParam("title", announcement.getTitle())
            .withParam("content", announcement.getContent())
            .withParam("beginDate", announcement.getBeginDate())
            .withParam("endDate", announcement.getEndDate())
            .withParam("active", announcement.getActive());

            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            LOG.error("Error saving announcement", e);
            throw new ActionException("Cannot save announcement");
        }
    }
}