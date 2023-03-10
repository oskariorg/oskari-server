package fi.nls.oskari.map.style;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

@Oskari
public class VectorStyleServiceMybatisImpl extends VectorStyleService {
    private static final Logger log = LogFactory.getLogger(VectorStyleServiceMybatisImpl.class);
    private SqlSessionFactory factory = null;

    public VectorStyleServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for vector style service");
        }
        factory = initializeMyBatis(dataSource);
    }
    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(VectorStyle.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.addMapper(VectorStyleMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }
    public VectorStyle getStyleById(final long id) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStyleById(id);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector style for id: " + id, e);
        }
    }
    public List<VectorStyle> getStylesByUser(final long user) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStylesByUser(user);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector styles for user: " + user, e);
        }
    }
    public List<VectorStyle> getStylesByLayerId(final int layerId) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStylesByLayerId(layerId);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector styles for layer: " + layerId, e);
        }
    }
    public List<VectorStyle> getStyles(final long userId, final int layerId) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStyles(userId, layerId);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector styles for layer: " + layerId, e);
        }
    }
    public long deleteStyle(final long id) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.deleteStyle(id);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to delete vector style: " + id, e);
        }
    }
    public long saveStyle(final VectorStyle style) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.saveStyle(style);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to save vector style", e);
        }
    }
    public long updateStyle(final VectorStyle style) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.updateStyle(style);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to update vector style", e);
        }
    }
}
