package org.oskari.announcements.actions;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.announcements.helpers.AnnouncementsDBHelper;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("GetAdminAnnouncements")
public class GetAdminAnnouncements extends AnnouncementsRestActionHandler{
    private static Logger LOG = LogFactory.getLogger(GetAnnouncements.class);

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireAdminUser();

        try {
            JSONObject result = AnnouncementsDBHelper.getAdminAnnouncements();
            
            ResponseHelper.writeResponse(params, 200, result);
        } catch (JSONException e) {
            LOG.error("Error for fetching admin-announcements", e);
            throw new ActionException("Cannot get admin-announcements");
        }
    }
}
