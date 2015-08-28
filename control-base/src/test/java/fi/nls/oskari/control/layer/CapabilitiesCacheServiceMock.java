package fi.nls.oskari.control.layer;

import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;

/**
 * Created by SMAKINEN on 28.8.2015.
 */
public class CapabilitiesCacheServiceMock extends CapabilitiesCacheService {
    private String response = null;

    public CapabilitiesCacheServiceMock(final String response) {
        this.response = response;
    }
    @Override
    public OskariLayerCapabilities find(String url, String layertype) {
        OskariLayerCapabilities caps = new OskariLayerCapabilities();
        caps.setUrl(url);
        caps.setLayertype(layertype);
        caps.setData(response);
        return caps;
    }

    @Override
    public OskariLayerCapabilities save(OskariLayerCapabilities capabilities) {
        return null;
    }
}
