INSERT INTO portti_bundle (name, startup)
  VALUES ('ol3','{}');


UPDATE portti_bundle set startup = '{
    "title" : "OpenLayers 3",
    "bundlename" : "ol3",
    "bundleinstancename" : "ol3",
    "metadata" : {
        "Import-Bundle" : {
            "ol3" : {
                "bundlePath" : "/Oskari/packages/ol3/bundle/"
            },
            "oskariui" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        }
     }
}' WHERE name = 'ol3';
