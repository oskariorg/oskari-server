var _ = require("lodash-node");

module.exports = function(client) {
  client.connect(function(err) {
    if(err) {
      return console.error('Could not connect to postgres', err);
    }
    var query = client.query(
    	"SELECT id, locale " +
      "FROM oskari_maplayer " +
      "WHERE " +
      "url LIKE '%lounaispaikka%'  order by id"
      );

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    query.on("row", function(row) {
      rowCount++;

        // Fix row locale
        var fixedjsonstr = row.locale;
        fixedjsonstr = fixedjsonstr.replace("fi:", "\"fi\":");
        fixedjsonstr = fixedjsonstr.replace("sv:", "\"sv\":");
        fixedjsonstr = fixedjsonstr.replace("en:", "\"en\":");
        fixedjsonstr = fixedjsonstr.replace("name:", "\"name\":");
        fixedjsonstr = fixedjsonstr.replace("name:", "\"name\":");
        fixedjsonstr = fixedjsonstr.replace("name:", "\"name\":");
        fixedjsonstr = fixedjsonstr.replace("subtitle:", "\"subtitle\":");
        fixedjsonstr = fixedjsonstr.replace("subtitle:", "\"subtitle\":");
        fixedjsonstr = fixedjsonstr.replace("subtitle:", "\"subtitle\":");
        fixedjsonstr = fixedjsonstr.replace("'"," ");

       // console.log("Updated jsonstr: " + fixedjsonstr);

      var locale = {};
      try {
          locale = JSON.parse(fixedjsonstr);
      }
      catch(e) {
          console.error("Unable to parse locale for maplayer " + row.id + ". Error:'", e, "'. Please update manually! locale:\r\n",row.locale);
          updateCount++;
          return;
      }
       // "{ fi:{name:"Satakunnan maakuntakaavan johtokohteet",subtitle:""},sv:{name:"Satakunnan maakuntakaavan johtokohteet",subtitle:""},en:{name:"Satakunnan maakuntakaavan johtokohteet",subtitle:""}}"
      // Add subtitles

        locale.fi.subtitle="Tekninen toteutus: Lounaispaikka";
        locale.sv.subtitle="genom Lounaispaikka";
        locale.en.subtitle="via Lounaispaikka";


      var updatedLocale = JSON.stringify(locale);

      console.log("Updated locale: " + row.id);


      var updateQuery = "UPDATE oskari_maplayer SET locale='" + updatedLocale + "' " +
        "WHERE  id="+row.id;

       // console.log("UpdateQuery: " + updateQuery  );

      client.query(updateQuery, function(err, res) {
       // end management might be invalid
       // if(err) throw err;   works without this - don't know why there is an error after last query row ?


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

