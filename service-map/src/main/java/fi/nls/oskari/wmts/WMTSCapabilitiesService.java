package fi.nls.oskari.wmts;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.ServiceCapabilitiesResult;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @deprecated Use org.oskari.capabilities.CapabilitiesService instead.
 */
@Deprecated
public class WMTSCapabilitiesService {


    public ServiceCapabilitiesResult getCapabilitiesResults (final String url, final String version,
                                                             final String user, final String pw,
                                                             final String currentCrs, final Set<String> systemCRSs)
                                                        throws ServiceException {
        ServiceCapabilitiesResult results = new ServiceCapabilitiesResult();
        results.setVersion(version);

        ServiceConnectInfo info = new ServiceConnectInfo(url, OskariLayer.TYPE_WMTS, version);
        info.setCredentials(user, pw);
        try {
            Map<String, LayerCapabilities> capabilitiesMap = CapabilitiesService.getLayersFromService(info);

            List<OskariLayer> layers = new ArrayList<>();
            final String[] languages = PropertyUtil.getSupportedLanguages();
            for (LayerCapabilities cap : capabilitiesMap.values()) {
                OskariLayer layer = new OskariLayer();
                layer.setType(OskariLayer.TYPE_WMTS);
                layer.setName(cap.getName());
                layer.setUrl(url);
                layer.setVersion(version);
                layer.setUsername(user);
                layer.setPassword(pw);

                for (String lang : languages) {
                    layer.setName(lang, cap.getTitle());
                }
                layer.setStyle(cap.getDefaultStyle());

                layer.setCapabilities(CapabilitiesService.toJSON(cap, systemCRSs));
                layers.add(layer);
            }
            results.setLayers(layers.stream()
                    .map(l -> LayerAdminJSONHelper.toJSON(l))
                    .collect(Collectors.toList()));

        } catch (IOException e) {
            throw new ServiceException("Error loading capabilities", e);
        } catch (ServiceException e) {
            // TODO: error handling?
            throw e;
        }

        return results;
    }

}
