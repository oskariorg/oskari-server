--------------------------------------------
-- 22. Analyse
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
       VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
        (SELECT id FROM portti_bundle WHERE name = 'analyse'), 
        (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
        '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
    "title": "Analyse",
    "bundleinstancename": "analyse",
    "fi": "analyse",
    "sv": "analyse",
    "en": "analyse",
    "bundlename": "analyse",
    "metadata": {
        "Import-Bundle": {
            "analyse": {
                "bundlePath": "/Oskari/packages/analysis/bundle/"
            }
        },
        "Require-Bundle-Instance": [ ]
    },
    "instanceProps": {}
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'analyse') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');