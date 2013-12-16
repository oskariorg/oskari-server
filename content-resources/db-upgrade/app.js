var pg = require("pg");
var config = require("./config");

var client = new pg.Client("postgres://" + 
  config.user + ":" + 
  config.password + "@" + 
  config.host + ":" + 
  config.port + "/"+ 
  config.database
);

var script = process.env.SCRIPT || "";

require("./scripts/" + script)(client);
