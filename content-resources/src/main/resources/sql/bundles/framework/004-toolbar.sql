
INSERT INTO portti_bundle (name, startup) 
       VALUES ('toolbar','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Toolbar",
    "fi" : "toolbar",
    "sv" : "toolbar",
    "en" : "toolbar",
    "bundlename" : "toolbar",
    "bundleinstancename" : "toolbar",
    "metadata" : {
        "Import-Bundle" : {
            "toolbar" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'toolbar';
