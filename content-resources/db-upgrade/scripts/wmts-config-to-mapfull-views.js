var _ = require("lodash-node");

module.exports = function(client) {
  client.connect(function(err) {
    if(err) {
      return console.error('Could not connect to postgres', err);
    }
    var query = client.query(
    	"SELECT view_id, config, startup " +
      "FROM portti_view_bundle_seq " +
      "WHERE " +
      "bundle_id = (SELECT id FROM portti_bundle WHERE name='mapfull') ORDER BY view_id"
    );

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;
      var config = JSON.parse(row.config);

      // Set the map options for WMTS
      config["mapOptions"] = {
				"resolutions": [8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5],
				"maxExtent": {
					"left": -548576.000000,
					"bottom": 6291456.000000,
					"right": 1548576.000000,
					"top": 8388608.000000
				},
				"srsName": "EPSG:3067"
			},

      // Add the WMTS plugin if not already present
      var wmtsPlugin = _.find(config.plugins, { id: "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" });
      if(!wmtsPlugin) {
        config.plugins.push({
        	"id": "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin"
        });
      }

      var updatedConfig = JSON.stringify(config);

      // Add mapwmts bundle to mapfull's imports if missing
      var startup = JSON.parse(row.startup);
      var bundles = startup["metadata"]["Import-Bundle"];

      if (!bundles["mapwmts"]) {
	      bundles["mapwmts"] = {
	      	"bundlePath": "/Oskari/packages/framework/bundle/"
	      };
      }

      var updatedStartup = JSON.stringify(startup);

      var updateQuery = "UPDATE portti_view_bundle_seq SET config='" + updatedConfig + "', startup='" + updatedStartup + "' " +
        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') " + 
        "AND view_id=" + row.view_id;

      client.query(updateQuery, function(err, res) {
        if(err) throw err;

        updateCount++;
        if(updateCount === rowCount && finished)
          console.log(updateCount + ' of ' + rowCount + ' rows updated');
          client.end();
      });
    });

    query.on("end", function(row) {
      finished = true;
    });
  });

}

