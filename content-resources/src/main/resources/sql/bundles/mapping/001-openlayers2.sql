INSERT INTO portti_bundle (name, startup)
  VALUES ('openlayers-default-theme','{}');


UPDATE portti_bundle set startup = '{
    "title" : "OpenLayers",
    "fi" : "OpenLayers",
    "sv" : "OpenLayers",
    "en" : "OpenLayers",
    "bundlename" : "openlayers-default-theme",
    "bundleinstancename" : "openlayers-default-theme",
    "metadata" : {
        "Import-Bundle" : {
            "openlayers-single-full" : {
                "bundlePath" : "/Oskari/packages/openlayers/bundle/"
            },
            "openlayers-default-theme" : {
                "bundlePath" : "/Oskari/packages/openlayers/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
     },
     "instanceProps" : {}
}' WHERE name = 'openlayers-default-theme';
