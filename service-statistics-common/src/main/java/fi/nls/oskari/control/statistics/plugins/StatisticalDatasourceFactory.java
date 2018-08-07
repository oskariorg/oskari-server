package fi.nls.oskari.control.statistics.plugins;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayerMapper;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.service.OskariComponent;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.oskari.service.util.ServiceFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used to create plugin instances for source
 */
public abstract class StatisticalDatasourceFactory extends OskariComponent {
    public abstract StatisticalDatasourcePlugin create(StatisticalDatasource source);

    public void setupSourceLayers(StatisticalDatasource source) {
        // Fetching the layer mapping from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        final SqlSessionFactory factory = initializeIBatis(dataSource);
        try (final SqlSession session = factory.openSession()) {
            final List<DatasourceLayer> layerRows = session.getMapper(DatasourceLayerMapper.class).getLayersForDatasource(source.getId());

            // fetch a list of permissions for the regionset layers
            List<Long> layerIdList = layerRows.stream()
                    .map(DatasourceLayer::getMaplayerId)
                    .collect(Collectors.toList());
            Map<Long, List<Permissions>> permissions = ServiceFactory.getPermissionsService()
                    .getPermissionsForLayers(layerIdList, Permissions.PERMISSION_TYPE_VIEW_LAYER);
            // attach role ids that are permitted to see this regionset for each layer
            layerRows.forEach(layer -> attachRoles(layer, permissions));
            source.setLayers(layerRows);
        }
    }

    /**
     * Adds roles that are permitted to see the regionset for the layer
     * @param layer layer to attach permitted roles
     * @param permissions map with layer id as key and layer permissions as value
     */
    private void attachRoles(DatasourceLayer layer, Map<Long, List<Permissions>> permissions) {
        List<Permissions> perm = permissions.get(layer.getMaplayerId());
        layer.addRoles(getRoleIdsForLayer(perm));
    }

    /**
     * Returns a list of role ids
     * @param permissions
     * @return
     */
    private Set<Long> getRoleIdsForLayer(List<Permissions> permissions) {
        if(permissions == null) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .filter(p -> p.getExternalIdType().equals(Permissions.EXTERNAL_TYPE_ROLE))
                .map(p -> Long.parseLong(p.getExternalId()))
                .collect(Collectors.toSet());
    }

    private SqlSessionFactory initializeIBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(DatasourceLayer.class);
        configuration.setLazyLoadingEnabled(true);
        // typehandlers aren't found from classpath even when annotated.
        // also important to register them before adding mappers
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.addMapper(DatasourceLayerMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
