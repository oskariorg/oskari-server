package org.oskari.status;

import fi.nls.oskari.service.OskariComponent;

/**
 * Get status with:
 *   OskariComponentManager.getComponentsOfType(AppStatus.class)
 *
 * Usage example:
 boolean highSeverityChecksOk = getChecks().stream()
 .filter(s -> s.isEnabled())
 .filter(s -> s.getSeverity() == AppStatus.Severity.HIGH)
 .allMatch(s -> s.isOk());
 */
public abstract class AppStatus extends OskariComponent {

    public enum Severity {
        // allow app to keep running
        LOW,
        // app should stop responding if this is not OK
        HIGH
    };

    public enum Level {
        // all good
        OK,
        // parts of the feature is working while others are not
        PARTIAL,
        // not working at all
        BROKEN
    };

    /**
     * Name for the check that could be shown in a status monitor
     * @return
     */
    public String getName() {
        return this.getClass().getName();
    }

    public boolean isEnabled() {
        return true;
    }
    /**
     * Describe what is being checked
     * @return
     */
    public String getDescription() {
        return "";
    }

    /**
     * Provide reason for check failing
     * @return
     */
    public String getReason() {
        return "";
    }

    /**
     * LOW if the app can run even without this feature working
     * HIGH if the app should be stopped when this functionality is not working
     * @return
     */
    public abstract Severity getSeverity();

    /**
     * Level of functionality: ok, partial, broken == not working
     * @return
     */
    public abstract Level getLevel();

    public boolean isOk() {
        return Level.OK.equals(getLevel());
    }
}
