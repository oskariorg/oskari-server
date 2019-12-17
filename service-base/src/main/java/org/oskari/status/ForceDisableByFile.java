package org.oskari.status;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.PropertyUtil;

import java.io.File;

@Oskari
public class ForceDisableByFile extends AppStatus {

    private String fileToCheck;

    public void init() {
        fileToCheck = PropertyUtil.get("disablefile.path", null);
    }

    public boolean isEnabled() {
        return fileToCheck != null;
    }

    /**
     * File found -> app should be disabled
     * not found -> OK
     * @return
     */
    public Level getLevel() {
        File status = new File(fileToCheck);
        if(status.exists()) {
            return Level.BROKEN;
        }
        return Level.OK;
    }

    public String getDescription() {
        return "Checks if file '" + fileToCheck + "' exists.";
    }

    /**
     * Provide reason for check failing
     * @return
     */
    public String getReason() {
        return fileToCheck + " file exists";
    }

    /**
     * This is a manual check for file. If it exists -> the app should be stopped
     * @return
     */
    public Severity getSeverity() {
        return Severity.HIGH;
    }
}
