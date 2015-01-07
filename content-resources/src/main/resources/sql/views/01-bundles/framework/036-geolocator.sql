INSERT INTO portti_bundle (name, startup) 
       VALUES ('geolocator','{}');

UPDATE portti_bundle set startup = '{
    "title": "Geolocator",
    "bundleinstancename": "geolocator",
    "bundlename": "geolocator",
    "metadata": {
        "Import-Bundle": {
            "rpc": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE name = 'geolocator';