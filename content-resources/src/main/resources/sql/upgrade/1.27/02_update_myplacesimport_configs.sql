-- update proper config for views
UPDATE portti_view_bundle_seq set config = '{
    "name": "MyPlacesImport",
    "sandbox": "sandbox",
    "flyoutClazz": "Oskari.mapframework.bundle.myplacesimport.Flyout",
	"maxFileSizeMb": 10
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'myplacesimport');

-- update proper config for bundle
UPDATE portti_bundle set config = '{
    "name": "MyPlacesImport",
    "sandbox": "sandbox",
    "flyoutClazz": "Oskari.mapframework.bundle.myplacesimport.Flyout",
	"maxFileSizeMb": 10
}' WHERE name = 'myplacesimport';
