--------------------------------------------
-- 10. Personal data
--------------------------------------------

-- original config
-- UPDATE portti_view_bundle_seq set config = '{
--    "changeInfoUrl": {
--        "en": "https://www.paikkatietoikkuna.fi/web/en/profile",
--        "fi": "https://www.paikkatietoikkuna.fi/web/fi/profiili",
--        "sv": "https://www.paikkatietoikkuna.fi/web/sv/profil"
--    },
--    "publishedMapUrl": {
--        "en": "/web/en/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=0&p_p_state=exclusive&published=true&viewId=",
--        "fi": "/web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=0&p_p_state=exclusive&published=true&viewId=",
--        "sv": "/web/sv/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=0&p_p_state=exclusive&published=true&viewId="
--    }  
--}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'personaldata') 
--    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "changeInfoUrl": {
        "en": "https://www.paikkatietoikkuna.fi/web/en/profile",
        "fi": "https://www.paikkatietoikkuna.fi/web/fi/profiili",
        "sv": "https://www.paikkatietoikkuna.fi/web/sv/profil"
    },
    "publishedMapUrl": {
        "en": "/published/en/",
        "fi": "/published/fi/",
        "sv": "/published/sv/"
    }
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'personaldata') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');

