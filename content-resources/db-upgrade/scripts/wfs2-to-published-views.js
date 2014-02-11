var _ = require("lodash-node");

module.exports = function(client) {

  client.connect(function(err) {
    if(err) {
      return console.error('could not connect to postgres', err);
    }
    var query = client.query("SELECT portti_view_bundle_seq.view_id, portti_view_bundle_seq.config " +
      "FROM portti_view " +
      "LEFT OUTER JOIN portti_view_bundle_seq ON portti_view_bundle_seq.view_id = portti_view.id " + 
      "WHERE " +
      "portti_view.type = 'PUBLISHED' AND " +
      "bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') ORDER BY id");

    var rowCount = 0;
    var oldWfsCount = 0;
    var updateCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;
      var config = JSON.parse(row.config);

      var wfs = _.find(config.plugins, { id: "Oskari.mapframework.bundle.mapwfs.plugin.wfslayer.WfsLayerPlugin" });
      if(wfs) {
        oldWfsCount++;
        wfs.id = "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin";
        wfs.config = { 
           "contextPath" : "/transport-0.0.1",
           "lazy" : true,
           "disconnectTime" : 30000,
           "backoffIncrement": 1000,
           "maxBackoff": 60000,
           "maxNetworkDelay": 10000
        };
      }

      var getInfoPlugin = _.find(config.plugins, { id: "Oskari.mapframework.mapmodule.GetInfoPlugin" });
      if(getInfoPlugin) {
        if(!getInfoPlugin.config) getInfoPlugin.config = {};
        getInfoPlugin.config.ignoredLayerTypes = ["WFS"],
        getInfoPlugin.config.infoBox = false;
      }

      var updatedConfig = JSON.stringify(config);

      var updateQuery = "UPDATE portti_view_bundle_seq SET config='" + updatedConfig + "' " +
        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') " + 
        "AND view_id=" + row.view_id;

      client.query(updateQuery, function(err, res) {
        if(err) throw err;

        updateCount++;
        if(updateCount === rowCount && finished) {
          console.log('Updated ' + updateCount + ' of ' + rowCount + ' rows.');
          console.log('Old wfs found in ' + oldWfsCount + ' rows');
          client.end();
        }
      });
    });

    query.on("end", function(row) {
      finished = true;
    });
  });

}

