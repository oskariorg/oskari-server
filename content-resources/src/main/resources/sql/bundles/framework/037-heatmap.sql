INSERT INTO portti_bundle (name, startup) 
       VALUES ('heatmap','{}');

UPDATE portti_bundle set startup = '{
    "title": "Heatmap",
    "bundleinstancename": "heatmap",
    "bundlename": "heatmap",
    "metadata": {
        "Import-Bundle": {
            "heatmap": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE name = 'heatmap';