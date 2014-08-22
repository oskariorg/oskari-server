INSERT INTO oskari_users(user_name, first_name, last_name, uuid) VALUES('admin', 'Antti', 'Aalto', 'asdf-asdf-asdf-asdf-asdf');
INSERT INTO oskari_users(user_name, first_name, last_name, uuid) VALUES('Arcticsdi', 'Arctic', 'sdi', '29d9-11e4-8c21-c9a6-asdi');

INSERT INTO oskari_roles(name, is_guest) VALUES('Guest', TRUE);
INSERT INTO oskari_roles(name) VALUES('Arcticsdi');
INSERT INTO oskari_roles(name) VALUES('Admin');

INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'admin'), (SELECT id FROM oskari_roles WHERE name = 'Arcticsdi'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'admin'), (SELECT id FROM oskari_roles WHERE name = 'Admin'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'Arcticsdi'), (SELECT id FROM oskari_roles WHERE name = 'Arcticsdi'));
