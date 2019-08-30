package flyway.oskari;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrates layer permissions from type+url+name to layer id as resource_mapping
 */
public class V1_54_0__layer_resource_mapping_change implements JdbcMigration {

    public void migrate(Connection conn) throws SQLException {
        List<Layer> layers = getLayers(conn);
        for (Layer layer : layers) {
            layer.permissions.addAll(getPermissions(conn, layer.getOldMapping()));
        }
        deleteMaplayerResources(conn);

        for (Layer layer : layers) {
            insertLayerResource(conn, layer);
        }
        conn.commit();
    }

    private List<Permission> getPermissions(Connection conn, String mapping) throws SQLException {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT p.external_type, p.external_id, p.permission " +
                "FROM oskari_permission " +
                "WHERE oskari_resource_id = " +
                "(SELECT id FROM oskari_resource WHERE resource_type = 'maplayer' AND resource_mapping = ?)";

        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, mapping);
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    Permission permission = new Permission();
                    permission.type = rs.getString("external_type");
                    permission.extId = rs.getInt("external_id");
                    permission.permission = rs.getString("permission");
                    list.add(permission);
                }
            }
        }
        return list;
    }

    private void deleteMaplayerResources(Connection conn) throws SQLException {
        List<String> statemets = new ArrayList<>();

        statemets.add("DELETE FROM oskari_permission " +
                "WHERE oskari_resource_id NOT IN (SELECT id FROM oskari_resource)");

        statemets.add("ALTER TABLE oskari_resource " +
                "ADD CONSTRAINT oskari_resource_pk PRIMARY KEY (id)");

        statemets.add("ALTER TABLE oskari_permission " +
                "ADD CONSTRAINT oskari_resource_id_fkey  FOREIGN KEY (oskari_resource_id) " +
                "REFERENCES oskari_resource (id) " +
                "ON DELETE CASCADE");

        statemets.add("DELETE FROM oskari_resource WHERE resource_type = 'maplayer'");

        for (String sql : statemets) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

    private void insertLayerResource(Connection conn, Layer layer) throws SQLException {
        int resourceId = 0;
        String fetchIdSql = "NEXTVAL('oskari_resource_id_seq')";
        try (PreparedStatement statement = conn.prepareStatement(fetchIdSql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                resourceId = rs.getInt(1);
            }
        }
        if (resourceId == 0) {
            throw new RuntimeException("Couldn't get next resource id from sequence");
        }
        String resourceInsertSQL =
                "INSERT INTO oskari_resource(id, resource_type, resource_mapping) " +
                "VALUES(?, 'maplayer', ?)";
        try (PreparedStatement statement = conn.prepareStatement(resourceInsertSQL)) {
            statement.setInt(1, resourceId);
            statement.setString(2, "" + layer.id);
            statement.execute();
        }
        String permissionInsertSQL =
                "INSERT INTO oskari_permission(oskari_resource_id, permission, external_type, external_id) " +
                "VALUES(?,?,?,?)";
        for (Permission permission : layer.permissions) {
            try (PreparedStatement statement = conn.prepareStatement(permissionInsertSQL)) {
                statement.setInt(1, resourceId);
                statement.setString(2, permission.permission);
                statement.setString(3, permission.type);
                statement.setLong(4, permission.extId);
            }
        }
    }

    private List<Layer> getLayers(Connection conn) throws SQLException {
        String sql = "SELECT id, type, url, name FROM oskari_maplayer";
        List<Layer> list = new ArrayList<>();
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    Layer lyr = new Layer();
                    lyr.id = rs.getInt("id");
                    lyr.type = rs.getString("type");
                    lyr.url = rs.getString("url");
                    lyr.name = rs.getString("name");
                    list.add(lyr);
                }
            }
        }
        return list;
    }

    class Layer {
        int id;
        String type;
        String url;
        String name;
        List<Permission> permissions = new ArrayList<>();

        String getOldMapping() {
            return type + "+" + url + "+" + name;
        }
    }

    class Permission {
        String type;
        // role or user id
        int extId;
        String permission;
    }

}
`