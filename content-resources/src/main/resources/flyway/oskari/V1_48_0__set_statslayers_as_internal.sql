-- Set internal flag to true on statslayers/statistical regionsets
-- This affects existing installs - any new install SHOULD setup the layers as internal through the appropriate flyway modules
UPDATE oskari_maplayer SET internal=true where type='statslayer';