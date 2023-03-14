package fi.nls.oskari.control.style;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.style.VectorStyleHelper;
import fi.nls.oskari.map.style.VectorStyleService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.log.AuditLog;

import java.time.OffsetDateTime;
import java.util.List;

@OskariActionRoute("VectorStyle")
public class VectorStyleHandler extends RestActionHandler {
    private final static Logger log = LogFactory.getLogger(VectorStyleHandler.class);

    private VectorStyleService getService() {
        return OskariComponentManager.getComponentOfType(VectorStyleService.class);
    }

    public void handleGet(final ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        final long userId = params.getUser().getId();
        List<VectorStyle> styles = getService().getStylesByUser(userId);
        params.getResponse().setCharacterEncoding("UTF-8");
        params.getResponse().setContentType("application/json;charset=UTF-8");
        ResponseHelper.writeResponse(params, VectorStyleHelper.writeJSON(styles));
    }

    public void handlePost(final ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        final long userId = params.getUser().getId();
        try {
            final VectorStyle style = VectorStyleHelper.readJSON(params.getPayLoad());
            style.setCreator(userId);
            long id = getService().saveStyle(style);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withParam("name", style.getName())
                    .withParam("layerId", style.getLayerId())
                    .added(AuditLog.ResourceType.VECTOR_STYLE);

        } catch (Exception e) {
            throw new ActionException("Error when trying add vector style", e);
        }
        ResponseHelper.writeResponse(params, true);
    }
    public void handlePut(final ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        final long userId = params.getUser().getId();

        try {
            final VectorStyle style = VectorStyleHelper.readJSON(params.getPayLoad());
            final VectorStyle old = getService().getStyleById(style.getId());
            if (userId != old.getCreator() || style.getLayerId() != old.getLayerId()) {
                throw new ActionDeniedException("User or layerId doesn't match");
            }
            style.setUpdated(OffsetDateTime.now());
            // creator isn't updated so no need to set it here
            long id = getService().updateStyle(style);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withParam("name", style.getName())
                    .withParam("layerId", style.getLayerId())
                    .updated(AuditLog.ResourceType.VECTOR_STYLE);

        } catch (Exception e) {
            throw new ActionException("Error when trying update vector style", e);
        }
        ResponseHelper.writeResponse(params, true);
    }
}
