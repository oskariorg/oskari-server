package fi.nls.oskari.spatineo.dao;

import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.PorttiBackendStatusDto;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * Data access to the Oskari / Portti database WMS service status information.
 */
public class WmsServiceStatusDao {

    private final SqlSessionFactory factory;

    public WmsServiceStatusDao(final DataSource dataSource) {
        this.factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeAliasRegistry().registerAlias(OskariMapLayerDto.class);
        configuration.getTypeAliasRegistry().registerAlias(PorttiBackendStatusDto.class);
        configuration.addMapper(OskariMapLayerDto.Mapper.class);
        configuration.addMapper(PorttiBackendStatusDto.Mapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public void insertStatus(final PorttiBackendStatusDto status) {
        final SqlSession session = factory.openSession();
        try {
            final PorttiBackendStatusDto.Mapper mapper = session.getMapper(PorttiBackendStatusDto.Mapper.class);
            mapper.saveStatus(status);
            session.commit();
        } finally {
            session.close();
        }
    }

}
