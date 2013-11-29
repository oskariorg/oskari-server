
-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='PUBLISH'), 
    (SELECT id FROM portti_bundle WHERE name = 'toolbar'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='PUBLISH')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "Toolbar",
        "fi" : "toolbar",
        "sv" : "toolbar",
        "en" : "toolbar",
        "bundlename" : "toolbar",
        "bundleinstancename" : "toolbar",
        "metadata" : {
            "Import-Bundle" : {
                "toolbar" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                 }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='PUBLISH');

-- update config so all buttons are disabled by default
UPDATE portti_view_bundle_seq set config =
'{
    "basictools": {
        "measurearea": false,
        "measureline": false,
        "select": false,
        "zoombox": false

    },
    "history": {
        "history_back": false,
        "history_forward": false,
        "reset": false
    },
    "viewtools": {
        "link": false,
        "print": false
    }
}'
    WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar') 
    AND  view_id=(SELECT id FROM portti_view WHERE type='PUBLISH');