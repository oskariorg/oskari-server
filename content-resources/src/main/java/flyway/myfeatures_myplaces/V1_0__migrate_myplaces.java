package flyway.myfeatures_myplaces;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldType;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

public class V1_0__migrate_myplaces extends BaseJavaMigration {

    public static final String TABLE_MYFEATURES_CATEGORIES = "myfeatures_categories";

    private static final Logger LOG = LogFactory.getLogger(V1_0__migrate_myplaces.class);

    public void migrate(Context ctx) throws Exception {
        BaseLayer baseLayer = getBaseLayer();

        DatasourceHelper helper = DatasourceHelper.getInstance();

        try (Connection cMyPlaces = helper.getDataSource(helper.getOskariDataSourceName("myplaces")).getConnection();
            Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            cMyPlaces.setAutoCommit(false);
            c.setAutoCommit(false);

            List<MyPlaceCategory> categories = getCategories(cMyPlaces);
            
            LOG.info("Beginning to migrate", categories.size(), "myplaces layers...");
            int i = 0;
            
            createMyFeaturesCategories(c);
            c.commit();

            for (MyPlaceCategory category : categories) {
                if (++i % 100 == 0) {
                    LOG.info("Progress", i, "/", categories.size());
                }
                
                if (isMigrated(c, category.getId())) {
                    continue;
                }
                
                List<MyPlace> myPlaces = getMyPlaces(cMyPlaces, category.getId());
                if (myPlaces.isEmpty()) {
                    // No need to migrate myplaces categories with zero features
                    continue;
                }
            
                MyFeaturesLayer layer = categoryToLayer(category, baseLayer);
                List<MyFeaturesFeature> features = myPlaces.stream()
                    .map(myPlace -> myPlaceToMyFeature(myPlace))
                    .collect(Collectors.toList());
            
                Instant min = features.stream()
                    .map(MyFeaturesFeature::getCreated)
                    .min(Instant::compareTo)
                    .orElseGet(Instant::now);

                layer.setCreated(min);
                layer.setUpdated(min);
            
                insertLayer(c, layer);
                insertFeatures(c, layer.getId(), features);
                refreshLayerMetadata(c, layer.getId());
                setMigrated(c, category.getId(), layer.getId());

                c.commit();
            }
        }
    }

