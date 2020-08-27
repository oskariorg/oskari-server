

ALTER TABLE oskari_jaas_users RENAME TO oskari_users_credentials;
ALTER TABLE oskari_role_oskari_user RENAME TO oskari_users_roles;

ALTER TABLE oskari_user_indicator RENAME TO oskari_statistical_indicator;

ALTER TABLE oskari_statistical_indicator
    ALTER COLUMN title TYPE TEXT,
    ALTER COLUMN source TYPE TEXT,
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE oskari_user_indicator_data RENAME TO oskari_statistical_indicator_data;