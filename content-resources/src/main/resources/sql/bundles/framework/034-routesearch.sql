INSERT INTO portti_bundle (name, startup) 
       VALUES ('routesearch','{}');

UPDATE portti_bundle set startup = '{
    "instanceProps": {},
    "title": "Route Search",
    "bundleinstancename": "routesearch",
    "fi": "Reittihaku",
    "sv": "Ruttsök",
    "en": "Route Search",
    "bundlename": "routesearch",
    "metadata": {
        "Import-Bundle": {
            "routesearch": {
                "bundlePath": "/Oskari/packages/paikkatietoikkuna/bundle/"
            }
        },
        "Require-Bundle-Instance": [

        ]
    }
}' WHERE name = 'routesearch';

UPDATE portti_bundle set config = '{
    "flyoutClazz": "Oskari.mapframework.bundle.routesearch.Flyout"
}' WHERE name = 'routesearch';