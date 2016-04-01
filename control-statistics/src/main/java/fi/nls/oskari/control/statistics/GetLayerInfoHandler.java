package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.db.LayerMetadata;
import fi.nls.oskari.control.statistics.db.LayerMetadataMapper;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Returns the layer information. This specifies the name and id attributes in the geoserver layer.
 * Sample response:
 * {
 *   "9": {
 *     "name": "oskari:kunnat2013",
 *     "nameTag": "kuntanimi",
 *     "idTag": "kuntakoodi",
 *     "url": " http://localhost:8080/geoserver"
 *   }
 * }
 */
@OskariActionRoute("GetLayerInfo")
public class GetLayerInfoHandler extends ActionHandler {
    private static final Logger LOG = LogFactory.getLogger(GetLayerInfoHandler.class);
    private Map<Long, LayerMetadata> layerMetadata;
    
    public void handleAction(ActionParameters ap) throws ActionException {
        JSONObject response = getLayerInfoJSON();
        ResponseHelper.writeResponse(ap, response);
    }

    JSONObject getLayerInfoJSON() throws ActionException {
        JSONObject response = new JSONObject();
        for (Long id : layerMetadata.keySet()) {
            LayerMetadata metadata = layerMetadata.get(id);
            JSONObject tags = new JSONObject();
            try {
                if (metadata != null && metadata.getType().equals("statslayer")) {
                    JSONObject attributes = new JSONObject(metadata.getAttributes());
                    JSONObject statistics = attributes.getJSONObject("statistics");
                    tags.put("nameIdTag", statistics.getString("nameIdTag"));
                    tags.put("regionIdTag", statistics.getString("regionIdTag"));
                    tags.put("url", metadata.getUrl());
                    tags.put("featuresUrl", statistics.getString("featuresUrl"));
                    response.put(String.valueOf(id), tags);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ActionException("Something went wrong serializing the layer infos.", e);
            }
        }
        return response;
    }

    public Map<Long, LayerMetadata> getLayerMetadata() {
        return layerMetadata;
    }

    @Override
    public void init() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        try {
            this.layerMetadata = new HashMap<>();
            List<LayerMetadata> layerMetadataRows = session.selectList("getAllMetadata");
            for (LayerMetadata row : layerMetadataRows) {
                this.layerMetadata.put(row.getOskariLayerId(),
                        row);
            }
            LOG.debug("Oskari layer metadatas: ", this.layerMetadata);
        } finally {
            session.close();
        }
    }
    
    private SqlSessionFactory initializeIBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(LayerMetadata.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(LayerMetadataMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

}
