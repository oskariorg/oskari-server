package fi.nls.oskari.transport;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.CachingSchemaLocator;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.work.WFSMapLayerJob;
import fi.nls.oskari.worker.JobQueue;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Serves images from cache (also temp)
 */
public class StatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LogFactory.getLogger(StatusServlet.class);

    public static final String PARAM_SESSION = "session";
    // action user uid API
    private static final String UID_API = "GetCurrentUser";

    /**
     * Call with /transport/status?session=<jsessionid> when logged in as admin. Prints out status data.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // params
        final String session = ConversionHelper.getString(request.getParameter(PARAM_SESSION), request.getRequestedSessionId());
        final User user = getOskariUser(session, request.getParameter("route"));
        if (user == null || !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't get user information");
            return;
        }
        response.getWriter().write("Logged in as: " + user.toJSON());
        response.getWriter().write("\n\nStatus\n\n");
        response.getWriter().write(getStatusMessage());

    }

    public static String getStatusMessage() {
        StringWriter w = new StringWriter();
        w.append("Schema cache size: " + CachingSchemaLocator.getCacheSize());
        w.append("\n");
        final JobQueue q = TransportService.getQueue();
        w.append("Queue current size: " + q.getQueueSize() + "/ max:" + q.getMaxQueueLength());
        w.append("\n");
        w.append("Queue job length(ms) min: " + q.getMinJobLength() + "/ max:" + q.getMaxJobLength() + "/ avg:" + q.getAvgRuntime());
        w.append("\n");
        w.append("Queue jobs processed: " + q.getJobCount());
        w.append("\n");
        w.append("Crashed jobs: " + q.getCrashedJobCount() + "/ first was: " + q.getFirstCrashedJob());
        return w.toString();
    }

    private User getOskariUser(final String sessionId, final String route) {
        log.debug("Getting user from:", WFSMapLayerJob.getAPIUrl(sessionId) + UID_API);
        String cookies = null;
        if (route != null && !route.equals("")) {
            cookies = WFSMapLayerJob.ROUTE_COOKIE_NAME + route;
        }
        final String response = HttpHelper.getRequest(WFSMapLayerJob.getAPIUrl(sessionId) + UID_API, cookies);
        final JSONObject json = JSONHelper.createJSONObject(response);
        return User.parse(json);
    }
}