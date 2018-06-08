-- set internal flag to true on baselayers for user generated content
UPDATE oskari_maplayer SET internal=true where name in ('oskari:my_places', 'oskari:analysis_data', 'oskari:vuser_layer_data');