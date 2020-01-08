package fi.nls.oskari;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.status.AppStatus;

import java.io.File;

@Oskari
public class ForceDisableByFile extends AppStatus {

    private String fileToCheck;

    private static final String PROP_FILE_REF = "disablefile.path";
    public void init() {
        fileToCheck = PropertyUtil.get(PROP_FILE_REF, null);
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
        if (!isEnabled()) {
            return Level.OK;
        }
        File status = new File(fileToCheck);
        if(status.exists()) {
            return Level.BROKEN;
        }
        return Level.OK;
    }

    public String getDescription() {
        return "Checks if a configured disabling-file (oskari-ext.properties: " + PROP_FILE_REF + ") exists.";
    }

    /**
     * Provide reason for check failing
     * @return
     */
    public String getReason() {
        if (isOk()) {
            return "";
        }
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
