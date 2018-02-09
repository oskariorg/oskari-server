INSERT INTO portti_bundle (name, startup) 
       VALUES ('rpc','{}');

UPDATE portti_bundle set startup = '{
    "title": "Remote procedure call",
    "bundleinstancename": "rpc",
    "bundlename": "rpc",
    "metadata": {
        "Import-Bundle": {
            "rpc": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE name = 'rpc';