package fi.nls.oskari.map.data.service;

import fi.nls.oskari.domain.map.wfs.FeatureType;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.domain.map.wfs.WFSService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

/**
 * This is a helper class that fakes the old db/domain object model from the new DB source.
 * Use actual data from WFSLayerConfigurationService instead!
 */
@Deprecated
public class WFSCompatibilityHelper {

    private static final WFSLayerConfigurationService wfsConfigService = new WFSLayerConfigurationServiceIbatisImpl();

    public static WFSLayer getLayer(final int id) {

        WFSLayer wfsLayer = new WFSLayer();
        wfsLayer.setId(id);

        final WFSLayerConfiguration configuration = wfsConfigService.findConfiguration(wfsLayer.getId());

        // for backwards compatibility - these need to be defined
        final WFSService service = new WFSService();
        service.setGmlVersion(configuration.getGMLVersion());
        service.setGml2typeSeparator(ConversionHelper.getBoolean(configuration.isGML2Separator(), false));
        service.setUrl(configuration.getURL());
        service.setUsername(configuration.getUsername());
        service.setPassword(configuration.getPassword());
        // use proxy not available in new transport - default to false
        service.setUseProxy(false);
        wfsLayer.getSelectedWfsServices().add(service);

        final FeatureType ft = new FeatureType();
        ft.setQname(configuration.getFeatureElementQName());
        ft.setWfsService(service);
        ft.setBboxParameterName(configuration.getGMLGeometryProperty());

        final SelectedFeatureType sft = new SelectedFeatureType();
        sft.setMaxNumDisplayedItems(configuration.getMaxFeatures());
        sft.setFeatureType(ft);
        wfsLayer.getSelectedFeatureTypes().add(sft);

        return wfsLayer;
    }
}
