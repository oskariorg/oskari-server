package org.oskari.service.wfs.client;

public class OWSException {

    private final String exceptionCode;
    private final String locator;
    private final String exceptionText;

    public OWSException(String exceptionCode, String locator, String exceptionText) {
        this.exceptionCode = exceptionCode;
        this.locator = locator;
        this.exceptionText = exceptionText;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public String getLocator() {
        return locator;
    }

    public String getExceptionText() {
        return exceptionText;
    }

}
