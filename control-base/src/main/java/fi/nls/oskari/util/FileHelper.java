package fi.nls.oskari.util;

import java.io.File;
import java.util.List;

/**
 * Helper class to for files.
 * Used in CreateUserLayerHandler
 */
public class FileHelper {
    private String fileName = null;
    private String baseName = null;
    private String extension = null;

    // for reference, need to be set with setter
    private String savedTo = null;

    public FileHelper(String param) {
        File f = new File(param);
        // name without path
        fileName = f.getName();
        int i = fileName.lastIndexOf(".");
        if(i != -1) {
            baseName = fileName.substring(0, i);
            extension = fileName.substring(i + 1);
        }
    }

    public File getFile() {
        if(getSavedTo() != null) {
            return new File(getSavedTo());
        }
        return null;
    }

    public String getSavedTo() {
        return savedTo;
    }

    public void setSavedTo(String savedTo) {
        this.savedTo = savedTo;
    }

    public String getFilename() {
        return fileName;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getExtension() {
        return extension;
    }

    public boolean hasNameAndExtension() {
        return baseName != null && extension != null;
    }

    public String getSafeName() {
        return baseName.replace(".", "_") + "." + getExtension();
    }

    public boolean isOfType(List<String> fileExtensions) {
        return fileExtensions.contains(getExtension().toUpperCase());
    }
}
