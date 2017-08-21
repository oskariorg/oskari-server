package fi.nls.oskari.service.capabilities;

import java.sql.Timestamp;
import java.util.List;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;

public class CapabilitiesCacheServiceMock extends CapabilitiesCacheService {

    private final String response;

    public CapabilitiesCacheServiceMock(final String response) {
        this.response = response;
    }

    @Override
    public OskariLayerCapabilities find(String url, String layertype, String version) {
        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        return new OskariLayerCapabilities(10L, url, layertype, version, response, ts, ts);
    }

    @Override
    public OskariLayerCapabilities save(OskariLayerCapabilities draft) {
        return draft;
    }

    public OskariLayerCapabilities getCapabilities(OskariLayer layer) throws ServiceException {
        if(layer.getType().equals("ERROR")) {
            throw new ServiceException("Testcase");
        }
        return super.getCapabilities(layer);
    }

    @Override
    protected void updateMultiple(List<OskariLayerCapabilities> caps) {
        // Do nothing
    }

    @Override
    protected List<OskariLayerCapabilities> getAllOlderThan(long maxAgeMs) {
        return null;
    }
}
