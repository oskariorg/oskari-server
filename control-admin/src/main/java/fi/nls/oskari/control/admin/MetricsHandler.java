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
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@OskariActionRoute("Metrics")
public class MetricsHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(MetricsHandler.class);

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
            JSONHelper.putValue(metricsJSON, "metrics", JSONHelper.createJSONObject(w.toString()));
        } catch (Exception e) {
            log.error(e, "Error writing metrics JSON");
        }

        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new MemoryUsageGaugeSet());
            JSONHelper.putValue(metricsJSON, "memory", JSONHelper.createJSONObject(w.toString()).optJSONObject("metrics"));
        } catch (Exception e) {
            log.error(e, "Error writing memory metrics");
        }

        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new GarbageCollectorMetricSet());
            JSONHelper.putValue(metricsJSON, "gc", JSONHelper.createJSONObject(w.toString()).optJSONObject("metrics"));
        } catch (Exception e) {
            log.error(e, "Error writing gc metrics");
        }
        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new ThreadStatesGaugeSet());
            JSONHelper.putValue(metricsJSON, "thread.states", JSONHelper.createJSONObject(w.toString()).optJSONObject("metrics"));
        } catch (Exception e) {
            log.error(e, "Error writing thread state metrics");
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