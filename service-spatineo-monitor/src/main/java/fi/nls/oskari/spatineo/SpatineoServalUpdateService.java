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
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.List;
import org.apache.commons.httpclient.HttpClient;

/**
 * The main entry point class for the Spatineo Serval data update service.
 */
public class SpatineoServalUpdateService {

    private static final Logger LOG = LogFactory.getLogger(SpatineoServalUpdateService.class);

    public static final String SERVAL_URL = "https://monitor.spatineo.com/api/public/availability-1.0";
    public static final String MONITORING_URL = "https://monitor.spatineo.com/api/public/monitoringAPI";

    /** Spatineo Serval maximum request batch size. */
    private static int CHUNK_SIZE = 8;

    /**
     * Executed every 5 minutes or so by the Quartz scheduler, inside the servlet-map JVM.
     */
    public static void scheduledServiceCall() throws Exception {
        LOG.info("Starting the Spatineo Serval update service call...");
        final DatasourceHelper helper = DatasourceHelper.getInstance();

        ConnectionInfo info  = helper.getPropsForDS(null);

        final BasicDataSource dataSource = createDatasource(info);
        try {
            final OskariDao oskariDao = new OskariDao(dataSource);
            final WmsServiceStatusDao serviceStatusDao = new WmsServiceStatusDao(dataSource);
            final SpatineoServalDao spatineoDao = new SpatineoServalDao(SERVAL_URL);
            final SpatineoMonitoringDao monitoringDao = new SpatineoMonitoringDao(MONITORING_URL, new HttpClient());
            serviceStatusDao.truncateStatusTable();
            for (final List<OskariMapLayerDto> layers : Lists.partition(oskariDao.findWmsMapLayerData(), CHUNK_SIZE)) {
                LOG.debug("checking status for layers", layers);
                final SpatineoResponseDto spatineoResponse = spatineoDao.checkServiceStatus(layers);
                //final SpatineoMonitoringResponseDto monitoringResponse = monitoringDao.checkServiceStatus(layers);
                LOG.debug("Spatineo response was", spatineoResponse);
                //LOG.debug("Spatineo monitoring response was", monitoringResponse);
                
                // old system
                for (int i = 0; i < layers.size(); i++) {
                    final OskariMapLayerDto l = layers.get(i);
                    final SpatineoResponseDto.Result s = spatineoResponse.result.get(i);
                    if (null != l && null != s) {
                        serviceStatusDao.insertStatus(new PorttiBackendStatusDto(l.id, s.status, s.statusMessage, s.infoUrl));
                    } else {
                        LOG.error("PROBLEM with the Spatineo request for the layers:", layers);
                    }
                }
                
                // new system
                for (OskariMapLayerDto layer : layers) {
                    String name = layer.name;
                    String url = layer.url;
//                    SpatineoMonitoringResponseDto.Meter meter = monitoringResponse.getMeterByName(name, url);
//                    if (meter != null) {
//                        serviceStatusDao.insertStatus(new PorttiBackendStatusDto(layer.id, meter.indicator.status, "NO STATUS MESSAGE IN THE JSON", meter.monitorLink));
//                    }
                }
            }
        }
        finally {
            // clean up the created datasource
            dataSource.close();
        }
        LOG.info("Done with the Spatineo Serval update service call");
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
