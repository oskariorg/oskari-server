package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SMAKINEN on 11.12.2017.
 */
public class ThematicMapsRegionsetHelper {

    private static final Logger LOG = LogFactory.getLogger(ThematicMapsRegionsetHelper.class);

    private static final String PROP_LAYER_KUNTA = "flyway.1_45_4.layer.name.kunta";
    private static final String PROP_LAYER_ALUEHALLINTOVIRASTO = "flyway.1_45_4.layer.name.aluehallintovirasto";
    private static final String PROP_LAYER_MAAKUNTA = "flyway.1_45_4.layer.name.maakunta";
    private static final String PROP_LAYER_NUTS1 = "flyway.1_45_4.layer.name.nuts1";
    private static final String PROP_LAYER_SAIRAANHOITOPIIRI = "flyway.1_45_4.layer.name.sairaanhoitopiiri";
    private static final String PROP_LAYER_SEUTUKUNTA = "flyway.1_45_4.layer.name.seutukunta";
    private static final String PROP_LAYER_ERVA = "flyway.1_45_4.layer.name.erva";
    private static final String PROP_LAYER_ELY_KESKUS= "flyway.1_45_4.layer.name.elykeskus";
    
    // Old regionCategory => property that tells the name of the layer (oskari_maplayer.name)
    private static final Map<String, String> REGION_CATEGORY_TO_PROP;
    static {
        REGION_CATEGORY_TO_PROP = new HashMap<>();
        REGION_CATEGORY_TO_PROP.put("KUNTA", PROP_LAYER_KUNTA);
        REGION_CATEGORY_TO_PROP.put("ALUEHALLINTOVIRASTO", PROP_LAYER_ALUEHALLINTOVIRASTO);
        REGION_CATEGORY_TO_PROP.put("MAAKUNTA", PROP_LAYER_MAAKUNTA);
        REGION_CATEGORY_TO_PROP.put("NUTS1", PROP_LAYER_NUTS1);
        REGION_CATEGORY_TO_PROP.put("SAIRAANHOITOPIIRI", PROP_LAYER_SAIRAANHOITOPIIRI);
        REGION_CATEGORY_TO_PROP.put("SEUTUKUNTA", PROP_LAYER_SEUTUKUNTA);
        REGION_CATEGORY_TO_PROP.put("ERVA", PROP_LAYER_ERVA);
        REGION_CATEGORY_TO_PROP.put("ELY-KESKUS", PROP_LAYER_ELY_KESKUS);
    }
    private Map<String, Integer> oldRegionNameToLayerId;
    public ThematicMapsRegionsetHelper(Connection conn) throws SQLException {
        oldRegionNameToLayerId = getRegionToLayerId(conn);
    }

    public Integer getLayerIdForName(String name) {
        return oldRegionNameToLayerId.get(name);
    }
    public Map<String, Integer> getOldNameToLayerIdMapping() {
        return oldRegionNameToLayerId;
    }

    private Map<String, Integer> getRegionToLayerId(Connection conn) throws SQLException {
        Map<String, String> categoryToLayerName = getCategoryToLayerNameFromProperties();

        Map<String, Integer> categoryToLayerId = new HashMap<>();
        String sql = "SELECT id FROM oskari_maplayer WHERE type = 'statslayer' AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<String, String> categoryLayerName : categoryToLayerName.entrySet()) {
                String category = categoryLayerName.getKey();
                String layerName = categoryLayerName.getValue();
                ps.setString(1, layerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        LOG.debug("Found layerId:", id,
                                "for category:", category,
                                "from database with layerName:", layerName);
                        categoryToLayerId.put(category, id);
                    } else {
                        LOG.warn("Could not find layerId for category:", category,
                                "from database with layerName:", layerName);
                    }
                }
            }
        }
        return categoryToLayerId;
    }

    private Map<String, String> getCategoryToLayerNameFromProperties() {
        Map<String, String> categoryToLayerName = new HashMap<>();
        for (Map.Entry<String, String> categoryToProperty : REGION_CATEGORY_TO_PROP.entrySet()) {
            String category = categoryToProperty.getKey();
            String property = categoryToProperty.getValue();
            String layerName = PropertyUtil.getOptional(property);
            if (layerName == null || layerName.isEmpty()) {
                LOG.warn("Could not find layerName for category:", category,
                        "from property:", property);
            } else {
                LOG.debug("Found layerName:", layerName,
                        "for category:", category,
                        "from property:", property);
                categoryToLayerName.put(category, layerName);
            }
        }
        return categoryToLayerName;
    }
}
