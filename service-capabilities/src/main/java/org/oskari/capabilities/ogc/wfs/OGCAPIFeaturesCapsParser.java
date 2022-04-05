package org.oskari.capabilities.ogc.wfs;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OGCAPIFeaturesCapsParser {

    private static ServiceCapabilitiesResult getCapabilitiesOAPIF(String url, String user, String pw, Set<String> systemCRSs) throws ServiceException {
        WFS3Service service = getCapabilitiesOAPIF(url, user, pw);
        List<OskariLayer> layers = new ArrayList<>();

        for (WFS3CollectionInfo collection : service.getCollections()) {
            String name = collection.getId();
            String title = collection.getTitle();
            OskariLayer ml = toOskariLayer(name, title, WFS3_VERSION, url, user, pw);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesOAPIF(service, ml, systemCRSs);
            layers.add(ml);
        }
        ServiceCapabilitiesResult result = new ServiceCapabilitiesResult();
        result.setVersion(WFS3_VERSION);
        result.setLayers(layers.stream()
                .map(l -> LayerAdminJSONHelper.toJSON(l))
                .collect(Collectors.toList()));
        // Do we need to parse title from WFS3Content.links
        return result;
    }
}
