package fi.nls.oskari.service.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.internal.WFSGetCapabilities;
import org.oskari.capabilities.ogc.api.features.OGCAPIFeaturesService;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class OskariLayerCapabilitiesHelper {

    public static void setPropertiesFromCapabilitiesWFS(WFSDataStore data, OskariLayer ml,
                                                        Set<String> systemCRSs) throws ServiceException {
        try {
            SimpleFeatureSource source = data.getFeatureSource(ml.getName());
            WFSGetCapabilities capa = data.getWfsClient().getCapabilities();
            setPropertiesFromCapabilitiesWFS(capa, source, ml, systemCRSs);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't find layer: " + ml.getName());
        }
    }

    public static void setPropertiesFromCapabilitiesWFS(WFSGetCapabilities capa, SimpleFeatureSource source, OskariLayer ml,
                                                        Set<String> systemCRSs) throws ServiceException {
        ml.setCapabilities(LayerJSONFormatterWFS.createCapabilitiesJSON(capa, source, systemCRSs));
        ml.setCapabilitiesLastUpdated(new Date());
    }

    public static void setPropertiesFromCapabilitiesOAPIF(OGCAPIFeaturesService service, OskariLayer ml,
                                                          Set<String> systemCRSs) {
        ml.setCapabilities(LayerJSONFormatterWFS.createCapabilitiesJSON(service, ml.getName(), systemCRSs));
        ml.setCapabilitiesLastUpdated(new Date());
    }

}
