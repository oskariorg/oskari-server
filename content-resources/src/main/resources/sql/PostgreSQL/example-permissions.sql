-- give view_layer permission for the resource to guest role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
  ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'Guest'));

-- give view_layer permission for the resource to user role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
  ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', (SELECT id FROM oskari_roles WHERE name = 'User'));

-- give publish permission for the resource to admin role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
  ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'PUBLISH', (SELECT id FROM oskari_roles WHERE name = 'Admin'));

-- give view_published_layer permission for the resource to guest role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
  ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', (SELECT id FROM oskari_roles WHERE name = 'Guest'));

-- give view_published_layer permission for the resource to user role;
INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
  ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_PUBLISHED', (SELECT id FROM oskari_roles WHERE name = 'User'));
