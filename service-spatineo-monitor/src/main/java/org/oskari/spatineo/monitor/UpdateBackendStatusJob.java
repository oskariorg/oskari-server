package org.oskari.spatineo.monitor;

import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.service.backendstatus.BackendStatusCode;
import org.oskari.service.backendstatus.BackendStatusService;
import org.oskari.service.backendstatus.BackendStatusServiceMyBatisImpl;
import org.oskari.service.backendstatus.maplayer.MapLayer;
import org.oskari.service.backendstatus.maplayer.MapLayerDao;
import org.oskari.spatineo.monitor.api.SpatineoMonitorDao;
import org.oskari.spatineo.monitor.api.model.*;

import java.util.ArrayList;
import java.util.List;

public class UpdateBackendStatusJob {
    
    private static final Logger LOG = LogFactory.getLogger(UpdateBackendStatusJob.class);

    private static final String PROP_MONITORING_URL = "spatineo.monitoring.url";
    private static final String PROP_MONITORING_KEY = "spatineo.monitoring.key";

    public static void scheduledServiceCall() {
        LOG.info("Starting the Spatineo Monitor update service call...");

        final String endPoint = PropertyUtil.getNecessary(PROP_MONITORING_URL,
                "Spatineo Monitoring API requires a end point address. Calls to API disabled.");
        final String key = PropertyUtil.getNecessary(PROP_MONITORING_KEY,
                "Spatineo Monitoring API requires a private access key. Calls to API disabled.");
        final SpatineoMonitorDao spatineoMonitorDao = new SpatineoMonitorDao(endPoint, key);

        final MapLayerDao mapLayerDao = new MapLayerDao();
        final BackendStatusService statusService = new BackendStatusServiceMyBatisImpl();

        final Response response = spatineoMonitorDao.query();
        if (response == null) {
            LOG.debug("Did not receive any response from Spatineo Monitor");
            return;
        }

        if (!Response.STATUS_OK.equals(response.getStatus())) {
            LOG.info("Received Response with status: ", response.getStatus(), 
                    " statusMessage: ", response.getStatusMessage());
            return;
        }

        final List<BackendStatus> statuses = new ArrayList<>();
        for (MapLayer layer : mapLayerDao.findWMSMapLayers()) {
            BackendStatus status = getStatus(layer, response.getResult(), false);
            if (status != null) {
                statuses.add(status);
            }
        }
        for (MapLayer layer : mapLayerDao.findWFSMapLayers()) {
            BackendStatus status = getStatus(layer, response.getResult(), true);
            if (status != null) {
                statuses.add(status);
            }
        }
        statusService.insertAll(statuses);

        LOG.info("Done with the Spatineo Monitor update service call");
    }
    
    private static BackendStatus getStatus(MapLayer layer, List<Result> results, boolean isWFS) {
        Meter meter = findMeter(results, layer.getName(), layer.getUrl(), isWFS);
        if (meter == null) {
            LOG.info("Could not find meter for layer: ", layer.getName());
            return null;
        }
        return getStatus(layer.getId(), meter);
    }

    private static Meter findMeter(List<Result> results, String name, String url, boolean isWFS) {
        final String type = isWFS ? Service.TYPE_WFS : Service.TYPE_WMS;
        for (Result r : results) {
            Service s = r.getService();
            if (s == null 
                    || !type.equals(s.getServiceType()) 
                    || !url.equalsIgnoreCase(s.getServiceUrl())) {
                // Wrong service
                continue;
            }
            for (Meter m : s.getMeters()) {
                String layerName = getLayerNameToCompare(m.getLayerName(), isWFS);
                if (name.equals(layerName)) {
                    return m;
                }
            }
        }
        return null;
    }
    
    private static String getLayerNameToCompare(String layerName, boolean isWFS) {
        // layerName may consist of two parts, separated by a ':', e.g. layerName:layerTarget
        // For WFS layers we are interested in layerTarget
        // For other layers the layerName part
        final int i = layerName.indexOf(':');
        if (isWFS) {
            return i < 0 ? null : layerName.substring(i + 1);
        }
        return i < 0 ? layerName : layerName.substring(0, i);
    }

    private static BackendStatus getStatus(int mapLayerId, Meter meter) {
        Indicator indicator = meter.getIndicator();
        String statusMessage = indicator.getStatus();
        BackendStatusCode status = getStatusFromMessage(statusMessage);
        String monitorLink = meter.getMonitorLink();
        return new BackendStatus(mapLayerId, status.toString(), statusMessage, monitorLink);
    }

    private static BackendStatusCode getStatusFromMessage(String statusMessage) {
        switch (statusMessage) {
        case "NO_INDICATOR":
            return BackendStatusCode.UNKNOWN;
        case "NO_ALERTS":
            return BackendStatusCode.UNKNOWN;
        case "NEW":
            return BackendStatusCode.OK;
        case "OK":
            return BackendStatusCode.OK;
        case "WARNING":
            return BackendStatusCode.UNSTABLE;
        case "ALERT":
            return BackendStatusCode.DOWN;
        case "INSUFFICIENT_DATA":
            return BackendStatusCode.UNKNOWN;
        default:
            return BackendStatusCode.UNKNOWN;
        }
    }

}
