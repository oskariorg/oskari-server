package fi.nls.oskari.transport;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.CachingSchemaLocator;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.work.JobHelper;
import fi.nls.oskari.work.hystrix.HystrixJobQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

/**
 * Shows some status data for admin users
 */
public class StatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LogFactory.getLogger(StatusServlet.class);

    public static final String PARAM_SESSION = "session";
    public static final String PARAM_CLEANUP = "clean";
    // action user uid API
    private static final String UID_API = "GetCurrentUser";

    private static ObjectMapper jsonMapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS,
            TimeUnit.SECONDS,
            true,
            MetricFilter.ALL));

    /**
     * Call with /transport/status?session=<jsessionid> when logged in as admin. Prints out status data.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // params
        final String session = getSessionId(request);
        final User user = getOskariUser(session, request.getParameter("route"));
        if (user == null || !user.isAdmin()) {
            /*
            For this to work in environment without database access - add these properties to transport-ext.properties:
            # Needed by status servlet if other than "Admin"
            oskari.user.role.admin = Administrator
            # User service implementation
            oskari.user.service=fi.nls.oskari.service.DummyUserService
            */

            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Couldn't get user information");
            return;
        }
        // force clean up
        final String cleanup = request.getParameter(PARAM_CLEANUP);
        if(cleanup != null && "true".equalsIgnoreCase(cleanup)) {
            TransportService.getQueue().cleanup(true);
        }
        response.setContentType("application/json");
        response.getWriter().write(getStatusMessage());

    }

    public String getSessionId(HttpServletRequest request) {
        final String sessionCookieName = "JSESSIONID";
        final Cookie[] cookies = request.getCookies();
        if(cookies != null && sessionCookieName != null) {
            for (int i=0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(sessionCookieName)) return cookies[i].getValue();
            }
        }
        return ConversionHelper.getString(request.getParameter(PARAM_SESSION), request.getRequestedSessionId());
    }

    public static String getStatusMessage() {
        JSONObject metricsJSON = new JSONObject();
        JSONHelper.putValue(metricsJSON, "schema.cache.size", CachingSchemaLocator.getCacheSize());
        final HystrixJobQueue q = (HystrixJobQueue)TransportService.getQueue();
        JSONHelper.putValue(metricsJSON, "queue.size.current",  q.getQueueSize());
        JSONHelper.putValue(metricsJSON, "queue.size.max",  q.getMaxQueueLength());
        JSONHelper.putValue(metricsJSON, "queue.job.length.min",  q.getMinJobLength());
        JSONHelper.putValue(metricsJSON, "queue.job.length.max",  q.getMaxJobLength());
        JSONHelper.putValue(metricsJSON, "queue.job.length.avg",  q.getAvgRuntime());
        JSONHelper.putValue(metricsJSON, "queue.job.count",  q.getJobCount());
        JSONHelper.putValue(metricsJSON, "queue.job.count.crashed",  q.getCrashedJobCount());
        JSONHelper.putValue(metricsJSON, "queue.job.crashed.first",  q.getFirstCrashedJob());
        JSONHelper.putValue(metricsJSON, "queue.jobs",  new JSONArray(q.getQueuedJobNames()));


        // dropwizard metrics
        MetricRegistry metrics = q.getMetricsRegistry();
        ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, metrics);
            JSONHelper.putValue(metricsJSON, "HystrixJobQueue", JSONHelper.createJSONObject(w.toString()));
        } catch (Exception e) {
            log.error(e, "Error writing metrics JSON");
        }

        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new MemoryUsageGaugeSet());
            JSONHelper.putValue(metricsJSON, "memory", JSONHelper.createJSONObject(w.toString()).optJSONObject("metrics"));
        } catch (Exception e) {
            log.error(e, "Error writing metrics JSON");
        }

        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new GarbageCollectorMetricSet());
            JSONHelper.putValue(metricsJSON, "gc", JSONHelper.createJSONObject(w.toString()).optJSONObject("metrics"));
        } catch (Exception e) {
            log.error(e, "Error writing metrics JSON");
        }
        try {
            StringWriter w = new StringWriter();
            writer.writeValue(w, new ThreadStatesGaugeSet());
            JSONHelper.putValue(metricsJSON, "thread.states", JSONHelper.createJSONObject(w.toString()).optJSONObject("metrics"));
        } catch (Exception e) {
            log.error(e, "Error writing metrics JSON");
        }


        try {
            return metricsJSON.toString(3);
        } catch (Exception ignored) { }
        return  metricsJSON.toString();
    }

    private User getOskariUser(final String sessionId, final String route) {
        log.debug("Getting user from:", JobHelper.getAPIUrl() + UID_API);
        final String response = HttpHelper.getRequest(JobHelper.getAPIUrl() + UID_API,
                JobHelper.getCookiesValue(sessionId, route));
        final JSONObject json = JSONHelper.createJSONObject(response);
        return User.parse(json);
    }
}
