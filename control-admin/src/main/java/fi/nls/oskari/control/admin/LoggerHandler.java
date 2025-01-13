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
import org.oskari.cluster.ClusterManager;
import org.oskari.log.AuditLog;

import java.util.Map;
import java.util.regex.Pattern;

@OskariActionRoute("Logger")
public class LoggerHandler extends RestActionHandler {

    protected static final String CLUSTER_FUNCTIONALITY_ID = "logger";
    protected static final String CLUSTER_CMD_SET_PREFIX = "SET: ";
    protected static final String CLUSTER_CMD_REMOVE_PREFIX = "REM: ";
    protected static final String CLUSTER_CMD_SEPARATOR = "||||";

    private Logger LOG = LogFactory.getLogger(LoggerHandler.class);
    private static final String NAME_ROOT = "ROOT";

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

    public void init() {
        super.init();

        LOG.debug("Is clustered env:", ClusterManager.isClustered());
        if (ClusterManager.isClustered()) {
            LOG.info("Cluster aware cache:", getName());
            ClusterManager
                    .getClientFor(CLUSTER_FUNCTIONALITY_ID)
                    .addListener(getName(), (msg) -> handleClusterMsg(msg));
        }
    }

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

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String level = params.getRequiredParam("level");
        String loggerName = params.getHttpParam("name", NAME_ROOT);

        setLevel(loggerName, level);

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("name", loggerName)
                .withParam("level", level)
                .updated("Log level");

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(loggerName, level));
        notifyLevelChange(loggerName, level);
    }

    public void handleDelete(ActionParameters params) throws ActionException {
        String loggerName = params.getRequiredParam("name");
        removeLogger(loggerName);
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("name", loggerName)
                .deleted("Logger");

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(loggerName, "Removed"));
        notifyRemoval(loggerName);
    }

    /**
     * Set a log level for logger (creating one if not specific match)
     * @param name
     * @param level defaults to DEBUG
     */
    protected void setLevel(String name, String level) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        if (name != null && !NAME_ROOT.equals(name)) {
            LoggerConfig lc = getConfig(name);
            if (lc == null) {
                // create a new one so we can give more granular logging
                lc = new LoggerConfig();
                config.addLogger(name, lc);
            }
            lc.setLevel(Level.toLevel(level));
        } else {
            config.getRootLogger().setLevel(Level.toLevel(level));
        }

        // This causes all Loggers to refetch information from their LoggerConfig.
        context.updateLoggers();
    }

    /**
     * Removes a logger (so custom added ones can be cleaned up without rebooting)
     * @param name
     */
    protected void removeLogger(String name) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        config.removeLogger(name);
        // This causes all Loggers to refetch information from their LoggerConfig.
        context.updateLoggers();
    }

    /**
     * Iterates loggers to find exact match on name (or returns null if not found).
     * This is done because `config.getLoggerConfig(name)` doesn't return the logger we set up in setLevel() for a name
     * and might return other logger than we expect instead of the exact match.
     * @param name
     * @return
     */
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

    /* ************************************************
     * Cluster env methods
     * ************************************************
     */

    protected void handleClusterMsg(String data) {
        LOG.debug("Got message:", data, getName());
        if (data == null) {
            return;
        }
        if (data.startsWith(CLUSTER_CMD_SET_PREFIX)) {
            String msg = data.substring(CLUSTER_CMD_REMOVE_PREFIX.length());
            String[] parts = msg.split(Pattern.quote(CLUSTER_CMD_SEPARATOR));
            if (parts.length != 2) {
                LOG.warn("Got invalid setLevel message:", data);
                return;
            }
            // silently so we don't trigger a new cluster message
            setLevel(parts[0], parts[1]);
            return;
        }
        if (data.startsWith(CLUSTER_CMD_REMOVE_PREFIX)) {
            // silently so we don't trigger a new cluster message
            removeLogger(data.substring(CLUSTER_CMD_REMOVE_PREFIX.length()));
            return;
        }
        LOG.warn("Received unrecognized cluster msg:", data);
    }

    private void notifyLevelChange(String name, String level) {
        notifyCluster(CLUSTER_CMD_SET_PREFIX + name + CLUSTER_CMD_SEPARATOR + level);
    }

    private void notifyRemoval(String name) {
        notifyCluster(CLUSTER_CMD_REMOVE_PREFIX + name);
    }

    private void notifyCluster(String msg) {
        if (!ClusterManager.isClustered()) {
            return;
        }
        ClusterManager
                .getClientFor(CLUSTER_FUNCTIONALITY_ID)
                .sendMessage(getName(), msg);
    }
}