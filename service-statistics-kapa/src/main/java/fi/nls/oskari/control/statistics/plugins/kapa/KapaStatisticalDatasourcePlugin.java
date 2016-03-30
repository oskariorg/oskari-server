package fi.nls.oskari.control.statistics.plugins.kapa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.PluginLayer;
import fi.nls.oskari.control.statistics.plugins.db.PluginLayerMapper;
import fi.nls.oskari.control.statistics.plugins.kapa.parser.KapaIndicator;
import fi.nls.oskari.control.statistics.plugins.kapa.parser.KapaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.kapa.requests.KapaRequest;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import org.json.JSONObject;

@Oskari("KAPA")
public class KapaStatisticalDatasourcePlugin extends OskariComponent implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(KapaStatisticalDatasourcePlugin.class);
    private KapaIndicatorsParser indicatorsParser;
    
    /**
     * Maps the KaPa layer identifiers to Oskari layers.
     */
    private Map<String, Long> layerMappings;
    private JSONObject config = new JSONObject();
    
    public KapaStatisticalDatasourcePlugin() {
        indicatorsParser = new KapaIndicatorsParser();
    }

    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        // Getting the general information of all the indicator layers.
        KapaRequest request = new KapaRequest();
        String jsonResponse = request.getIndicators();
        List<KapaIndicator> indicators = indicatorsParser.parse(jsonResponse, layerMappings);
        return indicators;
    }

    public void setConfig(JSONObject obj) {
        if(obj == null) {
            return;
        }
        config = obj;
    }

    @Override
    public void init() {
        // Fetching the layer mapping from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        try {
            final List<PluginLayer> layerRows = session.selectList("getAllForPlugin",
                    "KAPA");
            layerMappings = new HashMap<>();
            for (PluginLayer row : layerRows) {
                layerMappings.put(row.getPluginLayerId().toLowerCase(), row.getOskariLayerId());
            }
            System.out.println("KaPa layer mappings: " + String.valueOf(layerMappings));
        } finally {
            session.close();
        }
    }
    
    private SqlSessionFactory initializeIBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(PluginLayer.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(PluginLayerMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public boolean canCache() {
        return true;
    }
}
