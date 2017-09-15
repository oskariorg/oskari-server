package org.oskari.capabilities;

import fi.nls.oskari.util.PropertyUtil;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.worker.ScheduledJob;
import java.sql.Timestamp;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

/**
 * ScheludedJob that updates Capabilities of WMS/WMTS layers
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
    private static final String PROP_MAX_AGE = "oskari.scheduler.job.UpdateCapabilitiesJob.maxAge";
    
    private final OskariLayerService layerService;
    private final CapabilitiesCacheService capabilitiesService;
    private final int maxAge;

    public UpdateCapabilitiesJob() {
        this(new OskariLayerServiceIbatisImpl(), 
                new CapabilitiesCacheServiceMybatisImpl(),
                PropertyUtil.getOptional(PROP_MAX_AGE, 0));
    }

    public UpdateCapabilitiesJob(OskariLayerService layerService,
            CapabilitiesCacheService capabilitiesService, int maxAge) {
        this.layerService = layerService;
        this.capabilitiesService = capabilitiesService;
        this.maxAge = maxAge;
    }

    @Override
    public void execute(Map<String, Object> params) {
        final long now = System.currentTimeMillis();
        final Timestamp oldestAllowed = maxAge > 0L ? new Timestamp(now - maxAge) : null;
        for (OskariLayer layer : layerService.findAll()) {
            updateCapabilities(layer, oldestAllowed);
        }
    }

    protected void updateCapabilities(OskariLayer layer, Timestamp oldestAllowed) {
        if (layer == null) {
            return;
        }

        String type = layer.getType();
        if (!OskariLayer.TYPE_WMS.equals(type) && !OskariLayer.TYPE_WMTS.equals(type)) {
            LOG.debug("Can't update capabilities of type:", layer.getType(),
                    "layer id:", layer.getId(), "skipping!");
            return;
        }

        String url = layer.getSimplifiedUrl(true);
        String version = layer.getVersion();

        String getCapabilitiesXML = null;
        if (oldestAllowed != null) {
            OskariLayerCapabilities cached = capabilitiesService.find(url, type, version);
            // Check if we actually found data from DB and if it's recently updated
            if (cached != null && cached.getUpdated().after(oldestAllowed)) {
                getCapabilitiesXML = cached.getData();
            }
        }

        if (getCapabilitiesXML == null || getCapabilitiesXML.isEmpty()) {
            try {
                /*
                 * If cache didn't have it
                 * or it was too old
                 * or cached value happened to be empty for whatever reason
                 * -> try to get it straight from the service
                 */
                getCapabilitiesXML = CapabilitiesCacheService.loadCapabilitiesFromService(layer);
            } catch (ServiceException e) {
                LOG.warn(e, "Failed to GetCapabilities for layer id:", layer.getId());
                return;
            }
        }

        boolean success;
        if (OskariLayer.TYPE_WMS.equals(type)) {
            success = updateWMS(getCapabilitiesXML, layer);
        } else { // OskariLayer.TYPE_WMTS.equals(type)
            success = updateWMTS(getCapabilitiesXML, layer);
        }

        /*
         * If something went wrong with the update method we don't want to
         * update the capabilities with that data
         */
        if (success) {
            OskariLayerCapabilities draft = new OskariLayerCapabilities(
                    url, type, version, getCapabilitiesXML);
            capabilitiesService.save(draft);
            layerService.update(layer);
        }
    }

    /**
     * @return true if we think the Capabilities document was valid and usable, false if not
     */
    private boolean updateWMTS(String getCapabilitiesXML, OskariLayer layer) {
        try {
            WMTSCapabilities wmts = WMTSCapabilitiesParser.parseCapabilities(getCapabilitiesXML);
            try { // Don't catch any NPEs that might occur within previous method call
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(wmts, layer, null);
            } catch (NullPointerException ignore) {
                /*
                 * TODO: Figure out what would be a better way to handle this
                 *       We don't want to bubble the exception
                 *       The capabilities was a valid response, it just didn't
                 *       contain something we hoped to find
                 */
            }
            return true;
        } catch (XMLStreamException | IllegalArgumentException e) {
            LOG.warn(e, "Failed to parse WMTS GetCapabilities for layer id:", layer.getId());
        }
        return false;
    }

    /**
     * @return true if we think the Capabilities document was valid and usable, false if not
     */
    private boolean updateWMS(String getCapabilitiesXML, OskariLayer layer) {
        WebMapService wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(getCapabilitiesXML, layer);
        if (wms == null) {
            LOG.warn("Failed to parse WMS GetCapabilities for layer id:", layer.getId());
            return false;
        }

        OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wms, layer);
        return true;
    }

}
