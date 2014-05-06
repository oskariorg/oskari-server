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
      "bundle_id = (SELECT id FROM portti_bundle WHERE name='mapfull') AND " +
      "view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')"
    );

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;

      var config = {};
      var startup = {};

      try {
          config = JSON.parse(row.config);
          startup = JSON.parse(row.startup);
      }
      catch(e) {
          console.error("Unable to parse config/startp for view " + row.view_id + ". Error:'", e, "'. Please update manually!");
          updateCount++;
          return;
      }

      startup['metadata']['Import-Bundle']['mapuserlayers'] = {
        "bundlePath" : "/Oskari/packages/framework/bundle/"
      };
      var infoPlugin = _.find(config.plugins, {"id": "Oskari.mapframework.mapmodule.GetInfoPlugin"});
      if (infoPlugin) {
        infoPlugin.config.ignoredLayerTypes.push('USERLAYER');
      }
      config.plugins.push({
        "id": "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin"
      });

      var updatedConfig = JSON.stringify(config);
      var updatedStartup = JSON.stringify(startup);

      var updateQuery = "UPDATE portti_view_bundle_seq SET config='" + updatedConfig + "', " +
        "startup='" + updatedStartup + "' " +
        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') " + 
        "AND view_id=" + row.view_id;

      client.query(updateQuery, function(err, res) {
        if(err) throw err;

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

}

