// SCRIPT=update-layer-options-and-params node app.js

var _ = require("lodash-node");

module.exports = function(client) {
  client.connect(function(err) {
    if(err) return console.error('Could not connect to postgres', err);

    var query = client.query(
        "SELECT id, options, params FROM oskari_maplayer"
        + " WHERE groupid = (SELECT id FROM oskari_layergroup WHERE lower(locale) LIKE '%valkeakosken kaupunki%')"
        + " AND type = 'wmslayer'"
    );

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    var changeCount = 0;

    query.on("row", function(row) {
      rowCount++;

      var options, params;

      try {
        options = JSON.parse(row.options) || {};
        params = JSON.parse(row.params) || {};
      } catch(err) {
        return console.error('Error whilst parsing JSON', err);
      }

      options["singleTile"] = true;
      params["format"] = "image/png; 8-bit";

      options = JSON.stringify(options);
      params = JSON.stringify(params);

      var updateQuery = "UPDATE oskari_maplayer"
          + " SET options='" + options + "', params='" + params + "'"
          + " WHERE id=" + row.id;

      client.query(updateQuery, function(err, res) {
          if (err) throw err;

          updateCount++;
          if (updateCount === rowCount && finished) shutdown(client, rowCount);
      });
    });

    query.on("end", function(row) {
      finished = true;
      if (updateCount === rowCount && finished) {
          shutdown(client, rowCount);
      }
    });
  });

  function shutdown(client, count) {
      console.log('===================================');
      console.log('Processed layers: ' + count);
      client.end();
  }
}
