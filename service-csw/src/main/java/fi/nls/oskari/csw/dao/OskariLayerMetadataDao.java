package fi.nls.oskari.csw.dao;

import fi.nls.oskari.csw.dto.OskariLayerMetadataDto;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 7.11.2014
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class OskariLayerMetadataDao {
    private final SqlSessionFactory factory;
    private static final Logger log = LogFactory.getLogger(OskariLayerMetadataDao.class);

    public OskariLayerMetadataDao(final DataSource dataSource) {
        this.factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeAliasRegistry().registerAlias(OskariLayerMetadataDto.class);
        configuration.addMapper(OskariLayerMetadataDto.Mapper.class);

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
