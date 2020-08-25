package org.oskari.usercontent;

import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by SMAKINEN on 4.9.2015.
 */
public class LayerHelper {

    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceMybatisImpl();
    private static final DataProviderService GROUP_SERVICE = new DataProviderServiceMybatisImpl();

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

}
