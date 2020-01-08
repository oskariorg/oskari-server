package flyway.myplaces;

import fi.nls.oskari.db.DatasourceHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class V1_0_8__update_property_fields implements JdbcMigration {

    public void migrate(Connection ignored) throws Exception {
        // myplaces _can_ use other db than the default one
        // -> Use connection to default db for this migration
        DataSource ds = DatasourceHelper.getInstance().getDataSource();
        if (ds == null) {
            ds = DatasourceHelper.getInstance().createDataSource();
        }
        Connection conn = ds.getConnection();
        final String sql = "update portti_wfs_layer\n" +
                "set\n" +
                "selected_feature_params =\n" +
                "'{\n" +
                "  \"default\": [\"name\", \"place_desc\",\"link\", \"image_url\", \"attention_text\"],\n" +
                "  \"fi\": [\"name\", \"place_desc\", \"link\", \"image_url\", \"attention_text\"],\n" +
                "  \"sv\": [\"name\", \"place_desc\", \"link\", \"image_url\", \"attention_text\"],\n" +
                "  \"en\": [\"name\", \"place_desc\", \"link\", \"image_url\", \"attention_text\"]\n" +
                "}',\n" +
                "feature_params_locales =\n" +
                "'{\n" +
                "  \"fi\": [\"Nimi\", \"Kuvaus\", \"Linkki\", \"Kuvalinkki\", \"Teksti kartalla\"],\n" +
                "  \"sv\": [\"Namn\", \"Beskrivelse\", \"Webbaddress\", \"URL-address\", \"Bild-URL\", \"Text på kartan\"],\n" +
                "  \"en\": [\"Name\", \"Description\", \"URL\", \"Image URL\", \"Text on map\"]\n" +
                "}'\n" +
                "where layer_name = 'oskari:my_places';\n";

        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.execute();
        }
    }

}
