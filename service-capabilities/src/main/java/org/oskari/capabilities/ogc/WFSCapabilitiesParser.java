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
import org.oskari.capabilities.RawCapabilitiesResponse;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.api.OGCAPIFeatureItemsDescriber;
import org.oskari.capabilities.ogc.api.OGCAPIFeaturesService;
import org.oskari.capabilities.ogc.wfs.DescribeFeatureTypeParser;
import org.oskari.capabilities.ogc.wfs.DescribeFeatureTypeProvider;
import org.oskari.capabilities.ogc.wfs.WFSCapsParser;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Oskari(OskariLayer.TYPE_WFS)
public class WFSCapabilitiesParser extends OGCCapabilitiesParser {

    private static final Logger LOG = LogFactory.getLogger(WFSCapabilitiesParser.class);
    private static final String OGC_API_VERSION = "3.0.0";
    // The providers are used to make mocking for tests easier
    private DescribeFeatureTypeProvider featureTypeProvider = new DescribeFeatureTypeProvider();
    private OGCAPIFeatureItemsDescriber ogcAPIFeaturesProvider = new OGCAPIFeatureItemsDescriber();

    public void setDescribeFeatureTypeProvider(DescribeFeatureTypeProvider provider) {
        featureTypeProvider = provider;
    }
    public void setOGCAPIFeatureItemsDescriber(OGCAPIFeatureItemsDescriber provider) {
        ogcAPIFeaturesProvider = provider;
    }
    public Class<? extends LayerCapabilities> getCapabilitiesClass() {
        return LayerCapabilitiesWFS.class;
    }
    protected String getVersionParamName() {
        return "acceptVersions";
    }
    protected String getDefaultVersion() { return "1.1.0"; }
    public String getExpectedContentType(String version) {
        if (OGC_API_VERSION.equals(version)) {
            return "json";
        }
        return getExpectedContentType();
    }

    public Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException {
        if (OGC_API_VERSION.equals(src.getVersion())) {
            try {
                OGCAPIFeaturesService service = OGCAPIFeaturesService.fromURL(src.getUrl(), src.getUser(), src.getPass());
                return listToMap(getOGCAPIFeatures(service));
            } catch (JsonParseException | JsonMappingException e) {
                // Don't attach JsonParseException as its an IOException which is detected as root cause and wrong error is sent to user
                throw new ServiceException("Error parsing response from url: " + src.getUrl() + " Message: " + e.getMessage());
            } catch (Exception e) {
                throw new ServiceException("Error occured getting OAPIF capabilities", e);
            }
        }
        return super.getLayersFromService(src);
    }

    /*
     Optimization for older WFS versions that require multiple requests/layer.
     Services that have several featureTypes in them are very slow to parse with each featureType requiring additional HTTP requests.
     If we only need to update a single featureType this is very much faster.
     */
    public LayerCapabilities getLayerFromService(ServiceConnectInfo src, String featureType) throws IOException, ServiceException {
        if (OGC_API_VERSION.equals(src.getVersion())) {
            Map<String, LayerCapabilities> layers = getLayersFromService(src);
            LayerCapabilitiesWFS collection = (LayerCapabilitiesWFS) layers.get(featureType);
            enhanceOGCAPIFeaturesCapabilitiesData(collection, src);
            return collection;
        }
        String capabilitiesUrl = contructCapabilitiesUrl(src.getUrl(), src.getVersion());
        RawCapabilitiesResponse response = fetchCapabilities(capabilitiesUrl, src.getUser(), src.getPass(), getExpectedContentType(src.getVersion()));
        String validResponse = validateResponse(response, src.getVersion());
        LayerCapabilities singleLayer = parseSingleLayer(validResponse, src, featureType);
        singleLayer.setUrl(response.getUrl());
        // parser name == layer type
        singleLayer.setType(getName());
        return singleLayer;
    }

    public Map<String, LayerCapabilities> parseLayers(String xml) throws ServiceException {
        throw new ServiceException("Not implemented");
    }

    public LayerCapabilities parseSingleLayer(String response, ServiceConnectInfo src, String featureType) throws ServiceException {
        try {
            List<LayerCapabilitiesWFS> caps = WFSCapsParser.parseCapabilities(response);
            LayerCapabilitiesWFS layer = caps.stream().filter(c -> c.getName().equals(featureType)).findFirst().orElse(null);
            if (layer == null) {
                throw new ServiceException("Layer not found");
            }
            enhanceCapabilitiesData(layer, src);
            return layer;
        } catch (Exception e) {
            throw new ServiceException("Unable to parse layers for WFS capabilities", e);
        }
    }
    public Map<String, LayerCapabilities> parseLayers(String response, String version, ServiceConnectInfo src) throws ServiceException {
        try {
            List<LayerCapabilitiesWFS> caps;
            if (OGC_API_VERSION.equals(version)) {
                caps = getOGCAPIFeatures(response);
                // enhance with describe feature type data
                caps.forEach(c -> enhanceOGCAPIFeaturesCapabilitiesData(c, src));
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

    public String contructCapabilitiesUrl(String url, String version) {
        if (OGC_API_VERSION.equals(version)) {
            return OGCAPIFeaturesService.constructUrl(url);
        }
        return super.contructCapabilitiesUrl(url, version);
    }

    protected void enhanceCapabilitiesData(LayerCapabilitiesWFS layer, ServiceConnectInfo src) {
        try {
            String url = contructDescribeFeatureTypeUrl(src.getUrl(), layer.getVersion(), layer.getName());
            String xml = featureTypeProvider.getDescribeContent(url, src.getUser(), src.getPass());
            if (xml == null) {
                LOG.info("DescribeFeatureType response not available:", src.getUrl());
                return;
            }
            layer.setFeatureProperties(DescribeFeatureTypeParser.parseFeatureType(xml, layer.getName()));
        } catch (IOException e) {
            LOG.error("Unable to access wfs describe feature type: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to enhance wfs/parse feature type: " + e.getMessage());
        }
    }


    protected void enhanceOGCAPIFeaturesCapabilitiesData(LayerCapabilitiesWFS layer, ServiceConnectInfo src) {
        try {
            String geojson = ogcAPIFeaturesProvider.getItemsSample(src, layer.getName());
            if (geojson == null) {
                LOG.info("OGC API Features items not available:", src.getUrl());
                return;
            }
            layer.setFeatureProperties(ogcAPIFeaturesProvider.getFeatureProperties(geojson));
        } catch (Exception e) {
            LOG.error("Unable to enhance OGC API Features/parse feature type: " + e.getMessage());
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
