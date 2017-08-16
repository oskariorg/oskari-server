package fi.nls.oskari.service.capabilities;

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

        url = url.toLowerCase();
        layertype = layertype.toLowerCase();
        if (version != null) {
            version = version.toLowerCase();
        }

        try (final SqlSession session = factory.openSession()) {
            return getMapper(session).findByUrlTypeVersion(url, layertype, version);
        }
    }

    /**
     * Inserts or updates a capabilities XML in database
     */
    public OskariLayerCapabilities save(final OskariLayerCapabilities capabilities) {
        try (final SqlSession session = factory.openSession()) {
            final CapabilitiesMapper mapper = getMapper(session);
            final OskariLayerCapabilities db = mapper.findByUrlTypeVersion(capabilities.getUrl(),
                    capabilities.getLayertype(),
                    capabilities.getVersion());

            long id;
            if (db != null) {
                if (db.hasData() && !capabilities.hasData()) {
                    LOG.info("Trying to write empty capabilities on top of existing ones, not saving!");
                    return db;
                }
                // Update
                id = db.getId();
                mapper.updateData(id, capabilities.getData());
            } else {
                // Insert
                id = mapper.insert(capabilities);
            }

            session.commit();

            // After we've updated or inserted the row read it from the database
            // to get correct created and updated column values
            final OskariLayerCapabilities saved = mapper.findById(id);
            LOG.debug("Saved capabilities with id: ", saved.getId());
            return saved;
        }
    }

}
