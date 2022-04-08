package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.ServiceCapabilitiesResult;
import org.oskari.utils.common.Sets;

import java.util.*;
import java.util.stream.Collectors;

public class WFSCapabilitiesService {
    private static Logger log = LogFactory.getLogger(WFSCapabilitiesService.class);
    protected static Set<String> SUPPORTED_FEATURE_FORMATS = new HashSet<>
            (Arrays.asList("text/xml; subtype=gml/3.1.1",
                    "text/xml; subtype=gml/3.2",
                    "application/gml+xml; version=3.2",
                    "application/json",
                    "text/xml"));

    public static ServiceCapabilitiesResult getCapabilitiesResults (String url, String version, String user, String pw,
                                                                    Set<String> systemCRSs)
                                                                throws ServiceException {
        try {
            ServiceConnectInfo info = new ServiceConnectInfo(url, OskariLayer.TYPE_WFS, version);
            info.setCredentials(user, pw);
            Map<String, LayerCapabilities> caps = CapabilitiesService.getLayersFromService(info);
            return parseCapabilitiesResults(caps, url, user, pw, systemCRSs);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException ("Failed to get capabilities version: " + version + " from url: " + url, e);
        }
    }

    protected static ServiceCapabilitiesResult parseCapabilitiesResults(Map<String, LayerCapabilities> caps, String url,
                                                                           String user, String pwd, Set<String> systemCRSs) {

        List<OskariLayer> layers = caps.values().stream()
                .map(layer -> toOskariLayer((LayerCapabilitiesWFS) layer, url, user, pwd, systemCRSs))
                .filter(l -> l != null)
                .collect(Collectors.toList());

        ServiceCapabilitiesResult results = new ServiceCapabilitiesResult();
        results.setTitle("N/A");
        if (!layers.isEmpty()) {
            results.setVersion(layers.get(0).getVersion());
            results.setLayers(layers.stream()
                    .map(l -> LayerAdminJSONHelper.toJSON(l))
                    .collect(Collectors.toList()));
        }

        return results;
    }

    private static OskariLayer toOskariLayer(LayerCapabilitiesWFS layer, String url, String user, String pw, Set<String> systemCRSs) {
        final OskariLayer ml = new OskariLayer();
        ml.setType(OskariLayer.TYPE_WFS);
        ml.setUrl(url);
        ml.setMaxScale(1d);
        ml.setMinScale(1500000d);
        ml.setName(layer.getName());
        ml.setVersion(layer.getVersion());
        ml.setPassword(pw);
        ml.setUsername(user);
        String title = layer.getTitle();
        if (title == null || title.isEmpty()) {
            title = layer.getName();
        }
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            ml.setName(lang, title);
        }
        ml.setCapabilities(CapabilitiesService.toJSON(layer, systemCRSs));
        return ml;
    }
    protected static Set<String> getFormatsToStore (Set<String> formats) {
        return Sets.intersection(SUPPORTED_FEATURE_FORMATS, formats);
    }


}

