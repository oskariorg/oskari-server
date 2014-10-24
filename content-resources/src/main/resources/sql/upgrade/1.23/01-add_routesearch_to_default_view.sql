--------------------------------------------
-- 24. Route Search
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'routesearch'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title": "Route Search",
    "bundleinstancename": "routesearch",
    "fi": "Reittihaku",
    "sv": "Ruttsök",
    "en": "Route Search",
    "bundlename": "routesearch",
    "metadata": {
        "Import-Bundle": {
            "routesearch": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'routesearch') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "flyoutClazz": "Oskari.mapframework.bundle.routesearch.Flyout"
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'routesearch') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');