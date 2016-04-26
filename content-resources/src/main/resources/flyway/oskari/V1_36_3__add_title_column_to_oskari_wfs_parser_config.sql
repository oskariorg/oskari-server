
--- Column: Add title column for wfs 2 layer parser config

-- ALTER TABLE oskari_wfs_parser_config DROP COLUMN title;

ALTER TABLE oskari_wfs_parser_config ADD COLUMN title text DEFAULT 'Parser';;