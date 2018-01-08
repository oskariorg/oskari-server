package fi.nls.oskari.map.layer;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.Reader;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.12.2013
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
@Oskari("OskariLayerService")
public class OskariLayerServiceIbatisImpl extends OskariLayerService {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerServiceIbatisImpl.class);
    private static boolean crsSupported = PropertyUtil.getOptional("oskari.crs.switch.supported", false);
    private SqlMapClient client = null;

    // make it static so we can change this with one call to all services when needed
    private static String SQL_MAP_LOCATION = "META-INF/SqlMapConfig.xml";

    private static DataProviderService dataProviderService = new DataProviderServiceIbatisImpl();
    private static OskariMapLayerGroupService oskariMapLayerGroupService = new OskariMapLayerGroupServiceIbatisImpl();

    // map different layer types
    private static Map<String, Class<OskariLayer>> typeMapping =
            Collections.singletonMap(OskariLayer.TYPE_WMS, OskariLayer.class);

    /**
     * Static setter to override default location
     * @param newLocation
     */
    public static void setSqlMapLocation(final String newLocation) {
        SQL_MAP_LOCATION = newLocation;
    }
    /**
     * Returns SQLmap
     *
     * @return
     */
    protected SqlMapClient getSqlMapClient() {
        if (client != null) {
            return client;
        }

        Reader reader = null;
        try {
            String sqlMapLocation = getSqlMapLocation();
            reader = Resources.getResourceAsReader(sqlMapLocation);
            client = SqlMapClientBuilder.buildSqlMapClient(reader);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SQL client", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return SQL_MAP_LOCATION;
    }

    protected String getNameSpace() {
        return "OskariLayer";
    }

    private OskariLayer createLayerInstance(final String type) {
        Class<OskariLayer> clazz = typeMapping.get(type);
        if(clazz != null) {
            try {
                return clazz.newInstance();
            }
            catch (Exception ex) {
                LOG.warn(ex, "Couldn't create instance for type:", type, "- Using default model.");
            }
        }
        else {
            //LOG.info("Unregistered layertype:", type, "- Using default model.");
        }
        return new OskariLayer();
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

        final OskariLayer result = createLayerInstance((String) data.get("type"));
        if(result == null) {
            LOG.warn("Unknown layer type:", data.get("type"));
            return null;
        }

        result.setId((Integer) data.get("id"));
        result.setParentId((Integer) data.get("parentid"));
        result.setType((String) data.get("type"));
        result.setExternalId((String) data.get("externalid"));
        result.setBaseMap((Boolean) data.get("base_map"));
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
        result.setTileMatrixSetId((String) data.get("tile_matrix_set_id"));

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

        // populate groups for top level layers
        if(result.getParentId() == -1) {
            // sublayers and internal baselayers don't have dataprovider_id
            Object dataProviderId = data.get("dataprovider_id");
            if(dataProviderId != null) {
                result.setDataproviderId((Integer)dataProviderId);
                try {
                    // populate layer group
                    // first run (~700 layers) with this lasts ~1800ms, second run ~300ms (cached)
                    final DataProvider dataProvider = dataProviderService.find(result.getDataproviderId());
                    result.addGroup(dataProvider);
                } catch (Exception ex) {
                    LOG.error("Couldn't get organisation for layer", result.getId());
                    return null;
                }
            }

            // FIXME: oskariMapLayerGroupService has built in caching (very crude) to make this fast,
            // without it getting maplayer groups makes the query 10 x slower
            // populate groups
            try {
                final List<MaplayerGroup> groups = oskariMapLayerGroupService.findByMaplayerId(result.getId());
                result.addGroups(groups);
            } catch (Exception ex) {
                LOG.error("Couldn't get groups for layer", result.getId());
                return null;
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
        Map<String, String> params = new HashMap<String, String>();
        params.put("url", url);
        params.put("name", name);
        final List<OskariLayer> layers =  mapDataList(queryForList(getNameSpace() + ".findByUrlAndName", params));
        return layers;
    }

    public OskariLayer find(final String idStr) {
        final int id = ConversionHelper.getInt(idStr, -1);
        if(id != -1) {
            return find(id);
        }
        // try to find with external id
        try {
            final List<Map<String, Object>> lresults = (List<Map<String, Object>>) getSqlMapClient().queryForList(getNameSpace() + ".findByExternalId", idStr);
            final OskariLayer layer = mapData(lresults.get(0));
            if(layer.isCollection()) {
                final List<OskariLayer> sublayers = findByParentId(layer.getId());
                LOG.debug("FindByParent returned", sublayers.size(), "sublayers for parent id:", layer.getId());
                layer.addSublayers(sublayers);
            }
            return layer;
        } catch (Exception e) {
            LOG.warn(e, "Couldn't find layer with id:", idStr);
        }
        return null;
    }

    public List<OskariLayer> find(final List<String> idList) {
        return find(idList, null);
    }
    
    public List<OskariLayer> find(final List<String> idList, String crs) {
        // TODO: break list into external and internalIds -> make 2 "where id/externalID in (...)" SQLs
        // ensure order stays the same
        final List<Integer> intList = ConversionHelper.getIntList(idList);
        final List<String> strList =  ConversionHelper.getStringList(idList);
        if(intList.size() < 1 && strList.size() < 1){
            return new ArrayList<OskariLayer>();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("strList", strList);
        params.put("intList", intList);
        if(crsSupported){
            params.put("crs", crs);
        } else {
            params.put("crs", null);
        }

        List<Map<String,Object>> result = queryForList(getNameSpace() + ".findByIdList", params);
        final List<OskariLayer> layers = mapDataList(result);

        //Reorder layers to requested order
        return OskariLayerWorker.reorderLayers(layers, idList);

    }


    public OskariLayer find(int id) {
        try {
            client = getSqlMapClient();
            // get as list since we might have a collection layer (get sublayers with same query)
            final List<OskariLayer> layers =  mapDataList(queryForList(getNameSpace() + ".findById", id));
            if(layers != null && !layers.isEmpty()) {
                // should we check for multiples? only should have one since sublayers are mapped in mapDataList()
                return layers.get(0);
            }
        } catch (Exception e) {
            LOG.warn(e, "Exception when getting layer with id:", id);
        }
        LOG.warn("Couldn't find layer with id:", id);
        return null;
    }

    public List<OskariLayer> findByMetadataId(String uuid) {
        try {
            client = getSqlMapClient();
            final List<Map<String,Object>> list = queryForList(getNameSpace() + ".findByUuId", uuid);
            final List<OskariLayer> layers = mapDataList(list);
            return layers;
        } catch (Exception e) {
            LOG.warn(e, "Exception when getting layer with uuid:", uuid);
        }
        LOG.warn("Couldn't find layer with id:", uuid);
        return Collections.emptyList();
    }

    private List<OskariLayer> findByParentId(int parentId) {
        try {
            client = getSqlMapClient();
            return mapDataList(queryForList(getNameSpace() + ".findByParentId", parentId));
        } catch (Exception e) {
            LOG.warn(e, "Couldn't find layers with parentId:", parentId);
        }
        return null;
    }

    public List<OskariLayer> findAll(String crs) {
        long start = System.currentTimeMillis();
        String crsIn = crsSupported ? crs : null;
        List<Map<String,Object>> result = queryForList(getNameSpace() + ".findAll", crsIn);
        LOG.debug("Find all layers:", System.currentTimeMillis() - start, "ms");
        start = System.currentTimeMillis();
        final List<OskariLayer> layers = mapDataList(result);
        LOG.debug("Parsing all layers:", System.currentTimeMillis() - start, "ms");
        return layers;
    }

    public List<OskariLayer> findAll() {
       return this.findAll(null);
    }

    public void update(final OskariLayer layer) {
        try {
            getSqlMapClient().update(getNameSpace() + ".update", layer);
            // link to inspire theme(s)
            oskariMapLayerGroupService.updateLayerGroups(layer.getId(), layer.getMaplayerGroups());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        }
    }

    public synchronized int insert(final OskariLayer layer) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.insert(getNameSpace() + ".insert", layer);
            Integer id = (Integer) client.queryForObject(getNameSpace()
                    + ".maxId");
            layer.setId(id);
            client.commitTransaction();
            // link to inspire theme(s)
            oskariMapLayerGroupService.updateLayerGroups(id, layer.getMaplayerGroups());
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }

    public void delete(int id) {
        try {
            client.delete(getNameSpace() + ".delete", id);
        } catch (Exception e) {
            LOG.error(e, "Couldn't delete with id:", id);
        }
    }

    public void delete(Map<String, String> parameterMap) {
        delete(ConversionHelper.getInt(parameterMap.get("id"), -1));
    }

    /**
     * Queries for list with given param object
     *
     * @param sqlId
     * @param param objectIdentifier
     * @return
     */
    private List<Map<String,Object>> queryForList(String sqlId, Object param) {
        try {
            client = getSqlMapClient();
            List<Map<String,Object>> results = client.queryForList(sqlId, param);
            return results;
        } catch (Exception e) {
            LOG.error(e, "Couldn't query list. SqlId:", sqlId, " - Param:", param);
        }
        return Collections.emptyList();
    }

    private List<Map<String,Object>> queryForList(String sqlId) {
        try {
            client = getSqlMapClient();
            List<Map<String,Object>> results = client.queryForList(sqlId);
            return results;
        } catch (Exception e) {
            LOG.error(e, "Couldn't query list. SqlId:", sqlId);
        }
        return Collections.emptyList();
    }


}
