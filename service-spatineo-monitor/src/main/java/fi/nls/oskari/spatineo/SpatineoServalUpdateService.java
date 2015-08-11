package fi.nls.oskari.spatineo;

import com.google.common.collect.Lists;
import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spatineo.dao.OskariDao;
import fi.nls.oskari.spatineo.dao.SpatineoServalDao;
import fi.nls.oskari.spatineo.dao.WmsServiceStatusDao;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.PorttiBackendStatusDto;
import fi.nls.oskari.spatineo.dto.SpatineoResponseDto;

import javax.sql.DataSource;
import java.util.List;

/**
 * The main entry point class for the Spatineo Serval data update service.
 */
public class SpatineoServalUpdateService {

    private static final Logger log = LogFactory.getLogger(SpatineoServalUpdateService.class);

    public static final String SERVAL_URL = "https://monitor.spatineo.com/api/public/availability-1.0";

    /** Spatineo Serval maximum request batch size. */
    private static int CHUNK_SIZE = 8;

    /**
     * Executed every 5 minutes or so by the Quartz scheduler, inside the servlet-map JVM.
     */
    public static void scheduledServiceCall() throws Exception {
        log.info("Starting the Spatineo Serval update service call...");
        final DatasourceHelper helper = new DatasourceHelper();

        final DataSource dataSource = helper.getDataSource();
        final OskariDao oskariDao = new OskariDao(dataSource);
        final WmsServiceStatusDao serviceStatusDao = new WmsServiceStatusDao(dataSource);
        final SpatineoServalDao spatineoDao = new SpatineoServalDao(SERVAL_URL);
        serviceStatusDao.truncateStatusTable();
        for (final List<OskariMapLayerDto> layers : Lists.partition(oskariDao.findWmsMapLayerData(), CHUNK_SIZE)) {
            log.debug("checking status for layers", layers);
            final SpatineoResponseDto spatineoResponse = spatineoDao.checkServiceStatus(layers);
            log.debug("Spatineo response was", spatineoResponse);
            for (int i = 0; i < layers.size(); i++) {
                final OskariMapLayerDto l = layers.get(i);
                final SpatineoResponseDto.Result s = spatineoResponse.result.get(i);
                if (null != l && null != s) {
                    serviceStatusDao.insertStatus(new PorttiBackendStatusDto(l.id, s.status, s.statusMessage, s.infoUrl));
                } else {
                    log.error("PROBLEM with the Spatineo request for the layers:", layers);
                }
            }
        }
        log.info("Done with the Spatineo Serval update service call");
    }

}
