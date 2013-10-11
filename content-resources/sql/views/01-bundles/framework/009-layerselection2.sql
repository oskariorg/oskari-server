
INSERT INTO portti_bundle (name, startup) 
       VALUES ('layerselection2','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Chosen maplayers",
    "fi" : "layerselection2",
    "sv" : "layerselection2",
    "en" : "layerselection2",
    "bundlename" : "layerselection2",
    "bundleinstancename" : "layerselection2",
    "metadata" : {
        "Import-Bundle" : {
            "layerselection2" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'layerselection2';
