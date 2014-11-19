var _ = require("lodash-node");

module.exports = function(client) {
    client.connect(function(err) {
        if(err) {
            return console.error('Could not connect to postgres', err);
        }
        var query = client.query("SELECT id FROM portti_view");

        var rowCount = 0;
        var updateCount = 0;
        var finished = false;
        query.on("row", function(row) {
            rowCount++;
            // generate uuids for views that don't have them
            var updateQuery = "UPDATE portti_view SET uuid=(SELECT uuid_in(md5(random()::text || now()::text)::cstring)) WHERE" +
                " id =" + row.id;

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

/**
 * Check that there is no duplicates:
 SELECT count(uuid) as c, uuid
 FROM portti_view group by uuid order by c desc
*/