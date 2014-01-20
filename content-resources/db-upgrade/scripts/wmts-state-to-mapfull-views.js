var _ = require("lodash-node");

module.exports = function(client) {
  client.connect(function(err) {
    if(err) {
      return console.error('Could not connect to postgres', err);
    }
    var query = client.query(
    	"SELECT view_id, state " +
      "FROM portti_view_bundle_seq " +
      "WHERE " +
      "bundle_id = (SELECT id FROM portti_bundle WHERE name='mapfull') ORDER BY view_id"
    );

    var rowCount = 0;
    var updateCount = 0;
    var successCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;

      var state = {};
      try {
          state = JSON.parse(row.state);
      }
      catch(e) {
          console.error("Unable to parse state for view " + row.view_id + ". Error:'", e, "'. Please update manually! State:\r\n",row.state);
          updateCount++;
          return;
      }

      // The new WMTS layers have more scales than the WMS layers.
      // Add 3 to the zoom level so that it's about the same as before.
      state.zoom = (state.zoom || 0) + 3;

      var updatedState = JSON.stringify(state);

      var updateQuery = "UPDATE portti_view_bundle_seq SET state='" + updatedState + "' " +
        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') " + 
        "AND view_id=" + row.view_id;

      client.query(updateQuery, function(err, res) {
        if(err) throw err;

        updateCount++;
        successCount++;
        if((updateCount === rowCount) && finished) {
          console.log(successCount + ' of ' + rowCount + ' rows successfully updated');
          client.end();
        }
      });
    });

    query.on("end", function(row) {
      finished = true;
    });
  });

}

