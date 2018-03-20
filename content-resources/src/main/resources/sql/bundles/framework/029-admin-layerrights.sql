
INSERT INTO portti_bundle (name, startup) 
       VALUES ('admin-layerrights','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Admin - Layer rights",
    "fi" : "admin-layerrights",
    "sv" : "admin-layerrights",
    "en" : "admin-layerrights",
    "bundlename" : "admin-layerrights",
    "bundleinstancename" : "admin-layerrights",
    "metadata" : {
        "Import-Bundle" : {
            "admin-layerrights" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'admin-layerrights';
