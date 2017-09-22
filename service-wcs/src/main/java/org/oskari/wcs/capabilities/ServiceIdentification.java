package org.oskari.wcs.capabilities;

import java.util.List;

public class ServiceIdentification {

    private final String title;
    private final String serviceType;
    private final List<String> serviceTypeVersion;
    private final List<String> profile;

    public ServiceIdentification(String title, String serviceType, List<String> serviceTypeVersion,
            List<String> profile) {
        this.title = title;
        this.serviceType = serviceType;
        this.serviceTypeVersion = serviceTypeVersion;
        this.profile = profile;
    }

    public String getTitle() {
        return title;
    }

    public String getServiceType() {
        return serviceType;
    }

    public List<String> getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    public List<String> getProfile() {
        return profile;
    }

}
