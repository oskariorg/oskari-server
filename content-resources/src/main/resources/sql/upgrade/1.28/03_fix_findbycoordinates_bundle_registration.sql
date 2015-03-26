-- updates the Import-bundle statement from rpc -> findbycoordinates
UPDATE portti_bundle set startup = '{
    "title": "FindByCoordinates",
    "bundleinstancename": "findbycoordinates",
    "bundlename": "findbycoordinates",
    "metadata": {
        "Import-Bundle": {
            "findbycoordinates": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE name = 'findbycoordinates';