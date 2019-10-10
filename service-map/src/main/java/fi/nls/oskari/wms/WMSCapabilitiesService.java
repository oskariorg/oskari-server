package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;

import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Service;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.xml.MetadataURL;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.oskari.service.util.ServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class WMSCapabilitiesService {
    private static final Logger log = LogFactory.getLogger(WMSCapabilitiesService.class);
    private CapabilitiesCacheService capabilitiesService = ServiceFactory.getCapabilitiesCacheService();

    public WebMapService updateCapabilities (OskariLayer ml) throws ServiceException {
        String data =  CapabilitiesCacheService.getFromService(ml);
        WebMapService wms;
        try {
            wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(data, ml);
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMS capabilities xml", e);
        }
        //update after parsing to cache valid xml
        capabilitiesService.save(ml, data);

        return wms;
    }
    public Map<String, Object> getCapabilitiesResults (final String url, final String version, final String user, final String pwd,
                                                              final Set<String> systemCRSs) throws ServiceException {
        try {
            final OskariLayerCapabilities capabilities = capabilitiesService.getCapabilities(url, OskariLayer.TYPE_WMS, version, user, pwd);
            final String data = capabilities.getData();
            WMSCapabilities caps = createCapabilities(data);
            Map<String, Object> toReturn = parseCapabilities(caps.getLayer(), url, caps, data, systemCRSs, false);
            if (capabilities.getId() == null) {
                capabilitiesService.save(capabilities);
            }
            return toReturn;
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url: " + url, ex);
        }

    }
    public static Map<String, Object> parseCapabilities(Layer layer, String url, WMSCapabilities caps, String capabilitiesXML, Set<String> systemCRSs, boolean recursiveCall)
            throws ServiceException {
        final Map<String, Object> result = new HashMap<>();
        if (layer == null) {
            throw new ServiceException("Failed to get capabilities");
        }
        try {
            result.put(KEY_TITLE, layer.getTitle());
            result.put(KEY_VERSION, caps.getVersion());
            List<Layer> sublayers = layer.getLayerChildren();
            if (sublayers != null && !sublayers.isEmpty()) {
                // note that this group layer is NOT the same as Oskari groupMap but a
                // presentational group "root" node which will be handled in admin bundle
                result.put(KEY_WMS_TYPE, WMS_GROUP_LAYER_TYPE);
                List <Map<String, Object>> groups = new ArrayList();
                List <OskariLayer> layers = new ArrayList();
                result.put(KEY_WMS_GROUPS, groups);
                result.put(KEY_LAYERS, layers);
                String layerName = layer.getName();
                if (layerName != null && !layerName.isEmpty()) {
                    // add self to layers if we have a wmsName so
                    // the group node layers are selectable as well on frontend
                    final OskariLayer self = layerToOskariLayer(layer, url, caps, capabilitiesXML, systemCRSs);
                    result.put(KEY_WMS_SELF_LAYER, self);
                }
                for (Layer sublayer : sublayers ) {
                    if (sublayer != null) {
                        final Map<String, Object> child = parseCapabilities(sublayer, url, caps, capabilitiesXML, systemCRSs, true);
                        final String type = (String) child.get(KEY_WMS_TYPE);
                        if (WMS_GROUP_LAYER_TYPE.equals(type)) {
                            groups.add(child);
                        } else {
                            layers.addAll((List) child.get(KEY_LAYERS));
                        }
                    }
                }
                return result;
            } else {
                //handle a single layer or a sublayer (=recursive call)
                if (recursiveCall) {
                    return Collections.singletonMap(KEY_LAYERS,
                            Collections.singletonList(layerToOskariLayer(layer, url, caps, capabilitiesXML,  systemCRSs)));
                } else {
                    //handle the case where there actually is just one layer
                    result.put(KEY_WMS_TYPE, OskariLayer.TYPE_WMS);
                    result.put(KEY_LAYERS,
                            Collections.singletonList(layerToOskariLayer(layer, url, caps, capabilitiesXML, systemCRSs)));
                    return result;
                }
            }
        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wms capabilities layer", ex);
        }
    }
    public static OskariLayer layerToOskariLayer(Layer capabilitiesLayer, String rurl, WMSCapabilities caps, String capabilitiesXML,
                                                 Set<String> systemCRSs) {

        final OskariLayer oskariLayer = new OskariLayer();
        final String layerName = capabilitiesLayer.getName();
        oskariLayer.setType(OskariLayer.TYPE_WMS);
        oskariLayer.setUrl(rurl);
        // THIS IS ON PURPOSE: min -> max, max -> min
        oskariLayer.setMaxScale(capabilitiesLayer.getScaleDenominatorMin());
        oskariLayer.setMinScale(capabilitiesLayer.getScaleDenominatorMax());
        oskariLayer.setName(layerName);
        oskariLayer.setVersion(caps.getVersion());

        // setup UI names for all supported languages
        final String[] languages = PropertyUtil.getSupportedLanguages();
        for (String lang : languages) {
            oskariLayer.setName(lang, capabilitiesLayer.getTitle());
        }

        // JSON formatter will parse uuid from url
        /*
        OnlineResource xlink:type="simple" xlink:href="http://www.paikkatietohakemisto.fi/geonetwork/srv/en/main.home?uuid=a22ec97f-d418-4957-9b9d-e8b4d2ec3eac"/>
        <inspire_common:MetadataUrl xsi:type="inspire_common:resourceLocatorType"><inspire_common:URL>http://www.paikkatietohakemisto.fi/geonetwork/srv/fi/iso19139.xml?uuid=a22ec97f-d418-4957-9b9d-e8b4d2ec3eac</inspire_common:URL>
        */
        oskariLayer.setMetadataId(getMetaDataUrl(caps.getService()));
        final List<MetadataURL> meta = capabilitiesLayer.getMetadataURL();
        if (meta != null) {
            if (meta.size() > 0 && meta.get(0).getUrl() != null) {
                oskariLayer.setMetadataId(meta.get(0).getUrl().toString());
            }
        }

        try {
            WebMapService wmsImpl = WebMapServiceFactory.createFromXML(oskariLayer.getName(), capabilitiesXML);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wmsImpl, oskariLayer, systemCRSs);
        } catch (Exception ex) {
            log.warn ("Couldn't parse capabilities for WMS layer:", layerName, "message:", ex.getMessage());
        }
        return oskariLayer;
    }
    static private String getMetaDataUrl(Service service) {
        if (service.getOnlineResource() != null) {
            return service.getOnlineResource().toString();
        }
        return null;
    }
    public static WMSCapabilities createCapabilities(String xml) {
        if(xml == null || xml.isEmpty()) {
            return null;
        }
        final Map hints = new HashMap();
        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
        hints.put(DocumentFactory.VALIDATION_HINT, false);
        try(InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            final Object object = DocumentFactory.getInstance(stream, hints, Level.WARNING);
            if(object instanceof WMSCapabilities) {
                return (WMSCapabilities) object;
            }
        } catch (Exception ex) {
            log.error(ex, "Error reading WMS capabilities");
        }

        return null;
    }
}
