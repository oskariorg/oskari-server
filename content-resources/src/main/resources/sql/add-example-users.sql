INSERT INTO oskari_users(user_name, first_name, last_name, uuid) VALUES('admin', 'Antti', 'Aalto', 'asdf-asdf-asdf-asdf-asdf');
INSERT INTO oskari_users(user_name, first_name, last_name, uuid) VALUES('user', 'Oskari', 'Olematon', 'fdsa-fdsa-fdsa-fdsa-fdsa');

INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'admin'), (SELECT id FROM oskari_roles WHERE name = 'User'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'admin'), (SELECT id FROM oskari_roles WHERE name = 'Admin'));
INSERT INTO oskari_role_oskari_user(user_id, role_id) VALUES((SELECT id FROM oskari_users WHERE user_name = 'user'), (SELECT id FROM oskari_roles WHERE name = 'User'));
