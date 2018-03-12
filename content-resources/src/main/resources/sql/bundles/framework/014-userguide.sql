
INSERT INTO portti_bundle (name, startup) 
       VALUES ('userguide','{}');

UPDATE portti_bundle set startup = '{
    "title" : "User Guide",
    "fi" : "userguide",
    "sv" : "userguide",
    "en" : "userguide",
    "bundlename" : "userguide",
    "bundleinstancename" : "userguide",
    "metadata" : {
        "Import-Bundle" : {
            "userguide" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'userguide';

UPDATE portti_bundle set config = '{
    "tags" : "userguide",
    "flyoutClazz" : "Oskari.mapframework.bundle.userguide.SimpleFlyout"
}' WHERE name = 'userguide';