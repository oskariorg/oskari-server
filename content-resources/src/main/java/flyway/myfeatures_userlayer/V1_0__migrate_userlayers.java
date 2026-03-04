package flyway.myfeatures_userlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldType;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

public class V1_0__migrate_userlayers extends BaseJavaMigration {

    public static final String TABLE_MYFEATURES_USERLAYER = "myfeatures_user_layer";

    private static final Logger LOG = LogFactory.getLogger(V1_0__migrate_userlayers.class);
    private static final String fid = MyFeaturesFieldInfo.FID.getName();

    public void migrate(Context ctx) throws Exception {
        BaseLayer baseLayer = getBaseLayer();
        
        DatasourceHelper helper = DatasourceHelper.getInstance();

        try (Connection cUserLayer = helper.getDataSource(helper.getOskariDataSourceName("userlayer")).getConnection();
            Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            doMigrate(baseLayer, cUserLayer, c);
        }
    }

    private static class BaseLayer {
        int opacity;
        JSONObject locale;
        JSONObject options;
        JSONObject attributes;
    }

    private static class UserLayerDb {
        long id;
        String uuid;
        String publisherName;
        Instant created;
        Instant updated;
        JSONObject options;
        JSONObject locale;
    }

    private static class UserLayerDataDb {
        String featureId;
        JSONObject propertyJson;
        Geometry geometry;
        int srid;
        Instant created;
        Instant updated;
    }


