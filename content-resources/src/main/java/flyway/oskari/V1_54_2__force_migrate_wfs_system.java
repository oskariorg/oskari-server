package flyway.oskari;

/**
 * Replaces transport based wfs plugin with vector implementation
 */
public class V1_54_2__force_migrate_wfs_system extends V1_54_1__migrate_wfs_system {

    public boolean optOut() {
        return false;
    }
}
