module.exports = function(client) {
    client.connect(function(err) {
        if (err) {
            return console.error('Could not connect to postgres', err);
        }
        // Get all published mapfulls
        var query = client.query(
            'SELECT' +
            '  view_id,' +
            '  config' +
            'FROM' +
            '  portti_view_bundle_seq' +
            'WHERE' +
            '  view_id IN (SELECT id FROM portti_view WHERE type=\'PUBLISHED\') AND' +
            '  bundle_id = (SELECT id FROM portti_bundle WHERE name=\'mapfull\')' +
            'ORDER BY view_id'
        );

        var rowCount = 0,
            updateCount = 0,
            finished = false;
        query.on('row', function(row) {
            rowCount++;

            var config = {};
            try {
                config = JSON.parse(row.config);
            } catch (e) {
                console.error('Unable to parse config for view ' + row.view_id + '. Error:\'', e, '\'. Please update manually! Config:\r\n', row.config);
                updateCount++;
                return;
            }

            // Add MarkersPlugin to config if not already there
            if (config && config.plugins) {
                if (!config.plugins.some(function (val) {
                    return 'Oskari.mapframework.mapmodule.MarkersPlugin' === val.id;
                })) {
                    config.plugins.push(
                        {
                            id: 'Oskari.mapframework.mapmodule.MarkersPlugin'
                        }
                    );
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
                if ((updateCount === rowCount) && finished) {
                    console.log(updateCount + ' of ' + rowCount + ' rows updated');
                    client.end();
                }
            });
        });

        query.on('end', function(row) {
            finished = true;
        });
    });

};