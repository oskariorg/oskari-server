-- default values have changed and are now in code, no need to use db for defaults;
UPDATE portti_bundle set config = '{}' WHERE name = 'statsgrid';