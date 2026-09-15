package flyway.myfeatures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

/**
 * Partition myfeatures_feature into n partitions by layer_id (hash)
 * only if configured via property 'myfeatures.numPartitions'
 */
public class V1_0_5__partitioning extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_0_5__partitioning.class);

    private static final String PROP_NUM_PARTITIONS = "myfeatures.numPartitions";

    public void migrate(Context ctx) throws Exception {
        int numPartitions = PropertyUtil.getOptional(PROP_NUM_PARTITIONS, -1);
        if (numPartitions <= 1) {
            LOG.info("Skipping partitioning due to nothing to do as", PROP_NUM_PARTITIONS, "=", numPartitions);
            return;
        }
        checkNumPartitions(numPartitions);

        LOG.info("Creating partitions using setting", PROP_NUM_PARTITIONS, "=", numPartitions);

        DatasourceHelper helper = DatasourceHelper.getInstance();
        try (Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            exec(c, createPartitionedTable());
            for (int i = 0; i < numPartitions; i++) {
                exec(c, createPartition(i, numPartitions));
            }
            exec(c, insertFromNonPartitioned());
            for (int i = 0; i < numPartitions; i++) {
                exec(c, createGeomIndex(i));
            }
            exec(c, dropNonPartitioned());
            exec(c, renamePartitioned());
        }
    }

    private static void checkNumPartitions(int numPartitions) {
        switch (numPartitions) {
            case 2, 4, 8, 16, 32, 64, 128, 256: break;
            default: throw new IllegalArgumentException(PROP_NUM_PARTITIONS + " must be one of 2, 4, 8, 16, 32, 64, 128, 256!");
        }
    }

    private static void exec(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        }
    }

    private static String createPartitionedTable() {
        return ""
            + "CREATE TABLE myfeatures_feature_p ("
            + "	id bigint GENERATED ALWAYS AS IDENTITY,"
            + "	layer_id uuid,"
            + "	created timestamp with time zone,"
            + "	updated timestamp with time zone,"
            + "	geom geometry,"
            + "	properties json,"
            // Primary key must include partition key (layer_id)
            + "	CONSTRAINT pk_myfeatures_feature_p PRIMARY KEY (layer_id, id),"
            + "	CONSTRAINT myfeatures_feature_p_myfeatures_layer_fkey FOREIGN KEY (layer_id) REFERENCES myfeatures_layer(id) ON DELETE CASCADE"
            + ")"
            + "PARTITION BY HASH (layer_id)";
    }

    private static String createPartition(int remainder, int modulus) {
        return String.format("CREATE TABLE myfeatures_feature_p%d PARTITION OF myfeatures_feature_p FOR VALUES WITH (MODULUS %d, REMAINDER %d)",
            remainder, modulus, remainder);
    }

    private static String insertFromNonPartitioned() {
        return "INSERT INTO myfeatures_feature_p (layer_id, created, updated, geom, properties)"
            + " SELECT layer_id, created, updated, geom, properties FROM myfeatures_feature";
    }

    private static String createGeomIndex(int remainder) {
        return String.format("CREATE INDEX myfeatures_feature_p%d_geom_idx ON myfeatures_feature_p%d USING gist (geom)", remainder, remainder);
    }

    private static String dropNonPartitioned() {
        return "DROP TABLE myfeatures_feature";
    }

    private static String renamePartitioned() {
        return "ALTER TABLE myfeatures_feature_p RENAME TO myfeatures_feature";
    }
}