    private static BaseLayer getBaseLayer() throws Exception {
        BaseLayer baseLayer = new BaseLayer();
        baseLayer.opacity = 80;
        baseLayer.locale = new JSONObject();
        baseLayer.options = new JSONObject();
        baseLayer.attributes = new JSONObject();

        int baseLayerId = PropertyUtil.getOptional("userlayer.baselayer.id", -1);
        if (baseLayerId < 0) {
            return baseLayer;
        }

        String sql = "SELECT opacity, locale, options, attributes FROM oskari_maplayer WHERE id = ?";

        DataSource oskaridb = DatasourceHelper.getInstance().getDataSource();
        try (Connection c = oskaridb.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, baseLayerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    baseLayer.opacity = rs.getInt("opacity");
                    baseLayer.locale = parseJSONObject(rs.getString("locale"));
                    baseLayer.options = parseJSONObject(rs.getString("options"));
                    baseLayer.attributes = parseJSONObject(rs.getString("attributes"));
                }
            }
        }

        baseLayer.attributes.remove("maxFeatures");
        baseLayer.attributes.remove("namespaceURL");

        return baseLayer;
    }

    private static void doMigrate(BaseLayer baseLayer, Connection cUserLayer, Connection c) throws Exception {
        cUserLayer.setAutoCommit(false);
        c.setAutoCommit(false);
        
        List<UserLayerDb> layers = getUserLayers(cUserLayer);
        int size = layers.size();
        LOG.info("Beginning to migrate", layers.size(), "userlayers...");
        
        createMyFeaturesUserLayer(c);
        c.commit();

        SynchronousQueue<UserLayerWithData> queue = new SynchronousQueue<>();
        
        Thread writer = new Thread(() -> {
            try {
                while (true) {
                    UserLayerWithData layerAndData = queue.take();
                    if (layerAndData.layer == null) {
                        break;
                    }

                    for (UserLayerDataDb x : layerAndData.data) {
                        dataToMyFeature(x);
                    }
                
                    JSONArray fields = getFields(layerAndData.layer, layerAndData.data);
                    UUID layerId = insertLayer(c, baseLayer, layerAndData.layer, fields);
                    insertFeatures(c, layerId, layerAndData.data);
                    refreshLayerMetadata(c, layerId);
                    setMigrated(c, layerAndData.layer.id, layerId);

                    c.commit();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        writer.start();

        int i = 0;
        for (UserLayerDb layer : layers) {
            if (++i % 100 == 0) {
                LOG.info("Progress", i, "/", size);
            }

            if (isMigrated(c, layer.id)) {
                continue;
            }

            List<UserLayerDataDb> data = getUserLayerData(cUserLayer, layer.id);
            if (data.isEmpty()) {
                // No need to migrate user_layers with zero features
                continue;
            }
            UserLayerWithData layerAndData = new UserLayerWithData();
            layerAndData.layer = layer;
            layerAndData.data = data;
            queue.put(layerAndData);
        }

        // "Poison pill"
        queue.put(new UserLayerWithData());
        // wait for writer to finish
        writer.join();
    }

    private static class UserLayerWithData {
        UserLayerDb layer;
        List<UserLayerDataDb> data;
    }

    private static void createMyFeaturesUserLayer(Connection c) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_MYFEATURES_USERLAYER + " (user_layer_id bigint PRIMARY KEY, layer_id uuid)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static boolean isMigrated(Connection c, long userLayerId) throws Exception {
        String sql = "SELECT layer_id FROM " + TABLE_MYFEATURES_USERLAYER + " WHERE user_layer_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userLayerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void setMigrated(Connection c, long userLayerId, UUID layerId) throws Exception {
        String sql = "INSERT INTO " + TABLE_MYFEATURES_USERLAYER + " (user_layer_id, layer_id) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userLayerId);
            ps.setObject(2, layerId);
            ps.executeUpdate();
        }
    }


    private static List<UserLayerDb> getUserLayers(Connection c) throws Exception {
        String sql = "SELECT id, uuid, publisher_name, created, updated, options, locale FROM user_layer";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<UserLayerDb> layers = new ArrayList<>();
            while (rs.next()) {
                layers.add(parseUserLayer(rs));
            }
            return layers;
        }
    }

    private static UserLayerDb parseUserLayer(ResultSet rs) throws Exception {
        UserLayerDb l = new UserLayerDb();
        l.id = rs.getLong("id");
        l.uuid = rs.getString("uuid");
        l.publisherName = rs.getString("publisher_name");
        l.created = offsetDateTimeToInstant(rs.getObject("created", OffsetDateTime.class), () -> null);
        l.updated = offsetDateTimeToInstant(rs.getObject("updated", OffsetDateTime.class), () -> null);
        l.options = parseJSONObject(rs.getString("options"));
        l.locale = parseJSONObject(rs.getString("locale"));
        return l;
    }

    private static Instant offsetDateTimeToInstant(OffsetDateTime odt, Supplier<Instant> fb) {
        return odt != null ? odt.toInstant() : fb.get();
    }

    private static List<UserLayerDataDb> getUserLayerData(Connection c, long userLayerId) throws Exception {
        String sql = "SELECT feature_id, property_json, ST_AsBinary(geometry) AS geometry, ST_SRID(geometry) AS srid, created, updated FROM user_layer_data WHERE user_layer_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userLayerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserLayerDataDb> data = new ArrayList<>();
                while (rs.next()) {
                    UserLayerDataDb x = parseUserLayerData(rs);
                    if (x != null) {
                        data.add(x);
                    }
                }
                return data;
            }
        }
    }

    private static UserLayerDataDb parseUserLayerData(ResultSet rs) throws Exception {
        UserLayerDataDb x = new UserLayerDataDb();
        x.featureId = rs.getString("feature_id");
        x.propertyJson = parseJSONObject(rs.getString("property_json"));
        x.geometry = new WKBReader().read(rs.getBytes("geometry"));
        x.srid = rs.getInt("srid");
        x.created = offsetDateTimeToInstant(rs.getObject("created", OffsetDateTime.class), () -> null);
        x.updated = offsetDateTimeToInstant(rs.getObject("updated", OffsetDateTime.class), () -> null);
        
        return isValid(x) ? x : null;
    }

    private static boolean isValid(UserLayerDataDb x) {
        return isGeometryValid(x.geometry);
    }

    private static boolean isGeometryValid(Geometry g) {
        if (g.isEmpty()) {
            return false;
        }

        if (g instanceof GeometryCollection c) {
            for (int i = 0; i < c.getNumGeometries(); i++) {
                if (!isGeometryValid(c.getGeometryN(i))) {
                    return false;
                }
            }
            return true;
        }

        if (g instanceof LineString ls) {
            if (ls.getNumPoints() < 2 || ls.getCoordinateN(0).equals2D(ls.getCoordinateN(1))) {
                return false;
            }
        }

        return true;
    }



    private static JSONArray getFields(UserLayerDb layer, List<UserLayerDataDb> data) {
        boolean addFid = data.stream().anyMatch(x -> x.featureId != null && !x.featureId.isBlank());
        return autoDetectFields(layer.id, data.get(0), addFid);
    }

    private static JSONArray autoDetectFields(long userLayerId, UserLayerDataDb data, boolean addFid) {
        JSONArray fields = new JSONArray();

        for (String key : data.propertyJson.keySet()) {
            Object o = data.propertyJson.get(key);

            if (o == null) {
                // What to do here...
                LOG.warn("Can't tell the type of null object, user_layer_id:", userLayerId, "property", key);
                continue;
            }

            Optional<MyFeaturesFieldType> optType = MyFeaturesFieldType.valueFromBinding(o.getClass());
            if (optType.isEmpty()) {
                LOG.warn("Can't detect type for class:", o.getClass().getName(), "user_layer_id:", userLayerId, "property:", key);
                continue;
            }

            MyFeaturesFieldType type = optType.get();
            fields.add(new JSONObject().put("name", key).put("type", type));
        }

        if (addFid) {
            // Make sure fid already doesn't exist
            for (int i = 0; i < fields.size(); i++) {
                JSONObject f = (JSONObject) fields.get(i);
                if (MyFeaturesFieldInfo.FID.getName().equals(f.getString("name"))) {
                    addFid = false;
                    break;
                }
            }

            if (addFid) {
                fields.add(new JSONObject()
                    .put("name", MyFeaturesFieldInfo.FID.getName())
                    .put("type", MyFeaturesFieldInfo.FID.getType())
                );
            }
        }

        return fields;
    }

    private static void dataToMyFeature(UserLayerDataDb x) {
        if (x.featureId != null && !x.featureId.isBlank()) {
            x.propertyJson.put(fid, x.featureId);
        }
        if (x.updated == null) {
            x.updated = x.created;
        }
    }

    private static UUID insertLayer(Connection c, BaseLayer baseLayer, UserLayerDb layer, JSONArray fields) throws Exception {
        JSONObject options = JSONHelper.merge(baseLayer.options, layer.options);
        JSONObject locale = JSONHelper.merge(baseLayer.locale, layer.locale);
        JSONObject attributes = baseLayer.attributes;

        UUID id = UUID.randomUUID();

        String sql = "INSERT INTO myfeatures_layer (id, opacity, published, created, updated, owner_uuid, locale, fields, options, attributes) VALUES (?, ?, ?, ?, ?, ?, ?::json, ?::json, ?::json, ?::json)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setInt(2, baseLayer.opacity);
            ps.setBoolean(3, layer.publisherName != null && !layer.publisherName.isBlank());
            ps.setTimestamp(4, layer.created != null ? Timestamp.from(layer.created) : null);
            ps.setTimestamp(5, layer.updated != null ? Timestamp.from(layer.created) : null); // fallback to created
            ps.setString(6, layer.uuid); // owner_uuid
            ps.setString(7, locale.toString());
            ps.setString(8, fields.toString());
            ps.setString(9, options.toString());
            ps.setString(10, attributes.toString());
            ps.executeUpdate();
        }

        return id;
    }

    private static void insertFeatures(Connection c, UUID layerId, List<UserLayerDataDb> features) throws Exception {
        String sql = "INSERT INTO myfeatures_feature (layer_id, created, updated, geom, properties) VALUES (?, ?, ?, ST_SetSRID(ST_GeomFromWKB(?), ?), ?::json)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (UserLayerDataDb feature : features) {
                ps.setObject(1, layerId);
                ps.setObject(2, feature.created != null ? Timestamp.from(feature.created) : null);
                ps.setObject(3, feature.updated != null ? Timestamp.from(feature.updated) : null);
                ps.setBytes(4, new WKBWriter().write(feature.geometry));
                ps.setInt(5, feature.srid);
                ps.setString(6, feature.propertyJson.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void refreshLayerMetadata(Connection c, UUID layerId) throws Exception {
        String sql = "UPDATE myfeatures_layer a SET feature_count = b.count, extent = b.extent FROM ("
            + "  SELECT cnt AS count, ARRAY [ST_XMin(xtent), ST_YMin(xtent), ST_XMax(xtent), ST_YMax(xtent)] AS extent "
            + "  FROM ("
            + "    SELECT COUNT(*) AS cnt, ST_Extent(geom) AS xtent FROM myfeatures_feature WHERE layer_id = ?"
            + "  ) AS sub"
            + ") AS b "
            + "WHERE a.id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, layerId);
            ps.setObject(2, layerId);
            ps.executeUpdate();
        }
    }

    // Simplified version of JSONObjectMyBatisTypeHandler.valueOf(String) (avoid the dependency)
    private static JSONObject parseJSONObject(String s) {
        if (s == null || s.isBlank()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            LOG.warn("Couldn't parse DB string to JSONObject:", s, e);
            return new JSONObject();
        }
    }

}
