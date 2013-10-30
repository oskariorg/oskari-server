package fi.mml.map.mapwindow.service.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.stats.StatsLayer;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.domain.map.wfs.FeatureType;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureParameter;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.domain.map.wfs.WFSService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

import javax.xml.namespace.QName;

/**
 * LayerClass implementation for Ibatis
 *
 */
public class MapLayerServiceIbatisImpl extends BaseIbatisService<Layer>
        implements MapLayerService {

    private CapabilitiesCacheService capabilitiesCacheService = new CapabilitiesCacheServiceIbatisImpl();
    private WFSLayerConfigurationService wfsConfigService = new WFSLayerConfigurationServiceIbatisImpl();
    private Logger log = LogFactory.getLogger(MapLayerServiceIbatisImpl.class);

    @Override
    protected String getNameSpace() {
        return "Layer";
    }

    public boolean hasPermissionToUpdate(final User user, final int layerId) {

        // TODO: check against permissions
        if (!user.isAdmin()) {
            return false;
        }
        if (layerId <= -1) {
            return false;
        }
        // TODO: maybe check if we have a layer with given id in DB
        return true;
    }

    @Override
    public List<Layer> findAll() {
        List<Layer> layers = queryForList(getNameSpace() + ".findAll");
        return populateLayers(layers);

    }

    @Override
    public Layer find(int id) {
        Layer layer = super.find(id);
        return populateLayer(layer);
    }

    @Override
    public List<Layer> findAllWMS() {
        List<Layer> layers = queryForList(getNameSpace() + ".findAllWMS");
        return layers;

    }

    public List<Layer> findWithLayerClass(int layerClassId) {
        List<Layer> layers = queryForList(getNameSpace()
                + ".findWithLayerClass", layerClassId);
        return populateLayers(layers);
    }

    public List<Layer> findWithInspireTheme(int inspireThemeId) {
        List<Layer> layers = queryForList(getNameSpace()
                + ".findWithInspireTheme", inspireThemeId);
        return populateLayers(layers);
    }

    //TODO make this take in the id only, backend should figure out the layer type
    private Layer populateLayer(Layer layer) {
        if(layer == null) {
            return null;
        }

        if (layer.getType().equals(Layer.TYPE_WFS)) {
            return findWFSLayer(layer.getId());
        } else if (layer.getType().equals(Layer.TYPE_STATS)) {
            return findStatsLayer(layer);
        } else {
            return layer;
        }
    }

    private List<Layer> populateLayers(List<Layer> layers) {
        List<Layer> populatedLayers = new ArrayList<Layer>();

        for (Layer layer : layers) {
            populatedLayers.add(populateLayer(layer));
        }

        return populatedLayers;
    }

    @Override
    public void delete(int id) {
        delete(getNameSpace() + ".delete", id);
    }

    /**
     * Insert WFS layer.
     * 
     * @param wfsLayer
     */
    public int insertWFSLayer(WFSLayer wfsLayer) {
        Integer wfsLayerId = null;
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.insert(getNameSpace() + ".insert", wfsLayer);
            wfsLayerId = (Integer) client.queryForObject(getNameSpace()
                    + ".maxId");
            wfsLayer.setId(wfsLayerId);
            insertWfsLayerAssociations(client, wfsLayer);
            client.commitTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException e) {
                    // forget
                }
            }
        }
        return wfsLayerId;
    }

    private void insertWfsLayerAssociations(SqlMapClient client,
            WFSLayer wfsLayer) throws SQLException {
        for (WFSService wfsService : wfsLayer.getSelectedWfsServices()) {
            Map<String, Integer> parameterWfsServiceOfLayer = new HashMap<String, Integer>();
            parameterWfsServiceOfLayer.put("wfsLayerId", wfsLayer.getId());
            parameterWfsServiceOfLayer.put("wfsServiceId", wfsService.getId());
            client.insert(getNameSpace() + ".insertWfsServiceOfLayer",
                    parameterWfsServiceOfLayer);
        }

        for (SelectedFeatureType selectedFeatureType : wfsLayer
                .getSelectedFeatureTypes()) {
            selectedFeatureType.setWfsLayerId(wfsLayer.getId());
            client.insert(getNameSpace() + ".insertSelectedFeatureTypes",
                    selectedFeatureType);
            int selectedFeatureTypeId = (Integer) client
                    .queryForObject(getNameSpace()
                            + ".maxIdSelectedFeatureType");
            selectedFeatureType.setId(selectedFeatureTypeId);

            for (SelectedFeatureParameter selectedFeatureParameter : selectedFeatureType
                    .getSelectedFeatureParameters()) {
                selectedFeatureParameter
                        .setSelectedFeatureTypeId(selectedFeatureTypeId);
                client.insert(getNameSpace()
                        + ".insertSelectedFeatureParameters",
                        selectedFeatureParameter);
            }
        }
    }

    /**
     * Modify WFS layer.
     * 
     * @param wfsLayer
     */
    public void modifyWFSLayer(WFSLayer wfsLayer) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.update(getNameSpace() + ".update", wfsLayer);
            client.delete(getNameSpace() + ".deleteSelectedWFSServices",
                    wfsLayer.getId());
            client.delete(getNameSpace() + ".deleteSelectedFeatureTypes",
                    wfsLayer.getId());
            insertWfsLayerAssociations(client, wfsLayer);
            client.commitTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException e) {
                    // forget
                }
            }
        }
    }

    /**
     * Return Stats layer information for given layer.
     * 
     * @param layer
     * @return
     */
    private StatsLayer findStatsLayer(final Layer layer) {
        final StatsLayer statsLayer = (StatsLayer) layer;
        statsLayer.setVisualizations(findStatsLayerVisualizations(layer.getId()));
        log.debug("Found visualizations: ", statsLayer.getVisualizations());
        return statsLayer;
    }

    public List<StatsVisualization> findStatsLayerVisualizations(
            final int layerId) {
        final List<StatsVisualization> visualizations = queryForList(
                getNameSpace() + ".findStatsLayerVisualizations", layerId);
        return visualizations;
    }

    /**
     * Return WFS layer with given id.
     * 
     * @param wfsLayerId
     * @return
     */
    public WFSLayer findWFSLayer(int wfsLayerId) {
        // find WFS layer
        WFSLayer wfsLayer = queryForObject(getNameSpace() + ".find",
                new Integer(wfsLayerId));
        return populateWFSLayer(wfsLayer);
    }

    public WFSLayer populateWFSLayer(WFSLayer wfsLayer) {

        if (wfsLayer == null) {
            throw new RuntimeException("WFS Layer not found in the database.");
        }

        final WFSLayerConfiguration configuration = wfsConfigService.findConfiguration(wfsLayer.getId());

        // for backwards compatibility - these need to be defined
        final WFSService service = new WFSService();
        service.setGmlVersion(configuration.getGMLVersion());
        service.setGml2typeSeparator(ConversionHelper.getBoolean(configuration.isGML2Separator(), false));
        service.setUrl(configuration.getURL());
        service.setUsername(configuration.getUsername());
        service.setPassword(configuration.getPassword());
        // use proxy not available in new transport - default to false
        service.setUseProxy(false);
        wfsLayer.getSelectedWfsServices().add(service);

        final FeatureType ft = new FeatureType();
        ft.setQname(configuration.getFeatureElementQName());
        ft.setWfsService(service);
        ft.setBboxParameterName(configuration.getGMLGeometryProperty());

        final SelectedFeatureType sft = new SelectedFeatureType();
        sft.setMaxNumDisplayedItems(configuration.getMaxFeatures());
        sft.setFeatureType(ft);
        wfsLayer.getSelectedFeatureTypes().add(sft);

        return wfsLayer;
    }

    public CapabilitiesCache getCapabilitiesCache(int id) {
        return capabilitiesCacheService.find(id);
    }

	public int insertCapabilities(CapabilitiesCache cc) {
		return capabilitiesCacheService.insert(cc);
	}

	public void updateCapabilities(CapabilitiesCache cc) {
		capabilitiesCacheService.update(cc);
	}
}
