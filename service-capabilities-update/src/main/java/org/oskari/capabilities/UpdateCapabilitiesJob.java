package org.oskari.capabilities;

import static java.util.stream.Collectors.groupingBy;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.TileMatrixLink;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import fi.nls.oskari.worker.ScheduledJob;

/**
 * ScheludedJob that updates Capabilities of WMS and WMTS layers
 * <ul>
 * <li>Updates oskari_capabilities_cache rows</li>
 * <li>Updates OskariLayer objects via #setCapabilities()</li>
 * </ul>
 * Available configuration:
 * <ul>
 * <li>maxAge, skip layer if its' oskari_capabilities_cache.updated is newer than (NOW() - maxAge)</li>
 * </ul>
 */
@Oskari("UpdateCapabilities")
public class UpdateCapabilitiesJob extends ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);
    private static final String PROP_MAX_AGE = "oskari.scheduler.job.UpdateCapabilities.maxAge";

    private final OskariLayerService layerService;
    private final CapabilitiesCacheService capabilitiesCacheService;
    private final int maxAge;

    public UpdateCapabilitiesJob() {
        this(new OskariLayerServiceIbatisImpl(),
                new CapabilitiesCacheServiceMybatisImpl(),
                PropertyUtil.getOptional(PROP_MAX_AGE, 0));
    }

    public UpdateCapabilitiesJob(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesService, int maxAge) {
        this.layerService = layerService;
        this.capabilitiesCacheService = capabilitiesService;
        this.maxAge = maxAge;
    }

    @Override
    public void execute(Map<String, Object> params) {
        Map<UrlTypeVersion, List<OskariLayer>> layersByUrlTypeVersion = layerService.findAll()
                .stream()
                .filter(l -> canUpdate(l.getType()))
                .collect(groupingBy(l -> new UrlTypeVersion(l)));

        long now = System.currentTimeMillis();
        Timestamp oldestAllowed = maxAge > 0L ? new Timestamp(now - maxAge) : null;
        for (Map.Entry<UrlTypeVersion, List<OskariLayer>> group : layersByUrlTypeVersion.entrySet()) {
            UrlTypeVersion utv = group.getKey();
            List<OskariLayer> layers = group.getValue();
            updateCapabilitiesGroup(utv, layers, oldestAllowed);
        }
    }

    protected static boolean canUpdate(String type) {
        switch (type) {
        case OskariLayer.TYPE_WMS:
        case OskariLayer.TYPE_WMTS:
            return true;
        default:
            return false;
        }
    }

    protected void updateCapabilitiesGroup(UrlTypeVersion utv,
            List<OskariLayer> layers, Timestamp oldestAllowed) {
        String url = utv.getUrl();
        String type = utv.getType();
        String version = utv.getVersion();
        
        int[] ids = layers.stream().mapToInt(l -> l.getId()).toArray();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating Capabilities for a group of layers - url:", url,
                    "type:", type, "version:", version, "ids:", Arrays.toString(ids));
        }

        String data = getFromCache(url, type, version, oldestAllowed);
        if (data == null || data.isEmpty()) {
            /*
             * If cache didn't have it
             * or it was too old
             * or cached value happened to be empty for whatever reason
             * -> try to get it straight from the service
             */
            if (containsDifferentUsernames(layers)) {
                LOG.warn("OH NOES!");
            }
            data = getFromService(url, type, version,
                    layers.get(0).getUsername(), layers.get(0).getPassword());
            if (data == null || data.isEmpty()) {
                return;
            }
        }

        switch (type) {
        case OskariLayer.TYPE_WMS:
            // TODO: implement
            break;
        case OskariLayer.TYPE_WMTS:
            try {
                WMTSCapabilities wmts = WMTSCapabilitiesParser.parseCapabilities(data);
                capabilitiesCacheService.save(new OskariLayerCapabilities(url, type, version, data));
                for (OskariLayer layer : layers) {
                    if (updateWMTS(wmts, layer)) {
                        layerService.update(layer);
                    }
                }
            } catch (XMLStreamException | IllegalArgumentException e) {
                LOG.warn(e, "Failed to parse WMTS GetCapabilities - url:", url,
                        "type:", type, "version:", version);
            }
            break;
            // Add more cases
        }

    }

    private boolean containsDifferentUsernames(List<OskariLayer> layers) {
        String old = layers.get(0).getUsername();
        for (int i = 1; i < layers.size(); i++) {
            String username = layers.get(i).getUsername();
            if (old == null) {
                if (username != null) {
                    return true;
                }
            } else {
                if (!old.equals(username)) {
                    return true;
                }
            }

        }
        return false;
    }

    protected String getFromCache(String url, String type, String version, Timestamp oldestAllowed) {
        if (oldestAllowed != null) {
            OskariLayerCapabilities cached = capabilitiesCacheService.find(url, type, version);
            // Check if we actually found data from DB and if it's recently updated
            if (cached != null) {
                if (cached.getUpdated().after(oldestAllowed)) {
                    return cached.getData();
                }
                LOG.warn("Found data from cache, but it was too old - url:", url,
                        "type:", type, "version:", version);
            }
        }
        return null;
    }

    protected String getFromService(String url, String type, String version, String user, String pass) {
        try {
            return CapabilitiesCacheService.loadCapabilitiesFromService(url, type, version, user, pass);
        } catch (ServiceException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                LOG.warn(e, "Failed to get GetCapabilities - url:", url,
                        "type:", type, "version:", version, "cause:", cause.getMessage());
            } else {
                LOG.warn(e, "Failed to get GetCapabilities - url:", url,
                        "type:", type, "version:", version);
            }
            return null;
        }
    }

    private boolean updateWMTS(WMTSCapabilities wmts, OskariLayer layer) {
        int id = layer.getId();
        String name = layer.getName();

        WMTSCapabilitiesLayer layerCaps = wmts.getLayer(name);
        if (layerCaps == null) {
            // TODO: Add notification via admin notification service (once such service is built)
            LOG.warn("Couldn't find layer from Capabilities! Layer id:", id, "name:", name);
            return false;
        }

        ResourceUrl resUrl = layerCaps.getResourceUrlByType("tile");
        if (resUrl == null) {
            // TODO: Add notification via admin notification service (once such service is built)
            LOG.warn("Couldn't find ResourceUrl of type 'tile' from GetCapabilities"
                    + " layer id:", id, "name:", name);
            return false;
        }

        JSONObject options = layer.getOptions();
        JSONHelper.putValue(options, "requestEncoding", "REST");
        JSONHelper.putValue(options, "format", resUrl.getFormat());
        JSONHelper.putValue(options, "urlTemplate", resUrl.getTemplate());

        JSONArray epsgToTileMatrixSet = new JSONArray();
        Set<String> supportedCrs = new HashSet<String>();

        for (TileMatrixLink link : layerCaps.getLinks()) {
            TileMatrixSet tms = link.getTileMatrixSet();
            String identifier = tms.getId();
            String crs = tms.getCrs();
            String epsg = ProjectionHelper.shortSyntaxEpsg(crs);
            epsgToTileMatrixSet.put(JSONHelper.createJSONObject(epsg, identifier));
            supportedCrs.add(epsg);
        }

        JSONObject capabilitiesJSON = new JSONObject();
        if (epsgToTileMatrixSet.length() > 0) {
            JSONHelper.put(capabilitiesJSON,
                    LayerJSONFormatterWMTS.KEY_TILEMATRIXIDS, epsgToTileMatrixSet);
        }
        layer.setCapabilities(capabilitiesJSON);
        layer.setSupportedCRSs(supportedCrs);

        return true;
    }

    static class UrlTypeVersion {

        private final String url;
        private final String type;
        private final String version;

        public UrlTypeVersion(OskariLayer layer) {
            this.url = layer.getSimplifiedUrl(true);
            this.type = layer.getType();
            this.version = layer.getVersion();
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof UrlTypeVersion)) {
                return false;
            }
            UrlTypeVersion s = (UrlTypeVersion) o;
            return url.equals(s.url)
                    && type.equals(s.type)
                    && version.equals(s.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, type, version);
        }

    }

}
