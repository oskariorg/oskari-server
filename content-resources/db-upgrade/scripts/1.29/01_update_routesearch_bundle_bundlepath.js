module.exports = function(client) {
    client.connect(function(err) {
        if (err) {
            return console.error('Could not connect to postgres', err);
        }

        var query = client.query("SELECT * FROM portti_bundle WHERE name = 'routesearch'");
        var rowCount = 0,
            updateCount = 0,
            finished = false;

        query.on("row", function(row) {
            rowCount++;
            var startup = {};
            try {
                startup = JSON.parse(row.startup);
            } catch (e) {
                console.error('Unable to parse startup for bundle ' + row.id + '. Error:\'', e, '\'. Please update manually! Startup:\r\n', row.startup);
                updateCount++;
                return;
            }

            startup.metadata["Import-Bundle"].routesearch.bundlePath = 
                startup.metadata["Import-Bundle"].routesearch.bundlePath.replace("packages/framework/bundle/", "packages/paikkatietoikkuna/bundle/");

            var updateQueryString = "UPDATE portti_bundle SET startup = '"+JSON.stringify(startup, false, '    ')+"' WHERE id = '"+row.id+"';";
//            console.log("============================")
//            console.log("Query? "+updateQueryString);
            var updateQuery = client.query(updateQueryString, function(err, result) {
                if (err) {
                    console.error("ERROR! query: "+updateQueryString);
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