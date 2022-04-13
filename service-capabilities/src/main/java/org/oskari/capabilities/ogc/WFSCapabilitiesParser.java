package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.api.OGCAPIFeaturesService;
import org.oskari.capabilities.ogc.wfs.DescribeFeatureTypeParser;
import org.oskari.capabilities.ogc.wfs.DescribeFeatureTypeProvider;
import org.oskari.capabilities.ogc.wfs.WFSCapsParser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Oskari(OskariLayer.TYPE_WFS)
public class WFSCapabilitiesParser extends OGCCapabilitiesParser {

    private static final Logger LOG = LogFactory.getLogger(WFSCapabilitiesParser.class);
    private static final String OGC_API_VERSION = "3.0.0";
    private DescribeFeatureTypeProvider featureTypeProvider = new DescribeFeatureTypeProvider();

    public void setDescribeFeatureTypeProvider(DescribeFeatureTypeProvider provider) {
        featureTypeProvider = provider;
    }
    protected String getVersionParamName() {
        return "acceptVersions";
    }
    protected String getDefaultVersion() { return "1.1.0"; }
    protected String getExpectedContentType(String version) {
        if (OGC_API_VERSION.equals(version)) {
            return "json";
        }
        return getExpectedContentType();
    }

    public Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException {
        if (OGC_API_VERSION.equals(src.getVersion())) {
            try {
                OGCAPIFeaturesService service = OGCAPIFeaturesService.fromURL(src.getUrl(), src.getUser(), src.getPass());
                listToMap(getOGCAPIFeatures(service));
            } catch (JsonParseException | JsonMappingException e) {
                // Don't attach JsonParseException as its an IOException which is detected as root cause and wrong error is sent to user
                throw new ServiceException("Error parsing response from url: " + src.getUrl() + " Message: " + e.getMessage());
            } catch (Exception e) {
                throw new ServiceException("Error occured getting OAPIF capabilities", e);
            }
        }
        return super.getLayersFromService(src);
    }

    public Map<String, LayerCapabilities> parseLayers(String xml) throws ServiceException {
        throw new ServiceException("Not implemented");
        //return parseLayers(xml, getDefaultVersion());
    }

    public Map<String, LayerCapabilities> parseLayers(String response, String version, ServiceConnectInfo src) throws ServiceException {
        try {
            List<LayerCapabilitiesWFS> caps;
            if (OGC_API_VERSION.equals(version)) {
                caps = getOGCAPIFeatures(response);
            } else {
                caps = WFSCapsParser.parseCapabilities(response);
                // enhance with describe feature type data
                caps.forEach(c -> enhanceCapabilitiesData(c, src));
            }
            return listToMap(caps);
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WFS capabilities", e);
        }
    }

    protected void enhanceCapabilitiesData(LayerCapabilitiesWFS layer, ServiceConnectInfo src) {
        try {
            String url = contructDescribeFeatureTypeUrl(src.getUrl(), layer.getVersion(), layer.getName());
            String xml = featureTypeProvider.getDescribeContent(url, src.getUser(), src.getPass());
            if (xml == null) {
                LOG.info("DescribeFeatureType response not available:", src.getUrl());
                return;
            }
            layer.setFeatureProperties(DescribeFeatureTypeParser.parseFeatureType(xml));
        } catch (IOException e) {
            LOG.error("Unable to access wfs describe feature type: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to enhance wfs/parse feature type: " + e.getMessage());
        }
    }

    protected Map<String, LayerCapabilities> listToMap(List<LayerCapabilitiesWFS> caps) {
        Map<String, LayerCapabilities> layers = new HashMap<>();
        caps.forEach(layer -> layers.put(layer.getName(), layer));
        return layers;
    }

    protected List<LayerCapabilitiesWFS> getOGCAPIFeatures(String json) {
        try {
            OGCAPIFeaturesService service = OGCAPIFeaturesService.fromJSON(json);
            return getOGCAPIFeatures(service);
        } catch (IOException e) {}
        return Collections.emptyList();
    }

    protected List<LayerCapabilitiesWFS> getOGCAPIFeatures(OGCAPIFeaturesService service) {
        return service.getCollections().stream().map(collection -> {
                    String name = collection.getId();
                    String title = collection.getTitle();

                    LayerCapabilitiesWFS featureType = new LayerCapabilitiesWFS(name, title);
                    featureType.setVersion(OGC_API_VERSION);
                    featureType.setDescription(collection.getDescription());
                    featureType.setSrs(service.getSupportedEpsgCodes(name));
                    featureType.setSupportedCrsURIs(service.getSupportedCrsURIs(name));

                    featureType.setFormats(service.getSupportedFormats(name));
                    return featureType;
                })
                .collect(Collectors.toList());
    }

    protected String contructDescribeFeatureTypeUrl(String url, String version, String featureType) {
        String urlLC = url.toLowerCase();

        final Map<String, String> params = new HashMap<>();
        // check existing params
        if (!urlLC.contains("service=")) {
            params.put("service", getType());
        }
        if (!urlLC.contains("request=")) {
            params.put("request", "DescribeFeatureType");
        }
        if (!urlLC.contains("version=")) {
            if (version == null || version.isEmpty()) {
                version = getDefaultVersion();
            }
            params.put(getVersionParamName(), version);
        }
        if (!urlLC.contains("featuretype=")) {
            params.put("featuretype", featureType);
        }

        return IOHelper.constructUrl(url, params);
    }
}
