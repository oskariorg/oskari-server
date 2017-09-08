package fi.nls.oskari.control.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.oskari.service.backendstatus.BackendStatusService;
import org.oskari.service.backendstatus.BackendStatusServiceMyBatisImpl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    protected static final String PARAM_SUBSET = "Subset";
    protected static final String SUBSET_ALL_KNOWN = "AllKnown";

    private final BackendStatusService service;
    private final ObjectMapper om;

    public GetBackendStatusHandler() {
        this(new BackendStatusServiceMyBatisImpl(), new ObjectMapper());
    }

    public GetBackendStatusHandler(BackendStatusService service, ObjectMapper om)  {
        this.service = service != null ? service : new BackendStatusServiceMyBatisImpl();
        this.om = om != null ? om : new ObjectMapper();
    }

    public void handleAction(ActionParameters params) throws ActionException {
        boolean alert = isRequestOnlyForLayersWithAlerts(params);
        List<BackendStatus> statuses;
        if (alert) {
            // findAllWithAlert() MUST NOT return statuses that should be modified for the frontend
            statuses = service.findAllWithAlert();
        } else {
            statuses = modifyErrorsWithUnknownMessageToUnknown(service.findAll());
        }
        statuses = modifyErrorsWithUnknownMessageToUnknown(statuses);
        LOG.debug("BackendStatus list size: " + statuses.size());

        try {
            byte[] b = serialize(om, statuses);
            ResponseHelper.writeResponse(params, 200, ResponseHelper.CONTENT_TYPE_JSON_UTF8, b);
        } catch (JsonProcessingException e) {
            LOG.warn(e, "Failed to write JSON!");
            throw new ActionException("Failed to serialize response to JSON!", e);
        }
    }

    /**
     * If the service is unrecognized by the Monitor API it's status is ERROR with statusMessage
     * 'Unknown service' or 'Unknown offering ${foobar}'. Convert these to UNKNOWN for the frontend
     */
    private List<BackendStatus> modifyErrorsWithUnknownMessageToUnknown(List<BackendStatus> statuses) {
        List<BackendStatus> modified = new ArrayList<>(statuses.size());
        for (BackendStatus status : statuses) {
            if ("ERROR".equals(status.getStatus())
                    && status.getStatusMessage() != null
                    && status.getStatusMessage().startsWith("Unknown")) {
                modified.add(new BackendStatus(status.getMapLayerId(), "UNKNOWN", null, status.getInfoUrl()));
            } else {
                modified.add(status);
            }
        }
        return modified;
    }

    protected static byte[] serialize(ObjectMapper om, List<BackendStatus> statuses) throws JsonProcessingException {
        return om.writeValueAsBytes(new BackendStatusResponse(statuses));
    }

    protected boolean isRequestOnlyForLayersWithAlerts(ActionParameters params) {
        String subset = params.getHttpParam(PARAM_SUBSET);
        return !SUBSET_ALL_KNOWN.equals(subset);
    }

    private static class BackendStatusResponse {

        @JsonSerialize(using = BackendStatusResponseSerializer.class)
        private final List<BackendStatus> backendstatus;

        private BackendStatusResponse(List<BackendStatus> backendstatus) {
            this.backendstatus = backendstatus;
        }

    }

    private static class BackendStatusResponseSerializer extends JsonSerializer<List<BackendStatus>> {

        @Override
        public void serialize(List<BackendStatus> value, JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (BackendStatus bs : value) {
                gen.writeStartObject();
                gen.writeNumberField("maplayer_id", bs.getMapLayerId());
                gen.writeStringField("status", bs.getStatus());
                gen.writeStringField("statusjson", bs.getStatusMessage());
                gen.writeStringField("infourl", bs.getInfoUrl());
                final Date ts = bs.getTimestamp();
                if (ts == null) {
                    gen.writeNullField("ts");
                } else {
                    gen.writeNumberField("ts", ts.getTime());
                }
                gen.writeEndObject();
            }
            gen.writeEndArray();
        }

    }

}