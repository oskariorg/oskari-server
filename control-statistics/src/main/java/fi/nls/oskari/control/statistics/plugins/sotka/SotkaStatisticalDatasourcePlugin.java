package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.db.SotkaLayer;
import fi.nls.oskari.control.statistics.plugins.sotka.db.SotkaLayerMapper;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SotkaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourcePlugin.class);
    
    private SotkaIndicatorsParser indicatorsParser = null;

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

    @Override
    public void init() {
        // Fetching the layer mapping from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        final List<SotkaLayer> layerRows = session.selectList("getAll");
        layerMappings = new HashMap<>();
        for (SotkaLayer row : layerRows) {
            layerMappings.put(row.getSotkaLayerId().toLowerCase(), row.getOskariLayerId());
        }
        System.out.println("SotkaNET layer mappings: " + String.valueOf(layerMappings));
    }
    
    private SqlSessionFactory initializeIBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(SotkaLayer.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(SotkaLayerMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }
    @Override
    public boolean canCache() {
        return true;
    }
}
