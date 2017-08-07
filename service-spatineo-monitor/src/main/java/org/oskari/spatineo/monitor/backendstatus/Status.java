package org.oskari.spatineo.monitor.backendstatus;

public enum Status {

    OK, DOWN, MAINTENANCE, UNKNOWN, UNSTABLE;

    public static Status getEnumByNewAPI(String val) {
        switch (val) {
        case "NO_INDICATOR":
            return UNKNOWN;
        case "NO_ALERTS":
            return UNKNOWN;
        case "NEW":
            return OK;
        case "OK":
            return OK;
        case "WARNING":
            return UNSTABLE;
        case "ALERT":
            return DOWN;
        case "INSUFFICIENT_DATA":
            return UNKNOWN;
        default:
            return UNKNOWN;
        }
    }

}
