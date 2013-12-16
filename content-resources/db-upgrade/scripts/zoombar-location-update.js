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
      "bundleinstance = 'mapfull' ORDER BY id");

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;
      var config = JSON.parse(row.config);

      config.layout = "default";


      var zoombar = _.find(config.plugins, { id: "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" });
      if(zoombar) {
        if(zoombar.config.location && !zoombar.config.location.classes) {
          zoombar.config.location = {
            classes: "top left"
          };
          console.log("Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar:", row.view_id);
        } else {
          console.log("no change.Portti2Zoombar:", row.view_id);
        }
      }

       var panbuttons = _.find(config.plugins, { id: "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" });
      if(panbuttons) {
        if(panbuttons.config.location && !panbuttons.config.location.classes) {
          panbuttons.config.location = {
            classes: "top left"
          };
          console.log("Oskari.mapframework.bundle.mapmodule.plugin.PanButtons:", row.view_id);
        } else {
          console.log("no change PanButtons:", row.view_id);
        }
      }

      var search = _.find(config.plugins, { id: "Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin" });
      if(search) {
        if(!search.config) {
          search.config = { 
            location: {
              classes: "top right"
            }
          };
          console.log("Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin:", row.view_id);
        } else if(search.config && !search.config.location) {
          search.config.location = {
            classes: "top right"
          };
          console.log("Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin:", row.view_id);
        } else {
          console.log("no change SearchPlugin:", row.view_id);
        }
      }

      var layersel = _.find(config.plugins, { id: "Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin" });
      if(layersel) {
        if(!layersel.config) {
          layersel.config = { 
            location: {
              classes: "top right"
            }
          };
          console.log("Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin:", row.view_id);
        } else if(layersel.config && !layersel.config.location) {
          layersel.config.location = {
            classes: "top right"
          };
          console.log("Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin:", row.view_id);
        } else {
          console.log("no change LayerSelectionPlugin:", row.view_id);
        }
      }

      var updatedConfig = JSON.stringify(config);

      var updateQuery = "UPDATE portti_view_bundle_seq SET config='" + updatedConfig + "' " +
        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') " + 
        "AND view_id=" + row.view_id;

      client.query(updateQuery, function(err, res) {
        if(err) throw err;

        updateCount++;
        if(updateCount === rowCount && finished)
          client.end();
      });
   
    });

    query.on("end", function(row) {
      finished = true;
    });
  });

}

