INSERT INTO portti_bundle (name, startup, config) VALUES (
    'myplacesimport','{
    "title" : "myplacesimport",
    "fi" : "myplacesimport",
    "sv" : "myplacesimport",
    "en" : "myplacesimport",
    "bundlename" : "myplacesimport",
    "bundleinstancename" : "myplacesimport",
    "metadata" : {
        "Import-Bundle" : {
            "myplacesimport" : {
                "bundlePath" : "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}', '{
    "name": "MyPlacesImport",
    "sandbox": "sandbox",
    "flyoutClazz": "Oskari.mapframework.bundle.myplacesimport.Flyout"
}');