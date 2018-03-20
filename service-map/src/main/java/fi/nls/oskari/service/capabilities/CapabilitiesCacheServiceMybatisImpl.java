package fi.nls.oskari.service.capabilities;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

@Oskari
public class CapabilitiesCacheServiceMybatisImpl extends CapabilitiesCacheService {

    private static final Logger LOG = LogFactory.getLogger(CapabilitiesCacheServiceMybatisImpl.class);

    private final SqlSessionFactory factory;

    public CapabilitiesCacheServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public CapabilitiesCacheServiceMybatisImpl(DataSource ds) {
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = MyBatisHelper.initMyBatis(ds, CapabilitiesMapper.class);
        }
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
        try (final SqlSession session = factory.openSession()) {
            return getMapper(session).findByUrlTypeVersion(url, layertype, version);
        }
    }

    /**
     * Inserts or updates a capabilities XML in database
     * The rows in the table are UNIQUE (url, layertype, version)
     */
    public OskariLayerCapabilities save(final OskariLayerCapabilities draft) {
        try (final SqlSession session = factory.openSession(false)) {
            final CapabilitiesMapper mapper = getMapper(session);

            String url = draft.getUrl();
            String type = draft.getLayertype();
            String version = draft.getVersion();

            // Check if a row already exists
            Long id = mapper.selectIdByUrlTypeVersion(url, type, version);
            if (id != null) {
                // Update
                mapper.updateData(id, draft.getData());
                OskariLayerCapabilities updated = mapper.findById(id);
                LOG.info("Updated capabilities:", updated);
                session.commit();
                return updated;
            } else {
                // Insert
                mapper.insert(draft);
                OskariLayerCapabilities inserted = mapper.findByUrlTypeVersion(url, type, version);
                LOG.info("Inserted capabilities:", inserted);
                session.commit();
                return inserted;
            }
        }
    }

}
