--------------------------------------------
-- 22. Promote - Analyse
--    NOTE! Check that seqno in WHERE matches the correct promote
--------------------------------------------

-- add bundle to view
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance) 
    VALUES ([VIEW_ID], 
    (SELECT id FROM portti_bundle WHERE name = 'promote'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = [VIEW_ID]), 
    '{}','{}', '{}', 'analyse');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "Analyse",
        "fi" : "Analyysi",
        "sv" : "Analys",
        "en" : "Analyse",
        "bundlename" : "promote",
        "bundleinstancename" : "analyse",
        "metadata" : {
            "Import-Bundle" : {
                "promote" : {
                    "bundlePath" : "/Oskari/packages/framework/bundle/"
                }
            },
            "Require-Bundle-Instance" : []
        },
        "instanceProps" : {}
    }' WHERE bundle_id = (SELECT max(id) FROM portti_bundle WHERE name = 'promote')
    AND seqno = 22
    AND view_id=[VIEW_ID];

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
        "__name": "Analyse",
        "title": {
            "en": "Analyse",
            "fi": "Analyysi",
            "sv": "Analys"
        },
        "desc": {
            "en": "You need to log in before using the embedding function.",
            "fi": "Voit käyttää Analyysitoimintoa kirjauduttuasi palveluun.",
            "sv": "Logga in i tjänsten för att använda analys funktioner."
        },
        "signup": {
            "en": "Log in",
            "fi": "Kirjaudu sisään",
            "sv": "Logga in"
        },
        "signupUrl": {
            "en": "/web/en/login",
            "fi": "/web/fi/login",
            "sv": "/web/sv/login"
        },
        "register": {
            "en": "Register",
            "fi": "Rekisteröidy",
            "sv": "Registrera dig"
        },
        "registerUrl": {
            "en": "/web/en/login?p_p_id=58&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account",
            "fi": "/web/fi/login?p_p_id=58&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account",
            "sv": "/web/sv/login?p_p_id=58&p_p_lifecycle=1&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account"
        }
    }' WHERE bundle_id = (SELECT max(id) FROM portti_bundle WHERE name = 'promote') 
    AND seqno = 22
    AND view_id=[VIEW_ID];
