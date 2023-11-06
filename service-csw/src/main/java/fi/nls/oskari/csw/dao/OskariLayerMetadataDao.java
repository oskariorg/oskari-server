package fi.nls.oskari.csw.dao;

import fi.nls.oskari.csw.dto.OskariLayerMetadataDto;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.sql.DataSource;

/**
 * Simple MyBatis Dao for saving CSW metadata for maplayers
 */
public class OskariLayerMetadataDao {
    private final SqlSessionFactory factory;
    private static final Logger log = LogFactory.getLogger(OskariLayerMetadataDao.class);

    public OskariLayerMetadataDao(final DataSource dataSource) {
        this.factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, OskariLayerMetadataDto.class);
        MyBatisHelper.addMappers(configuration, OskariLayerMetadataDto.Mapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public void saveMetadata(OskariLayerMetadataDto dto) {
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMetadataDto.Mapper mapper = session.getMapper(OskariLayerMetadataDto.Mapper.class);
            OskariLayerMetadataDto saved = mapper.find(dto.metadataId);
            if(saved == null) {
                mapper.insert(dto);
            }
            else {
                dto.id = saved.id;
                mapper.update(dto);
            }
            session.commit();
        } catch (Exception e) {
            log.error(e, "Error saving metadata");
        } finally {
            session.close();
        }
    }
}
