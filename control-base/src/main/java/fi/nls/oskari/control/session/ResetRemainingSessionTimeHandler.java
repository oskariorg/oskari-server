package fi.nls.oskari.control.session;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

/**
 * ActionRoute is intended to call when user indicates activity (e.g. mousemove-
 * event) to extend session alive time. Handler resets remaining time
 * of session associated with the request (Done behind the scenes when HttpRequest is received).
 * Information about remaining session time is returned in case of successful execution for verification / debug purposes.
 * 
 */
@OskariActionRoute("ResetRemainingSessionTime")
public class ResetRemainingSessionTimeHandler extends ActionHandler {

    private static final Logger LOGGER = LogFactory.getLogger(ResetRemainingSessionTimeHandler.class);
    private static final String RESULT_MESSAGE_KEY = "result";
    private static final String SESSION_TIME_EXTENDED = "Session time extended. Remaining session time is %s seconds";
    private static final String NO_SESSION = "Session could not be determined based on request";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        HttpSession session = params.getRequest().getSession(false);
        JSONObject result = null;

        if (session != null) {

            long nowInMilliSeconds = System.currentTimeMillis();
            long lastAccessedInMilliSeconds = session.getLastAccessedTime();
            long timeoutPeriodInSeconds = session.getMaxInactiveInterval();
            long remainingTimeInSeconds = ((timeoutPeriodInSeconds * 1000)
                    - (nowInMilliSeconds - lastAccessedInMilliSeconds)) / 1000;

            if (LOGGER.isDebugEnabled()) {

                LocalDateTime now = getDateTimeFromEpochMilliseconds(nowInMilliSeconds);
                LocalDateTime lastAccessed = getDateTimeFromEpochMilliseconds(lastAccessedInMilliSeconds);

                LOGGER.debug(
                        String.format("now=%s, lastAccessed=%s, timeoutPeriodInSeconds=%s, remainingTimeInSeconds=%s",
                                now, lastAccessed, timeoutPeriodInSeconds, remainingTimeInSeconds));
            }
            result = JSONHelper.createJSONObject(RESULT_MESSAGE_KEY,
                    String.format(SESSION_TIME_EXTENDED, remainingTimeInSeconds));
        } else {
            result = JSONHelper.createJSONObject(RESULT_MESSAGE_KEY, NO_SESSION);
        }
        ResponseHelper.writeResponse(params, result);
    }

    private LocalDateTime getDateTimeFromEpochMilliseconds(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
    }
}
