-- Set internal flag to true on baselayers for user generated content
-- This affects existing installs - any new install will automatically setup the layers as internal through the appropriate flyway modules
UPDATE oskari_maplayer SET internal=true where name in ('oskari:my_places', 'oskari:analysis_data', 'oskari:vuser_layer_data');