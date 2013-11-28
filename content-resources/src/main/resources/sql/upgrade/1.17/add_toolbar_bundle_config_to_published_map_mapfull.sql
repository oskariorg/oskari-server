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