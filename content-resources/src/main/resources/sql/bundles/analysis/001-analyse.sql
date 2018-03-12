
INSERT INTO portti_bundle (name, startup) 
       VALUES ('analyse','{}');


UPDATE portti_bundle set startup = '{
    "title": "Analysis UI",
    "bundleinstancename": "analyse",
    "fi": "analyysi",
    "sv": "analys",
    "en": "analyse",
    "bundlename": "analyse",
    "metadata": {
        "Import-Bundle": {
            "analyse": {
                "bundlePath": "/Oskari/packages/analysis/bundle/"
            },
            "geometryeditor": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE name = 'analyse';
