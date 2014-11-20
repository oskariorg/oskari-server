module.exports = function(client) {
    client.connect(function(err) {
        if(err) {
            return console.error('Could not connect to postgres', err);
        }
        // Get all published mapfulls
        var query = client.query(
            'SELECT' +
            '  view_id,' +
            '  config ' +
            'FROM' +
            '  portti_view_bundle_seq ' +
            'WHERE' +
            '  view_id IN (SELECT id FROM portti_view WHERE type=\'PUBLISHED\') AND' +
            '  bundle_id = (SELECT id FROM portti_bundle WHERE name=\'mapfull\')' +
            'ORDER BY view_id'
        );

        var rowCount = 0;
        var updateCount = 0;
        var finished = false;
        query.on("row", function(row) {
            rowCount++;

            var config = {};
            try {
                config = JSON.parse(row.config);
            } catch (e) {
                console.error('Unable to parse config for view ' + row.view_id + '. Error:\'', e, '\'. Please update manually! Config:\r\n', row.config);
                updateCount++;
                return;
            }

            if (config && config.plugins) {
                for (var i = 0; i < config.plugins.length; i++) {
                    if ((config.plugins[i].id) && (config.plugins[i].id === "Oskari.mapframework.mapmodule.MarkersPlugin")) {
                        if (config.plugins[i].config) {
                            config.plugins[i].config.markerButton = false;
                        } else {
                            config.plugins[i].config = { markerButton: false };
                        }
                    }
                }
            }

            var updatedConfig = JSON.stringify(config);

            var updateQuery = ('UPDATE portti_view_bundle_seq SET config=\'' + updatedConfig + '\' WHERE' +
                ' bundle_id = (SELECT id FROM portti_bundle WHERE name = \'mapfull\') AND view_id=' + row.view_id);

            client.query(updateQuery, function(err, res) {
                if (err) {
                    throw err;
                }

                updateCount++;
                if((updateCount === rowCount) && finished) {
                    console.log(updateCount + ' of ' + rowCount + ' rows updated');
                    client.end();
                }
            });
        });

        query.on("end", function(row) {
            finished = true;
        });
    });
};
