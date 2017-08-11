package fi.nls.oskari.control.layer;

import java.util.List;

import org.oskari.service.backendstatus.BackendStatusService;
import org.oskari.service.backendstatus.BackendStatusServiceMyBatisImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetBackendStatus")
public class GetBackendStatusHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(GetBackendStatusHandler.class);

    private final BackendStatusService service;
    private final ObjectMapper om;

    public GetBackendStatusHandler() {
        this(new BackendStatusServiceMyBatisImpl(), new ObjectMapper());
    }

    public GetBackendStatusHandler(BackendStatusService service, ObjectMapper om)  {
        this.service = service != null ? service : new BackendStatusServiceMyBatisImpl();
        this.om = om != null ? om : new ObjectMapper();
    }

    private final static String PARAM_SUBSET = "Subset";
    private final static String SUBSET_ALL_KNOWN = "AllKnown";

    public void handleAction(ActionParameters params) throws ActionException {
        String subset = params.getHttpParam(PARAM_SUBSET);
        boolean alert = !SUBSET_ALL_KNOWN.equals(subset);
        List<BackendStatus> statuses = alert ? service.findAllWithAlert() : service.findAll();
        LOG.debug("BackendStatus list size: " + statuses.size());

        try {
            byte[] b = om.writeValueAsBytes(statuses);
            ResponseHelper.writeResponse(params, 200, ResponseHelper.CONTENT_TYPE_JSON_UTF8, b);
        } catch (JsonProcessingException e) {
            LOG.warn(e, "Failed to write JSON!");
            throw new ActionException("Failed to write JSON!", e);
        }
    }
}
