package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayerMapper;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.OskariComponentManager;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.ResourceType;

import javax.sql.DataSource;
import java.util.*;
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
            List<Integer> layerIdList = layerRows.stream()
                    .map(l -> (int) l.getMaplayerId())
                    .collect(Collectors.toList());

            OskariLayerService layerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
            PermissionService permissionService = OskariComponentManager.getComponentOfType(PermissionService.class);
            List<OskariLayer> layers = layerService.findByIdList(layerIdList);

            // layer id -> set of role ids that have permissions
            Map<String, Set<Long>> rolesForLayers = new HashMap<>();
            layers.forEach(layer ->
                permissionService.findResource(ResourceType.maplayer, Integer.toString(layer.getId()))
                        .ifPresent( res ->
                                rolesForLayers.put(Integer.toString(layer.getId()), getRoleIdsForLayer(res.getPermissions())))
            );
            //  Adds roles that are permitted to see the regionset for the layer
            layerRows.forEach( dsLayer -> dsLayer.addRoles(rolesForLayers.getOrDefault(Long.toString(dsLayer.getMaplayerId()), Collections.emptySet())));
            source.setLayers(layerRows);
        }
    }

    /**
     * Returns a list of role ids
     * @param permissions
     * @return
     */
    private Set<Long> getRoleIdsForLayer(List<Permission> permissions) {
        if(permissions == null) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .filter(p -> PermissionExternalType.ROLE.equals(p.getExternalType()))
                .map(p -> (long) p.getExternalId())
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
