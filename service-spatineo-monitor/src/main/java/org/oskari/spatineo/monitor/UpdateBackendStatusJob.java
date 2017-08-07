package org.oskari.spatineo.monitor;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.oskari.spatineo.monitor.api.SpatineoMonitorDao;
import org.oskari.spatineo.monitor.api.model.Indicator;
import org.oskari.spatineo.monitor.api.model.Meter;
import org.oskari.spatineo.monitor.api.model.Response;
import org.oskari.spatineo.monitor.api.model.Result;
import org.oskari.spatineo.monitor.api.model.Service;
import org.oskari.spatineo.monitor.backendstatus.BackendStatus;
import org.oskari.spatineo.monitor.backendstatus.BackendStatusDao;
import org.oskari.spatineo.monitor.backendstatus.BackendStatusMapper;
import org.oskari.spatineo.monitor.backendstatus.Status;
import org.oskari.spatineo.monitor.maplayer.MapLayer;
import org.oskari.spatineo.monitor.maplayer.MapLayerDao;
import org.oskari.spatineo.monitor.maplayer.MapLayerMapper;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

public class UpdateBackendStatusJob {
    
    private static final String PROP_MONITORING_URL = "spatineo.monitoring.url";
    private static final String PROP_MONITORING_KEY = "spatineo.monitoring.key";

    private static final Logger LOG;
    
    static {
        PropertyUtil.loadProperties("/oskari.properties");
        PropertyUtil.loadProperties("/portal-ext.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
        LOG = LogFactory.getLogger(UpdateBackendStatusJob.class);
    }

    private static SpatineoMonitorDao spatineoMonitorDao;
    private static MapLayerDao mapLayerDao;
    private static BackendStatusDao serviceStatusDao;

    private static boolean initd = false;

    private static void init() {
        String url = PropertyUtil.getNecessary(PROP_MONITORING_URL, 
                "Spatineo Monitoring API requires a end point address. Calls to API disabled.");
        String key = PropertyUtil.getNecessary(PROP_MONITORING_KEY, 
                "Spatineo Monitoring API requires a private access key. Calls to API disabled.");

        final SqlSessionFactory factory = initMyBatis();

        spatineoMonitorDao = new SpatineoMonitorDao(url, key);
        mapLayerDao = new MapLayerDao(factory);
        serviceStatusDao = new BackendStatusDao(factory);

        LOG.info("Spatineo Monitor init completed!");
        initd = true;
    }

    private static SqlSessionFactory initMyBatis() {
        final DataSource ds = DatasourceHelper.getInstance().getDataSource();
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, ds);
        final Configuration configuration = new Configuration(environment);
        configuration.addMapper(MapLayerMapper.class);
        configuration.addMapper(BackendStatusMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public static void scheduledServiceCall() {
        if (!initd) {
            init();
            if (!initd) {
                LOG.info(UpdateBackendStatusJob.class.getName() + " init failed");
                return;
            }
        }

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
        serviceStatusDao.insertStatuses(statuses);

        LOG.info("Done with the Spatineo Serval update service call");
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

    private static BackendStatus getStatus(long mapLayerId, Meter meter) {
        Indicator indicator = meter.getIndicator();
        String statusMessage = indicator.getStatus();
        Status status = Status.getEnumByNewAPI(statusMessage);
        String monitorLink = meter.getMonitorLink();
        return new BackendStatus(mapLayerId, status.toString(), statusMessage, monitorLink);
    }

}
