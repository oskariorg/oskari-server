package fi.nls.oskari.control.style;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
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
        params.requireLoggedInUser();
        List<VectorStyle> styles = getService().getStylesByUser(params.getUser().getId());
        params.getResponse().setCharacterEncoding("UTF-8");
        params.getResponse().setContentType("application/json;charset=UTF-8");
        ResponseHelper.writeResponse(params, VectorStyleHelper.writeJSON(styles));
    }

    public void handlePost(final ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        try {
            final VectorStyle style = VectorStyleHelper.readJSON(params.getPayLoad());
            VectorStyleService service = getService();
            style.setCreator(params.getUser().getId());
            long id = service.saveStyle(style);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withParam("name", style.getName())
                    .withParam("layerId", style.getLayerId())
                    .added(AuditLog.ResourceType.VECTOR_STYLE);

            final VectorStyle inserted = service.getStyleById(id);
            ResponseHelper.writeResponse(params, VectorStyleHelper.writeJSON(inserted));
        } catch (Exception e) {
            throw new ActionException("Error when trying add vector style", e);
        }

    }
    public void handlePut(final ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        try {
            VectorStyleService service = getService();
            final VectorStyle style = VectorStyleHelper.readJSON(params.getPayLoad());
            if (!service.hasPermissionToAlter(style.getId(), params.getUser())) {
                throw new ActionDeniedException("Not allowed to update vector style with id: " + style.getId());
            }
            style.setUpdated(OffsetDateTime.now());
            // creator isn't updated so no need to set it here
            long id = service.updateStyle(style);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withParam("name", style.getName())
                    .withParam("layerId", style.getLayerId())
                    .updated(AuditLog.ResourceType.VECTOR_STYLE);
            final VectorStyle updated = service.getStyleById(id);
            ResponseHelper.writeResponse(params, VectorStyleHelper.writeJSON(updated));
        } catch (Exception e) {
            throw new ActionException("Error when trying update vector style", e);
        }
    }
    public void handleDelete(final ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        try {
            VectorStyleService service = getService();
            final long id = params.getRequiredParamLong("id");
            if (!service.hasPermissionToAlter(id, params.getUser())) {
                throw new ActionDeniedException("Not allowed to delete vector style with id: " + id);
            }
            service.deleteStyle(id);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .deleted(AuditLog.ResourceType.VECTOR_STYLE);

        } catch (Exception e) {
            throw new ActionException("Error when trying delete vector style", e);
        }
        ResponseHelper.writeResponse(params, true);
    }
}
