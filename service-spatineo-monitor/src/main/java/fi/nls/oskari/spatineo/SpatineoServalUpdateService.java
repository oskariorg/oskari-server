package fi.nls.oskari.spatineo;

import com.google.common.collect.Lists;
import fi.nls.oskari.db.ConnectionInfo;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spatineo.dao.OskariDao;
import fi.nls.oskari.spatineo.dao.SpatineoMonitoringDao;
import fi.nls.oskari.spatineo.dao.SpatineoServalDao;
import fi.nls.oskari.spatineo.dao.WmsServiceStatusDao;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.PorttiBackendStatusDto;
import fi.nls.oskari.spatineo.dto.SpatineoMonitoringResponseDto;
import fi.nls.oskari.spatineo.dto.SpatineoResponseDto;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.List;
import org.apache.commons.httpclient.HttpClient;

/**
 * The main entry point class for the Spatineo Serval data update service.
 */
public class SpatineoServalUpdateService {

    private static final Logger LOG = LogFactory.getLogger(SpatineoServalUpdateService.class);

// for debugging:
//    public static final String SERVAL_URL = "http://localhost:9179";
    public static final String SERVAL_URL = "https://monitor.spatineo.com/api/public/availability-1.0";
//    public static String MONITORING_URL = "https://beta.spatineo-devops.com/api/public/monitoringAPI";
    public static String MONITORING_URL = "https://monitor.spatineo.com/api/public/monitoringAPI";

    private static WmsServiceStatusDao serviceStatusDao;
    private static SpatineoServalDao spatineoDao;
    private static SpatineoMonitoringDao monitoringDao;
    
    /** Spatineo Serval maximum request batch size. */
    private static int CHUNK_SIZE = 25;
    private static boolean initialized;

    /**
     * Executed every 5 minutes or so by the Quartz scheduler, inside the servlet-map JVM.
     */
    public static void scheduledServiceCall() throws Exception {
        LOG.info("Starting the Spatineo Serval update service call...");

        // Spatineo Access Key
        PropertyUtil.loadProperties("/portal-ext.properties");
        String key = PropertyUtil.getNecessary("spatineo.monitoring.key", "Spatineo Monitoring API requires a private access key. Calls to API disabled.");
        if (key != null && !initialized) {
            MONITORING_URL += "?privateAccessKey=" + key;            
        } else if (!initialized) {
            // for development use, check also environment variables
            MONITORING_URL += "?privateAccessKey=" + System.getProperty("spatineo.monitoring.key");
        }
        initialized = true;
        
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        ConnectionInfo info  = helper.getPropsForDS(null);
        final BasicDataSource dataSource = createDatasource(info);
        
        try {
            final OskariDao oskariDao = new OskariDao(dataSource);
            serviceStatusDao = new WmsServiceStatusDao(dataSource);
            spatineoDao = new SpatineoServalDao(SERVAL_URL);
            monitoringDao = new SpatineoMonitoringDao(MONITORING_URL, new HttpClient(), true);
            serviceStatusDao.truncateStatusTable();
            List<List<OskariMapLayerDto>> ll = Lists.partition(oskariDao.findWmsMapLayerData(), CHUNK_SIZE);
            LOG.debug("Number of chunks: " + ll.size());
            for (final List<OskariMapLayerDto> layers : ll) {
                //LOG.debug("checking status for layers", layers);
                spatineoStatus(layers);
                Thread.sleep(5000);
            }
            if (key != null) {
                monitoringStatus(oskariDao.findWmsMapLayerData());
            }
        } finally {
            // clean up the created datasource
            dataSource.close();
        }
        LOG.info("Done with the Spatineo Serval update service call");
    }
    
    // the old implementation
    private static void spatineoStatus(final List<OskariMapLayerDto> layers) {
        final SpatineoResponseDto spatineoResponse = spatineoDao.checkServiceStatus(layers);
        LOG.debug("Spatineo response was", spatineoResponse);
        
        if (spatineoResponse != null) {            
            for (int i = 0; i < layers.size(); i++) {
                final OskariMapLayerDto layer = layers.get(i);
                final SpatineoResponseDto.Result result = spatineoResponse.result.get(i);
                if (null != layer && null != result) {
                    serviceStatusDao.insertStatus(new PorttiBackendStatusDto(layer.id, result.status, result.statusMessage, result.infoUrl, PorttiBackendStatusDto.SourceEnum.SPATINEO_SERVAL.toString()));
                } else {
                    LOG.error("PROBLEM with the Spatineo request for the layers:", layers);
                }
            }
        } else {
            LOG.warn("Spatineo serval response was null.");
        }
    }
    
    // the new implementation
    private static void monitoringStatus(final List<OskariMapLayerDto> layers) {
        final SpatineoMonitoringResponseDto dto = monitoringDao.checkServiceStatus();
        LOG.debug("Spatineo monitoring response was", dto);        
        
        if (dto != null) {            
            LOG.debug(dto.getLayerNames());
            
            if (dto.isError()) {
                LOG.warn("Spatineo Monitoring API returned error. Response handling skipped.");
                return;
            }
            
            for (OskariMapLayerDto l : layers) {
                String name = l.name;
                String url = l.url;
                SpatineoMonitoringResponseDto.Meter meter = dto.getMeterByLayerName(name);            
                LOG.debug("Matching <" + name + "> with <" + meter + ">");
                if (meter != null) {
                    String status = PorttiBackendStatusDto.StatusEnum.getEnumByNewAPI(meter.indicator.status).toString();
                    serviceStatusDao.insertStatus(new PorttiBackendStatusDto(l.id, status, meter.indicator.status, meter.monitorLink, PorttiBackendStatusDto.SourceEnum.SPATINEO_MONITORING.toString()));
                }
            }
        } else {
            LOG.warn("Spatineo Monitoring API status request failed");
        }
    }

    private static BasicDataSource createDatasource(ConnectionInfo info) {

        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(info.driver);
        dataSource.setUrl(info.url);
        dataSource.setUsername(info.user);
        dataSource.setPassword(info.pass);
        dataSource.setTimeBetweenEvictionRunsMillis(-1);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setValidationQueryTimeout(100);
        return dataSource;
    }

}