    private static void createMyFeaturesCategories(Connection c) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_MYFEATURES_CATEGORIES + " (category_id bigint PRIMARY KEY, layer_id uuid)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static boolean isMigrated(Connection c, long categoryId) throws Exception {
        String sql = "SELECT layer_id FROM " + TABLE_MYFEATURES_CATEGORIES + " WHERE category_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void setMigrated(Connection c, long categoryId, UUID layerId) throws Exception {
        String sql = "INSERT INTO " + TABLE_MYFEATURES_CATEGORIES + " (category_id, layer_id) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            ps.setObject(2, layerId);
            ps.executeUpdate();
        }
    }

    private static class BaseLayer {
        int opacity;
        JSONObject locale;
        JSONObject options;
        JSONObject attributes;
    }

    private static BaseLayer getBaseLayer() throws Exception {
        BaseLayer baseLayer = new BaseLayer();
        baseLayer.opacity = 50;
        baseLayer.locale = new JSONObject();
        baseLayer.options = new JSONObject();
        baseLayer.attributes = new JSONObject();

        int baseLayerId = PropertyUtil.getOptional("myplaces.baselayer.id", -1);
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
                    baseLayer.locale = JSONObjectMybatisTypeHandler.valueOf(rs.getString("locale"));
                    baseLayer.options = JSONObjectMybatisTypeHandler.valueOf(rs.getString("options"));
                    baseLayer.attributes = JSONObjectMybatisTypeHandler.valueOf(rs.getString("attributes"));
                }
            }
        }

        baseLayer.attributes.remove("maxFeatures");
        baseLayer.attributes.remove("namespaceURL");

        return baseLayer;
    }

    private static List<MyPlaceCategory> getCategories(Connection c) throws Exception {
        String sql = "SELECT id, uuid, publisher_name, category_name, options, locale FROM categories";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<MyPlaceCategory> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(parseCategory(rs));
            }
            return categories;
        }
    }

    private static MyPlaceCategory parseCategory(ResultSet rs) throws Exception {
        MyPlaceCategory category = new MyPlaceCategory();
        category.setId(rs.getLong("id"));
        category.setUuid(rs.getString("uuid"));
        category.setPublisher_name(rs.getString("publisher_name"));
        category.setCategory_name(rs.getString("category_name"));
        category.setOptions(JSONObjectMybatisTypeHandler.valueOf(rs.getString("options")));
        category.setLocale(JSONObjectMybatisTypeHandler.valueOf(rs.getString("locale")));
        return category;
    }

    private static List<MyPlace> getMyPlaces(Connection c, long categoryId) throws Exception {
        String sql = "SELECT id, uuid, name, attention_text, created, updated, ST_AsBinary(geometry) AS geometry, ST_SRID(geometry) AS srid, place_desc, link, image_url FROM my_places WHERE category_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            // category_id is of type integer in DB (whereas categories.id is bigint)
            ps.setInt(1, (int) categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MyPlace> myPlaces = new ArrayList<>();
                while (rs.next()) {
                    myPlaces.add(parseMyPlace(rs));
                }
                return myPlaces;
            }
        }
    }

    private static MyPlace parseMyPlace(ResultSet rs) throws Exception {
        MyPlace place = new MyPlace();
        place.setId(rs.getLong("id"));
        place.setUuid(rs.getString("uuid"));
        place.setName(rs.getString("name"));
        place.setAttentionText(rs.getString("attention_text"));
        place.setCreated(rs.getObject("created", OffsetDateTime.class));
        place.setUpdated(rs.getObject("updated", OffsetDateTime.class));
        Geometry g = new WKBReader().read(rs.getBytes("geometry"));
        int srid = rs.getInt("srid");
        g.setSRID(srid);
        place.setGeometry(g);
        place.setDesc(rs.getString("place_desc"));
        place.setLink(rs.getString("link"));
        place.setImageUrl(rs.getString("image_url"));
        return place;
    }

    private static MyFeaturesLayer categoryToLayer(MyPlaceCategory category, BaseLayer baseLayer) {
        WFSLayerOptions options = category.getWFSLayerOptions();
        options.injectBaseLayerOptions(baseLayer.options);

        JSONObject locale = JSONHelper.merge(baseLayer.locale, category.getLocale());
        JSONObject attributes = baseLayer.attributes;

        MyFeaturesLayer layer = new MyFeaturesLayer();
        layer.setId(UUID.randomUUID());
        layer.setOpacity(baseLayer.opacity);
        layer.setPublished(category.isPublished());        
        layer.setOwnerUuid(category.getUuid());
        layer.setLocale(locale);
        layer.setLayerFields(getLayerFields());
        layer.setOptions(options.getOptions());
        layer.setAttributes(attributes);
        return layer;
    }

    private static List<MyFeaturesFieldInfo> getLayerFields() {
        return List.of(
            MyFeaturesFieldInfo.of("attention_text", MyFeaturesFieldType.String),
            MyFeaturesFieldInfo.of("image_url", MyFeaturesFieldType.String),
            MyFeaturesFieldInfo.of("link", MyFeaturesFieldType.String),
            MyFeaturesFieldInfo.of("name", MyFeaturesFieldType.String),
            MyFeaturesFieldInfo.of("place_desc", MyFeaturesFieldType.String)
        );
    }

    private static MyFeaturesFeature myPlaceToMyFeature(MyPlace myPlace) {
        MyFeaturesFeature feature = new MyFeaturesFeature();
        feature.setCreated(offsetDateTimeToInstant(myPlace.getCreated(), Instant::now));
        feature.setUpdated(offsetDateTimeToInstant(myPlace.getUpdated(), Instant::now));
        feature.setGeometry(myPlace.getGeometry());
        feature.setProperties(getProperties(myPlace));
        return feature;
    }
    
    private static Instant offsetDateTimeToInstant(OffsetDateTime odt, Supplier<Instant> fb) {
        return odt != null ? odt.toInstant() : fb.get();
    }

    private static JSONObject getProperties(MyPlace myPlace) {
        JSONObject properties = new JSONObject();
        properties.put("attention_text", myPlace.getAttentionText());
        properties.put("image_url", myPlace.getImageUrl());
        properties.put("link", myPlace.getLink());
        properties.put("name", myPlace.getName());
        properties.put("place_desc", myPlace.getDesc());
        return properties;
    }

    private static void insertLayer(Connection c, MyFeaturesLayer layer) throws Exception {
        String sql = "INSERT INTO myfeatures_layer (id, opacity, published, created, updated, owner_uuid, locale, fields, options, attributes) VALUES (?, ?, ?, ?, ?, ?, ?::json, ?::json, ?::json, ?::json)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, layer.getId());
            ps.setInt(2, layer.getOpacity());
            ps.setBoolean(3, layer.isPublished());
            ps.setTimestamp(4, layer.getCreated() != null ? Timestamp.from(layer.getCreated()) : null);
            ps.setTimestamp(5, layer.getUpdated() != null ? Timestamp.from(layer.getUpdated()) : null);
            ps.setString(6, layer.getOwnerUuid());
            ps.setString(7, layer.getLocale().toString());
            ps.setString(8, layer.getFields().toString());
            ps.setString(9, layer.getOptions().toString());
            ps.setString(10, layer.getAttributes().toString());
            ps.executeUpdate();
        }
    }

    private static void insertFeatures(Connection c, UUID layerId, List<MyFeaturesFeature> features) throws Exception {
        String sql = "INSERT INTO myfeatures_feature (layer_id, created, updated, geom, properties) VALUES (?, ?, ?, ST_SetSRID(ST_GeomFromWKB(?), ?), ?::json)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (MyFeaturesFeature feature : features) {
                ps.setObject(1, layerId);
                ps.setObject(2, feature.getCreated() != null ? Timestamp.from(feature.getCreated()) : null);
                ps.setObject(3, feature.getUpdated() != null ? Timestamp.from(feature.getUpdated()) : null);
                ps.setBytes(4, new WKBWriter().write(feature.getGeometry()));
                ps.setInt(5, feature.getGeometry().getSRID());
                ps.setString(6, feature.getProperties().toString());
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

}
