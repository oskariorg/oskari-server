package org.oskari.service.wfs3.model;

public class WFS3Exception extends Exception {

    private static final long serialVersionUID = 1L;

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
