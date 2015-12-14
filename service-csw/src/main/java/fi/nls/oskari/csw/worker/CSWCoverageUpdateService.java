package fi.nls.oskari.csw.worker;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.csw.dao.OskariLayerMetadataDao;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.dto.OskariLayerMetadataDto;
import fi.nls.oskari.csw.service.CSWService;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.worker.ScheduledJob;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Scheduled job for retrieving coverage data for maplayers having metadataids.
 */
@Oskari("CSWCoverageImport")
public class CSWCoverageUpdateService extends ScheduledJob {
    private static final Logger log = LogFactory.getLogger(CSWCoverageUpdateService.class);

    final OskariLayerService layerService = new OskariLayerServiceIbatisImpl();
    final private static String PROPERTY_BASE_URL = "service.metadata.url";

    @Override
    public void execute(Map<String, Object> params) {
        log.info("Starting the CSW coverage update service call...");

        final String baseUrl = PropertyUtil.getOptional(PROPERTY_BASE_URL);
        if(baseUrl == null) {
            log.warn("Trying to get metadata for layers but property", PROPERTY_BASE_URL, "is missing - quitting!");
            return;
        }
        final CSWService cswService = getCSWService(baseUrl);
        if(cswService == null) {
            log.warn("Couldn't get a usable CSW service - quitting!");
            // no reason to go forward since we can't contact the service
            return;
        }

        final Set<String> metadataIdSet = getMetadataIdSet();
        if(metadataIdSet.isEmpty()) {
            log.info("No layers with metadata id - quitting!");
            return;
        }
        final DataSource dataSource = getDatasource();
        if(dataSource == null) {
            log.error("Couldn't get datasource - quitting!");
            return;
        }
        final OskariLayerMetadataDao dao = new OskariLayerMetadataDao(dataSource);
        for (String metadataId : metadataIdSet) {
            final CSWIsoRecord csw = getMetadata(cswService, metadataId);
            final Geometry geom = getGeometry(csw);
            if(geom == null) {
                // no geometry on metadata, move to next
                log.info("Couldn't get geometry for", metadataId);
                continue;
            }
            final OskariLayerMetadataDto dto = new OskariLayerMetadataDto();
            dto.metadataId  = metadataId;
            // NOTE! wkt is WGS:84
            dto.wkt = geom.getEnvelope().toText();
            dto.json = csw.toJSON().toString();
            dao.saveMetadata(dto);
        }
        log.info("Done with the CSW coverage update service call");
    }

    private DataSource getDatasource() {
        try {
            return DatasourceHelper.getInstance().getDataSource();
        }
        catch (Exception ex) {
            log.error(ex, "Couldn't get datasource");
        }
        return null;
    }

    /**
     * Returns a set of metadata ids which should be updated
     * @return
     */
    public Set<String> getMetadataIdSet() {
        final Set<String> result = new HashSet<String>();
        // TODO: make it a query in DB rather than looping here for distinct values
        for(OskariLayer layer : layerService.findAll()) {
            final String uuid = layer.getMetadataId();
            if(uuid != null && !uuid.trim().isEmpty()) {
                result.add(layer.getMetadataId());
            }
        }
        return result;
    }


    private CSWIsoRecord getMetadata(final CSWService cswService, final String metadataId) {
        final String language = PropertyUtil.getDefaultLanguage();
        try {
            return cswService.getRecordById(metadataId, language);
        } catch (Exception e) {
            log.error(e, "Error fetching metadata for id:", metadataId);
        }
        return null;
    }

    /**
     * Parses the geometry from CSW record. Note! Only returns the first one!
     * @param csw
     * @return
     */
    private Geometry getGeometry(CSWIsoRecord csw) {
        if(csw == null) {
            return null;
        }
        if(!csw.getIdentifications().isEmpty()) {
            return csw.getIdentifications().get(0).getExtents();
        }
        return null;
    }

    public CSWService getCSWService(final String baseUrl) {
        try {
            return new CSWService(baseUrl);
        } catch (Exception e) {
            log.error("Failed to initialize CSWService:" + e.getMessage());
        }
        return null;
    }
}