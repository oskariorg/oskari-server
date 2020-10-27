package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.json.JSONObject;
import org.oskari.log.AuditLog;

import java.util.Map;

@OskariActionRoute("Logger")
public class LoggerHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(LoggerHandler.class);

    private static final String NAME_ROOT = "ROOT";

    /**
     * Returns current loggers and log levels
     * @param params
     */
    @Override
    public void handleGet(ActionParameters params) {
        final JSONObject response = new JSONObject();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        for (Map.Entry<String, LoggerConfig> entry : config.getLoggers().entrySet()) {
            String name = entry.getKey();
            if (name.isEmpty()) {
                name = NAME_ROOT;
            }
            JSONHelper.putValue(response, name, entry.getValue().getLevel());
        }

        ResponseHelper.writeResponse(params, response);
    }

    /**
     * Set a log level for logger (creating one if not specific match)
     * @param params
     */
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        String level = params.getRequiredParam("level");
        String loggerName = params.getHttpParam("name", NAME_ROOT);
        if (loggerName != null && !NAME_ROOT.equals(loggerName)) {
            LoggerConfig lc = getConfig(loggerName);
            if (lc == null) {
                // create a new one so we can give more granular logging
                lc = new LoggerConfig();
                config.addLogger(loggerName, lc);
            }
            lc.setLevel(Level.toLevel(level));
        } else {
            config.getRootLogger().setLevel(Level.toLevel(level));
        }

        // This causes all Loggers to refetch information from their LoggerConfig.
        context.updateLoggers();

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("name", loggerName)
                .withParam("level", level)
                .updated("Log level");

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(loggerName, level));
    }

    private LoggerConfig getConfig(String name) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        for (Map.Entry<String, LoggerConfig> entry : config.getLoggers().entrySet()) {
            if (entry.getKey().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Removes a logger (so custom added ones can be cleaned up without rebooting)
     * @param params
     */
    public void handleDelete(ActionParameters params) throws ActionException {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        String loggerName = params.getRequiredParam("name");
        config.removeLogger(loggerName);
        // This causes all Loggers to refetch information from their LoggerConfig.
        context.updateLoggers();

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("name", loggerName)
                .deleted("Logger");

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(loggerName, "Removed"));
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }
}