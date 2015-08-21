
INSERT INTO portti_bundle (name, startup)
VALUES ('metrics','{}');

UPDATE portti_bundle set startup = '{
    "title": "Admin metrics panel",
    "bundleinstancename": "metrics",
    "bundlename": "metrics",
    "metadata": {
        "Import-Bundle": {
            "metrics": {
                "bundlePath": "/Oskari/packages/admin/bundle/"
            }
        }
    }
}' WHERE name = 'metrics';