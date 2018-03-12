INSERT INTO portti_bundle (name, startup) 
       VALUES ('admin-users','{}');

UPDATE portti_bundle set startup = '{
    "title" : "Admin - Users",
    "fi" : "admin-users",
    "sv" : "admin-users",
    "en" : "admin-users",
    "bundlename" : "admin-users",
    "bundleinstancename" : "admin-users",
    "metadata" : {
        "Import-Bundle" : {
            "admin-users" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
             }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'admin-users';

UPDATE portti_bundle set config = '{
    "restUrl": "action_route=Users"
}' WHERE name = 'admin-users';
