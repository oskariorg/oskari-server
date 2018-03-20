package fi.nls.oskari.geoserver;

import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceIbatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;

import java.util.List;

/**
 * Created by SMAKINEN on 4.9.2015.
 */
public class LayerHelper {

    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();
    private static final DataProviderService GROUP_SERVICE = new DataProviderServiceIbatisImpl();

    public static OskariLayer getLayerWithName(final String name) {
        final List<OskariLayer> layers = LAYER_SERVICE.findAll();
        for(OskariLayer l :  layers) {
            if(name.equals(l.getName())) {
                return l;
            }
        }
        return null;
    }

    public static DataProvider getDataprovider() {

        // setup data producer/layergroup since original doesn't have one
        final List<DataProvider> groups = GROUP_SERVICE.findAll();
        // just use the first one, doesn't really matter
        return groups.get(0);
    }

    public static void insert(OskariLayer layer) {
        LAYER_SERVICE.insert(layer);
    }

    public static void update(OskariLayer layer) {
        LAYER_SERVICE.update(layer);
    }

    public static WFSLayerConfiguration getConfig(OskariLayer layer, String namespace) {

        WFSLayerConfiguration conf = new WFSLayerConfiguration();
        conf.setLayerId("" + layer.getId());
        conf.setLayerName(layer.getName());
        conf.setGMLGeometryProperty("geometry");
        conf.setGMLVersion("3.1.1");
        conf.setGML2Separator(false);
        conf.setMaxFeatures(2000);
        conf.setFeatureNamespace(namespace);
        conf.setFeatureParamsLocales("{\"default\": [\"name\", \"place_desc\",\"link\", \"image_url\"],\"fi\": [\"name\", \"place_desc\",\"link\", \"image_url\"]}");
        conf.setGeometryType("2d");
        conf.setGetMapTiles(false);
        conf.setGetFeatureInfo(true);
        conf.setTileRequest(false);
        conf.setGetHighlightImage(true);

        conf.setFeatureNamespaceURI("http://www.oskari.org");

        conf.setFeatureType("");
        conf.setSelectedFeatureParams("{}");
        conf.setGeometryNamespaceURI("");
        conf.setWps_params("{}");
        conf.setTileBuffer("{}");
        return conf;
    }

}
