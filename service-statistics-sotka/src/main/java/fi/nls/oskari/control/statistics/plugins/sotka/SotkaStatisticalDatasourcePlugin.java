package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

import fi.nls.oskari.util.JSONHelper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.PluginLayer;
import fi.nls.oskari.control.statistics.plugins.db.PluginLayerMapper;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import org.json.JSONObject;

@Oskari("SotkaNET")
public class SotkaStatisticalDatasourcePlugin extends OskariComponent implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourcePlugin.class);
    
    private SotkaIndicatorsParser indicatorsParser = null;
    private SotkaConfig config = new SotkaConfig();

    private static final String KEY_URL = "url";

    /**
     * Maps the SotkaNET layer identifiers to Oskari layers.
     */
    private Map<String, Long> layerMappings;

    public SotkaStatisticalDatasourcePlugin() {
        indicatorsParser = new SotkaIndicatorsParser();
    }
    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        try {
            // First getting general information of all the indicator layers.
            // Note that some mandatory information about the layers is not given here,
            // for example the year range, but must be requested separately for each indicator.
            SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
            request.setBaseURL(getBaseURL());
            String jsonResponse = request.getData();

            // We will later need to add the year range information to the preliminary information using separate requests.
            return indicatorsParser.parse(jsonResponse, layerMappings);
        } catch (APIException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new APIException("Something went wrong calling SotkaNET Indicators interface.", e);
        }
    }

    public void setConfig(JSONObject obj) {
        if(obj == null) {
            return;
        }
        config.setUrl(obj.optString(KEY_URL));
    }

    String getBaseURL() {
        return config.getUrl();
    }

    @Override
    public void init() {
        if(config.getUrl() == null) {
            // populate default base url
            config.setUrl("http://www.sotkanet.fi/rest");
        }
        indicatorsParser.setConfig(config);
        // Fetching the layer mapping from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        try {
            final List<PluginLayer> layerRows = session.selectList("getAllForPlugin",
                    "SotkaNET");
            layerMappings = new HashMap<>();
            for (PluginLayer row : layerRows) {
                layerMappings.put(row.getPluginLayerId().toLowerCase(), row.getOskariLayerId());
            }
            System.out.println("SotkaNET layer mappings: " + String.valueOf(layerMappings));
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
