var _ = require("lodash-node");

module.exports = function (client) {
    client.connect(function (err) {
        if (err) {
            return console.error('Could not connect to postgres', err);
        }
        var query = client.query(
            "SELECT " +
            "  view_id, " +
            "  config " +
            "FROM " +
            "  portti_view_bundle_seq " +
            "WHERE " +
            "  config LIKE '%LogoPlugin%' AND " +
            "  ( " +
            "    config NOT LIKE '%mapUrlPrefix%' OR " +
            "    config NOT LIKE '%termsUrl%' " +
            "  ) AND " +
            "  view_id IN (SELECT id FROM portti_view WHERE type='PUBLISHED') AND " +
            "  bundle_id IN (SELECT id FROM portti_bundle WHERE name='mapfull') " +
            "ORDER BY " +
            "  view_id"
        );

        var rowCount = 0,
            updateCount = 0,
            finished = false;
        query.on("row", function (row) {
            rowCount++;

            var config = {};
            try {
                config = JSON.parse(row.config);
            } catch (e) {
                console.error("Unable to parse config for view " + row.view_id + ". Error:'", e, "'. Please update manually! Config:\r\n", row.config);
                updateCount++;
                return;
            }

            // Add mapUrlPrefix and termsUrl to LogoPlugin config if not already present
            var logoPlugin = _.find(config.plugins, {
                id: "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin"
            });
            if (logoPlugin) {
                if (!logoPlugin.config) {
                    logoPlugin.config = {};
                }
                if (!logoPlugin.config.mapUrlPrefix) {
                    logoPlugin.config.mapUrlPrefix = {
                        "fi": "//www.paikkatietoikkuna.fi/web/fi/kartta?",
                        "sv": "//www.paikkatietoikkuna.fi/web/sv/kartfonstret?",
                        "en": "//www.paikkatietoikkuna.fi/web/en/map-window?"
                    };
                }
                if (!logoPlugin.config.termsUrl) {
                    logoPlugin.config.termsUrl = {
                        "fi": "//www.paikkatietoikkuna.fi/web/fi/kayttoehdot",
                        "sv": "//www.paikkatietoikkuna.fi/web/sv/anvandningsvillkor",
                        "en": "//www.paikkatietoikkuna.fi/web/en/terms-and-conditions"
                    };
                }
            }


            var updatedConfig = JSON.stringify(config);
            var updateQuery = "UPDATE portti_view_bundle_seq SET config='" + updatedConfig + "' WHERE" +
                " bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') AND view_id=" + row.view_id;

            client.query(updateQuery, function (err, res) {
                if (err) {
                    throw err;
                }

                updateCount++;
                if ((updateCount === rowCount) && finished) {
                    console.log(updateCount + ' of ' + rowCount + ' rows updated');
                    client.end();
                }
            });
        });

        query.on("end", function (row) {
            finished = true;
        });
    });

};
