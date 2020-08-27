

ALTER TABLE oskari_jaas_users RENAME TO oskari_users_credentials;
ALTER TABLE oskari_role_oskari_user RENAME TO oskari_users_roles;

ALTER TABLE oskari_user_indicator RENAME TO oskari_statistical_indicator;

ALTER TABLE oskari_statistical_indicator
    ALTER COLUMN title TYPE TEXT,
    ALTER COLUMN source TYPE TEXT,
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE oskari_user_indicator_data RENAME TO oskari_statistical_indicator_data;

ALTER TABLE ratings RENAME TO oskari_ratings;

ALTER TABLE oskari_ratings
    ALTER COLUMN category TYPE TEXT,
    ALTER COLUMN categoryitem TYPE TEXT,
    ALTER COLUMN comment TYPE TEXT;

ALTER TABLE oskari_permission RENAME TO oskari_resource_permission;

ALTER TABLE oskari_resource_permission
    RENAME COLUMN oskari_resource_id TO resource_id;

ALTER TABLE oskari_role_external_mapping RENAME TO oskari_roles_external_mapping;

ALTER TABLE oskari_statistical_layer RENAME TO oskari_statistical_datasource_regionsets;
