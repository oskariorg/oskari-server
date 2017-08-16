package fi.nls.oskari.service.capabilities;

import java.sql.Timestamp;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;

@Oskari
public class CapabilitiesCacheServiceMybatisImpl extends CapabilitiesCacheService {

    private static final Logger LOG = LogFactory.getLogger(CapabilitiesCacheServiceMybatisImpl.class);

    private final SqlSessionFactory factory;

    public CapabilitiesCacheServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public CapabilitiesCacheServiceMybatisImpl(DataSource ds) {
        this.factory = MyBatisHelper.initMyBatis(ds, CapabilitiesMapper.class);
    }

    private CapabilitiesMapper getMapper(SqlSession session) {
        return session.getMapper(CapabilitiesMapper.class);
    }

    /**
     * Tries to load capabilities from the database
     * @return null if not saved to db
     * @throws IllegalArgumentException if url or layertype is null or empty
     */
    public OskariLayerCapabilities find(String url, String layertype, String version)
            throws IllegalArgumentException {
        if (url == null || url.isEmpty()) {
            LOG.warn("Missing required parameter url");
            throw new IllegalArgumentException("Missing required parameter url");
        }
        if (layertype == null || layertype.isEmpty()) {
            LOG.warn("Layertype can not be null or empty");
            throw new IllegalArgumentException("Layertype can not be null or empty");
        }

        try (final SqlSession session = factory.openSession()) {
            return getMapper(session).findByUrlTypeVersion(url.toLowerCase(),
                    layertype.toLowerCase(),
                    version != null ? version.toLowerCase() : null);
        }
    }

    /**
     * Inserts or updates a capabilities XML in database
     * The rows in the table are UNIQUE (layertype, version, url)
     */
    public OskariLayerCapabilities save(final OskariLayerCapabilitiesDraft draft) {
        try (final SqlSession session = factory.openSession()) {
            final CapabilitiesMapper mapper = getMapper(session);

            // Check if a row already exists
            OskariLayerCapabilities db = mapper.findByUrlTypeVersion(draft.getUrl(),
                    draft.getLayertype(),
                    draft.getVersion());
            if (db != null) {
                if (db.hasData() && !draft.hasData()) {
                    LOG.info("Trying to write empty capabilities on top of existing ones, not saving!");
                    return db;
                }
                // Update
                Timestamp updatedTs = mapper.updateData(db.getId(), draft.getData());
                session.commit();
                OskariLayerCapabilities updated = new OskariLayerCapabilities(db.getId(),
                        db.getUrl(), db.getLayertype(), db.getVersion(),
                        draft.getData(), db.getCreated(), updatedTs);
                LOG.info("Updated capabilities:", updated);
                return updated;
            }

            // Insert
            final OskariLayerCapabilitiesInsertInfo info = mapper.insert(draft);
            session.commit();
            OskariLayerCapabilities inserted = new OskariLayerCapabilities(info.id,
                    draft.getUrl(), draft.getLayertype(), draft.getVersion(),
                    draft.getData(), info.created, info.updated);
            LOG.info("Inserted capabilities:", inserted);
            return inserted;
        }
    }

}
