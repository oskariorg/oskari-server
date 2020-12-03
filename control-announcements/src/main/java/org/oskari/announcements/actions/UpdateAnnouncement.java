package org.oskari.announcements.actions;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.control.RestActionHandler;
import org.oskari.announcements.helpers.AnnouncementsDBHelper;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("UpdateAnnouncement")
public class UpdateAnnouncement extends AnnouncementsRestActionHandler{
    private static Logger LOG = LogFactory.getLogger(SaveAnnouncement.class);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();

        try {
            JSONObject result = AnnouncementsDBHelper.updateAnnouncement(params);
            ResponseHelper.writeResponse(params, result);
        } catch (JSONException e) {
            LOG.error("Error updating announcement", e);
            throw new ActionException("Cannot update announcement");
        }
    }
}
