package fi.nls.oskari.map.layer;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.*;

@Oskari("OskariLayerService")
public class OskariLayerServiceMybatisImpl extends OskariLayerService {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerServiceMybatisImpl.class);

    private static DataProviderService dataProviderService = new DataProviderServiceMybatisImpl();

    private SqlSessionFactory factory = null;

    public OskariLayerServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            LOG.error("Couldn't get datasource for oskari layer service");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(OskariLayer.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.addMapper(OskariLayerMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private OskariLayer mapData(Map<String, Object> data) {
        if(data == null) {
            return null;
        }
        if(data.get("id") == null) {
            // this will make the keys case insensitive (needed for hsqldb compatibility...)
            final Map<String, Object> caseInsensitiveData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
            caseInsensitiveData.putAll(data);
            data = caseInsensitiveData;
        }

        final OskariLayer result = new OskariLayer();

        result.setId((Integer) data.get("id"));
        result.setParentId((Integer) data.get("parentid"));
        result.setType((String) data.get("type"));
        result.setBaseMap((Boolean) data.get("base_map"));
        result.setInternal((Boolean) data.get("internal"));
        result.setName((String) data.get("name"));
        result.setUrl((String) data.get("url"));
        result.setLocale(JSONHelper.createJSONObject((String) data.get("locale")));

        // defaults
        result.setOpacity((Integer) data.get("opacity"));
        result.setStyle((String) data.get("style"));
        result.setMinScale((Double) data.get("minscale"));
        result.setMaxScale((Double) data.get("maxscale"));

        // additional info
        result.setLegendImage((String) data.get("legend_image"));
        result.setMetadataId((String) data.get("metadataid"));

        // map implementation parameters
        result.setParams(JSONHelper.createJSONObject((String) data.get("params")));
        result.setOptions(JSONHelper.createJSONObject((String) data.get("options")));
        result.setAttributes(JSONHelper.createJSONObject((String) data.get("attributes")));
        result.setCapabilities(JSONHelper.createJSONObject((String) data.get("capabilities")));

        // gfi configurations
        result.setGfiType((String) data.get("gfi_type"));
        result.setGfiXslt((String) data.get("gfi_xslt"));
        result.setGfiContent((String) data.get("gfi_content"));

        // realtime configurations
        result.setRealtime((Boolean) data.get("realtime"));
        result.setRefreshRate((Integer) data.get("refresh_rate"));

        // username and password
        result.setUsername((String) data.get("username"));
        result.setPassword((String) data.get("password"));

        result.setGeometry((String) data.get("geom"));

        result.setSrs_name((String) data.get("srs_name"));
        result.setVersion((String) data.get("version"));

        result.setCreated((Date) data.get("created"));
        result.setUpdated((Date) data.get("updated"));

        // Automatic update of Capabilities
        result.setCapabilitiesLastUpdated((Date) data.get("capabilities_last_updated"));
        result.setCapabilitiesUpdateRateSec((Integer) data.get("capabilities_update_rate_sec"));

        // populate dataproviders for top level layers
        if(result.getParentId() == -1) {
            // sublayers and internal baselayers don't have dataprovider_id
            Object dataProviderId = data.get("dataprovider_id");
            if(dataProviderId != null) {
                result.setDataproviderId((Integer)dataProviderId);
                try {
                    // populate layer group
                    // first run (~700 layers) with this lasts ~1800ms, second run ~300ms (cached)
                    final DataProvider dataProvider = dataProviderService.find(result.getDataproviderId());
                    result.addDataprovider(dataProvider);
                } catch (Exception ex) {
                    LOG.error("Couldn't get organisation for layer", result.getId());
                    return null;
                }
            }

        }

        return result;
    }

    private List<OskariLayer> mapDataList(final List<Map<String,Object>> list) {
        final List<OskariLayer> layers = new ArrayList<OskariLayer>();
        final Map<Integer, OskariLayer> collections = new HashMap<Integer, OskariLayer>(20);
        for(Map<String, Object> map : list) {
            final OskariLayer layer = mapData(map);
            if(layer != null) {
                // collect parents so we can map sublayers more easily
                if(layer.isCollection()) {
                    collections.put(layer.getId(), layer);
                }
                // NOTE! SQLs need to return parents before sublayers so we can do this (ORDER BY parentId ASC)
                final OskariLayer parent = collections.get(layer.getParentId());
                if(parent != null) {
                    // add layer as sublayer for parent IF there is a parent
                    parent.addSublayer(layer);
                }
                else {
                    // otherwise these are actual layers OR we are mapping sublayers individually (f.ex. finding by external id)
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

    public List<OskariLayer> findByUrlAndName(final String url, final String name) {

        LOG.debug("Find by url: " + url + " and name: " + name);
        final SqlSession session = factory.openSession();
        List<OskariLayer> layers = new ArrayList<>();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            Map<String, String> params = new HashMap<String, String>();
            params.put("url", url);
            params.put("name", name);
            layers = mapDataList(mapper.findByUrlAndName(params));
        } catch (Exception e) {
            LOG.warn(e,"Unable to find by url: " + url + " and name: " + name);
        } finally {
            session.close();
        }
        return layers;
    }

    public List<OskariLayer> findByIdList(final List<Integer> intList) {
        LOG.debug("Find by id list");
        if(intList.isEmpty()){
            return new ArrayList<>();
        }
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            Map<String, Object> params = new HashMap<>();
            params.put("intList", intList);
            params.put("parentIntList", intList);
            List<Map<String,Object>> result = mapper.findByIdList(params);
            final List<OskariLayer> layers = mapDataList(result);
            //Reorder layers to requested order
            List<OskariLayer> reLayers = new ArrayList<>();
            for (Integer id : intList) {
                for (OskariLayer lay : layers) {
                    if (lay.getId() == id) {
                        reLayers.add(lay);
                        break;
                    }
                }
            }
            return reLayers;
        } catch (Exception e) {
            LOG.warn(e, "Unable to find by id list");
        } finally {
            session.close();
        }
        return new ArrayList<>();
    }

    public OskariLayer find(int id) {
        LOG.debug("find by id: " + id);
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            // get as list since we might have a collection layer (get sublayers with same query)
            final List<OskariLayer> layers =  mapDataList(mapper.find(id));
            if(layers != null && !layers.isEmpty()) {
                // should we check for multiples? only should have one since sublayers are mapped in mapDataList()
                return layers.get(0);
            }
        } catch (Exception e) {
            LOG.warn(e, "Exception when getting layer with id: " + id);
        } finally {
            session.close();
        }
        return null;
    }

    public List<OskariLayer> findByMetadataId(String uuid) {
        LOG.debug("Find by metadata id: " + uuid);
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            return mapDataList(mapper.findByUuid(uuid));
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting metadata with id: " + uuid);
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }

    private List<OskariLayer> findByParentId(int parentId) {
        LOG.debug("Find by parent id: " + parentId);
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            return mapDataList(mapper.findByParentId(parentId));
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting by parent id: " + parentId);
        } finally {
            session.close();
        }
        return null;
    }

    public List<OskariLayer> findAll() {
        long start = System.currentTimeMillis();
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            List<Map<String,Object>> result = mapper.findAll();
            LOG.debug("Find all layers:", System.currentTimeMillis() - start, "ms");
            start = System.currentTimeMillis();
            final List<OskariLayer> layers = mapDataList(result);
            LOG.debug("Parsing all layers:", System.currentTimeMillis() - start, "ms");
            return layers;
        } catch (Exception e) {
            LOG.warn(e, "");
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }

    @Override
    public List<OskariLayer> findAllWithPositiveUpdateRateSec() {
        long t0 = System.currentTimeMillis();
        LOG.debug("Find all with positive update rate sec");
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            List<Map<String,Object>> result = mapper.findAllWithPositiveUpdateRateSec();
            long t1 = System.currentTimeMillis();
            LOG.debug("Find layers with positive update rate sec took:", t1-t0, "ms");
            List<OskariLayer> layers = mapDataList(result);
            long t2 = System.currentTimeMillis();
            LOG.debug("Parse layers with positive update rate sec took:", t2-t1, "ms");
            return layers;
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting oskari layers with positive update rate sec");
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }
    public List<String> findAllLayerNames () {
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            return mapper.findAllNames();
        } catch (Exception e) {
            LOG.warn(e, "Exception while getting oskari layer names");
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }

    public void update(final OskariLayer layer) {
        LOG.debug("update layer");
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            mapper.update(layer);
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        } finally {
            session.close();
        }
    }

    public synchronized int insert(final OskariLayer layer) {
        LOG.debug("insert new layer");
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            mapper.insert(layer);
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            session.close();
        }
        return layer.getId();
    }

    public void delete(int id) {
        LOG.debug("delete layer with id: " + id);
        final SqlSession session = factory.openSession();
        try {
            final OskariLayerMapper mapper = session.getMapper(OskariLayerMapper.class);
            mapper.delete(id);
            session.commit();
        } catch (Exception e) {
            LOG.error(e, "Couldn't delete with id:", id);
        } finally {
            session.close();
        }
    }

    public void delete(Map<String, String> parameterMap) {
        delete(ConversionHelper.getInt(parameterMap.get("id"), -1));
    }

}