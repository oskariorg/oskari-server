package fi.nls.oskari.control.admin;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

@OskariActionRoute("Metrics")
public class MetricsHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(MetricsHandler.class);
    private static final String KEY_METRICS = "metrics";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(
            new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, true, MetricFilter.ALL));

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        // only available for admins
        params.requireAdminUser();

        // dropwizard metrics
        MetricRegistry metrics = ActionControl.getMetrics();

        JSONObject metricsJSON = new JSONObject();
        ObjectWriter writer = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, metrics);
            JSONHelper.putValue(metricsJSON, KEY_METRICS, JSONHelper.createJSONObject(w.toString()));
        } catch (Exception e) {
            LOG.error(e, "Error writing metrics JSON");
        }

        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new MemoryUsageGaugeSet());
            JSONHelper.putValue(metricsJSON, "memory", JSONHelper.createJSONObject(w.toString()).optJSONObject(KEY_METRICS));
        } catch (Exception e) {
            LOG.error(e, "Error writing memory metrics");
        }

        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new GarbageCollectorMetricSet());
            JSONHelper.putValue(metricsJSON, "gc", JSONHelper.createJSONObject(w.toString()).optJSONObject(KEY_METRICS));
        } catch (Exception e) {
            LOG.error(e, "Error writing gc metrics");
        }
        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new ThreadStatesGaugeSet());
            JSONHelper.putValue(metricsJSON, "thread.states", JSONHelper.createJSONObject(w.toString()).optJSONObject(KEY_METRICS));
        } catch (Exception e) {
            LOG.error(e, "Error writing thread state metrics");
        }

        ResponseHelper.writeResponse(params, metricsJSON);
    }


    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}