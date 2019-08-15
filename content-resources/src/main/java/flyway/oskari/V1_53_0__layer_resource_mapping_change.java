package flyway.oskari;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrates layer permissions from type+url+name to layer id as resource_mapping
 */
public class V1_53_0__layer_resource_mapping_change implements JdbcMigration {

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

    private List<Permission> getPermissions(Connection conn, String mapping) {
        String sql = "select p.external_type, p.external_id, p.permission from oskari_permission where oskari_resource_id = (" +
                "select id from oskari_resource where resource_type = 'maplayer' and resource_mapping = 'mapping')";
        return new ArrayList<>();
    }

    private void deleteMaplayerResources(Connection conn) {
        // TODO 1: DELETE all resources of type maplayer and the permissions for them/add cascade?
        String sql = "delete from oskari_resource where resource_type = 'maplayer'";
    }

    private void insertLayerResource(Connection conn, Layer layer) {
        // TODO 2: Insert new resources with mapping == layer.id
        String resourceInsertSQL = "insert into oskari_resource(resource_type, resource_mapping) VALUES('maplayer', layer.id)";
        String permissionInsertSQL = "insert into oskari_permission(oskari_resource_id, permission, external_type, external_id) VALUES(?,?,?,?)";
    }

    private List<Layer> getLayers(Connection conn) {
        String sql = "select id, type, url, name from oskari_maplayer";
        return new ArrayList<>();
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
