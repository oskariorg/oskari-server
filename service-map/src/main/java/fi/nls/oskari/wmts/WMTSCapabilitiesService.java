package fi.nls.oskari.wmts;


import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.util.*;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class WMTSCapabilitiesService {
    private CapabilitiesCacheService capabilitiesService = ServiceFactory.getCapabilitiesCacheService();

    public Map<String, Object> getCapabilitiesResults (final String url, final String version,
                                                       final String user, final String pw,
                                                       final String currentCrs, final Set<String> systemCRSs)
                                                        throws ServiceException {

        OskariLayerCapabilities caps = capabilitiesService.getCapabilities(url, OskariLayer.TYPE_WMTS, version, user, pw);
        String capabilitiesXML = caps.getData();
        WMTSCapabilities wmtsCaps = parseXML(capabilitiesXML);

        if (caps.getId() == null) {
            capabilitiesService.save(caps);
        }
        // start building result
        Map<String, Object> results = new HashMap<>();
        List<OskariLayer> layers = new ArrayList<>();
        List<String> unsupportedLayers = new ArrayList<>();
        for (WMTSCapabilitiesLayer layer : wmtsCaps.getLayers()) {
            String matrixsetId = WMTSCapabilitiesParser.getMatrixSetId(layer.getLinks(), currentCrs);
            OskariLayer ml = layer.getOskariLayer(url, matrixsetId);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(wmtsCaps, ml, systemCRSs);
            if (matrixsetId == null) {
                unsupportedLayers.add(ml.getName());
            }
            layers.add(ml);
        }

        JSONObject matrixNode = new JSONObject();
        for (TileMatrixSet matrix : wmtsCaps.getTileMatrixSets()) {
            JSONHelper.putValue(matrixNode, matrix.getId(), matrix.getAsJSON());
        }
        results.put(KEY_LAYERS, layers);
        results.put(KEY_UNSUPPORTED_LAYERS, unsupportedLayers);
        results.put(KEY_WMTS_MATRIXSET, matrixNode);
        //results.put(CapabilitiesConstants.KEY_XML, capabilitiesXML); // TODO is raw xml used in frontend?
        return results;
    }

    public WMTSCapabilities updateCapabilities (OskariLayer ml) throws ServiceException {
        String data =  capabilitiesService.getFromService(ml);
        WMTSCapabilities caps = parseXML(data);
        //update after parsing to cache valid xml
        capabilitiesService.save(ml, data);
        return caps;
    }

    private WMTSCapabilities parseXML (String xml) throws ServiceException {
        try {
            return WMTSCapabilitiesParser.parseCapabilities(xml);
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMTS capabilities from xml.", e);
        }
    }
}
