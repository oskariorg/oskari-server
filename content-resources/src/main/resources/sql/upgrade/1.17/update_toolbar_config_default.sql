-------------------------------------------------
-- from 01-create-default-view.sql line:227
-------------------------------------------------
-- UPDATE portti_view_bundle_seq set config = '{
--        "viewtools": {
--            "print" : false,
--            "link" : false
--        },
--        "basictools": {
--            "measureline" : false,
--            "measurearea" : false
--        }
--    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar')
--             AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update just basictools: measureline & area
-- from toolbar confs for logged in user
UPDATE portti_view_bundle_seq set config = '{
    "viewtools": {
        "print": false
    },
    "basictools": {
        "measureline" : false,
        "measurearea" : false
    },
    "mapUrlPrefix": {
        "en": "http://www.paikkatietoikkuna.fi/web/en/map-window?",
        "fi": "http://www.paikkatietoikkuna.fi/web/fi/kartta?",
        "sv": "http://www.paikkatietoikkuna.fi/web/sv/kartfonstret?"
    }
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'toolbar')
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');
