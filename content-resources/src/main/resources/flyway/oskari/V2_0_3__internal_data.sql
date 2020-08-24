-- Initial dataprovider for user generated data (myplaces etc) baselayers to use
INSERT INTO oskari_dataprovider (locale) VALUES ('{ "en":{ "name":"Oskari.org"} }');

-- Initial roles for the database
INSERT INTO oskari_roles (name, is_guest) VALUES ('Guest', true);
INSERT INTO oskari_roles (name, is_guest) VALUES ('User', false);
INSERT INTO oskari_roles (name, is_guest) VALUES ('Admin', false);
