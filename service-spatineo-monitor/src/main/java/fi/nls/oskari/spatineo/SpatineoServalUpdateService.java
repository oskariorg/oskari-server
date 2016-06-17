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

    public static final String SERVAL_URL = "https://monitor.spatineo.com/api/public/availability-1.0";
    public static String MONITORING_URL = "https://beta.spatineo-devops.com/api/public/monitoringAPI?privateAccessKey=15pN55SIjytRwZIuXG4DT75euT6FXrsXmsXg7LPOexkt0EYgtndew";

    private static WmsServiceStatusDao serviceStatusDao;
    private static SpatineoServalDao spatineoDao;
    private static SpatineoMonitoringDao monitoringDao;
    
    /** Spatineo Serval maximum request batch size. */
    private static int CHUNK_SIZE = 8;

    /**
     * Executed every 5 minutes or so by the Quartz scheduler, inside the servlet-map JVM.
     */
    public static void scheduledServiceCall() throws Exception {
        LOG.info("Starting the Spatineo Serval update service call...");

        // Spatineo Access Key
        PropertyUtil.loadProperties("/oskari-ext.properties");
        String key = PropertyUtil.getNecessary("spatineo.monitoring.url", "Spatineo Monitoring API requires an access key. Calls to API disabled");
        if (key != null) {
            MONITORING_URL += "?privateAccessKey=" + key;
        }
        
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        ConnectionInfo info  = helper.getPropsForDS(null);
        final BasicDataSource dataSource = createDatasource(info);
        
        try {
            final OskariDao oskariDao = new OskariDao(dataSource);
            serviceStatusDao = new WmsServiceStatusDao(dataSource);
            spatineoDao = new SpatineoServalDao(SERVAL_URL);
            monitoringDao = new SpatineoMonitoringDao(MONITORING_URL, new HttpClient(), false);
            serviceStatusDao.truncateStatusTable();
            for (final List<OskariMapLayerDto> layers : Lists.partition(oskariDao.findWmsMapLayerData(), CHUNK_SIZE)) {
                LOG.debug("checking status for layers", layers);
                spatineoStatus(layers);
                monitoringStatus(layers);
            }
        }
        finally {
            // clean up the created datasource
            dataSource.close();
        }
        LOG.info("Done with the Spatineo Serval update service call");
    }
    
    private static void spatineoStatus(final List<OskariMapLayerDto> layers) {
        final SpatineoResponseDto spatineoResponse = spatineoDao.checkServiceStatus(layers);
        LOG.debug("Spatineo response was", spatineoResponse);

        for (int i = 0; i < layers.size(); i++) {
            final OskariMapLayerDto l = layers.get(i);
            final SpatineoResponseDto.Result s = spatineoResponse.result.get(i);
            if (null != l && null != s) {
                serviceStatusDao.insertStatus(new PorttiBackendStatusDto(l.id, s.status, s.statusMessage, s.infoUrl, PorttiBackendStatusDto.SourceEnum.SPATINEO_SERVAL.toString()));
            } else {
                LOG.error("PROBLEM with the Spatineo request for the layers:", layers);
            }
        }
    }
    
    private static void monitoringStatus(final List<OskariMapLayerDto> layers) {
        final SpatineoMonitoringResponseDto monitoringResponse = monitoringDao.checkServiceStatus();
        LOG.debug("Spatineo monitoring response was", monitoringResponse);
        for (OskariMapLayerDto layer : layers) {
            String name = layer.name;
            String url = layer.url;
            SpatineoMonitoringResponseDto.Meter meter = monitoringResponse.getMeterByName(name);
            if (meter != null) {
//                serviceStatusDao.insertStatus(new PorttiBackendStatusDto(layer.id, meter.indicator.status, "NO STATUS MESSAGE IN THE JSON", meter.monitorLink, PorttiBackendStatusDto.SourceEnum.SPATINEO_MONITORING.toString()));
            }
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
