
INSERT INTO portti_bundle (name, startup) 
       VALUES ('myplaces2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "My places",
    "fi" : "Kohteet",
    "sv" : "Platsar",
    "en" : "Places",
    "bundlename" : "myplaces2",
    "bundleinstancename" : "myplaces2",
    "metadata" : {
        "Import-Bundle" : {
            "myplaces2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'myplaces2';

UPDATE portti_bundle SET config = '{
  "queryUrl" : "[REPLACED BY HANDLER]",
  "featureNS" : "http://www.oskari.org",
  "layerDefaults" : {
    "wmsName" : "oskari:my_places_categories"
  },
  "wmsUrl" : "/karttatiili/myplaces?myCat="
}' WHERE name = 'myplaces2'
