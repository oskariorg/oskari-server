
INSERT INTO portti_bundle (name, startup) 
       VALUES ('admin','{}');

UPDATE portti_bundle set startup = '{
    "title": "Generic Admin",
    "bundleinstancename": "admin",
    "bundlename": "admin",
    "metadata": {
        "Import-Bundle": {
            "admin": {
                "bundlePath": "/Oskari/packages/admin/bundle/"
            }
        }
    }
}' WHERE name = 'admin';
