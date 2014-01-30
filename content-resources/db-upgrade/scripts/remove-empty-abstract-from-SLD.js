var _ = require("lodash-node");

module.exports = function(client) {
  client.connect(function(err) {
    if(err) {
      return console.error('Could not connect to postgres', err);
    }
    var query = client.query(
    	"SELECT id, name, sld_style FROM portti_wfs_layer_style"
    );

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;

      var style = row.sld_style;
      try {
          style = style.replace("<Abstract/>","");
          style = style.replace("<Abstract />","");
          style = style.replace("<Abstract></Abstract>","");
      }
      catch(e) {
          console.error("Unable to parse sld for wfs-layer style " + row.id + ". Error:'", e, "'. Please update manually! SLD:\r\n",row.sld_style);
          updateCount++;
          return;
      }

      var updateQuery = "UPDATE portti_wfs_layer_style SET sld_style='" + style + "' " +
        "WHERE id = " + row.id;

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

