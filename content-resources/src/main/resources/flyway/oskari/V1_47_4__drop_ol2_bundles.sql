
-- OpenLayers 2 library bundle
DELETE FROM portti_bundle WHERE name = 'openlayers-default-theme';
-- OpenLayers 2 implementation of myplaces
DELETE FROM portti_bundle WHERE name = 'myplaces2';

-- Old statsgrid bundles published version - current uses same bundle for geoportal and embedded maps
DELETE FROM portti_bundle WHERE name = 'publishedgrid';
-- Old version of publisher that is no longer maintained
DELETE FROM portti_bundle WHERE name = 'publisher';

-- Old bundles from parcel application that have not been maintained for years
DELETE FROM portti_bundle WHERE name = 'parcel';
DELETE FROM portti_bundle WHERE name = 'parcelinfo';
DELETE FROM portti_bundle WHERE name = 'parcelselector';

