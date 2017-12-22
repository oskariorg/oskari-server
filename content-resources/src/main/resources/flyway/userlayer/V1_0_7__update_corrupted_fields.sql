/* UserLayerDataService parseFields() have parsed schema/featureType incorrectly for userlayers which geom crs can be mapped to srid code.
 * GeoTools encodeType() adds srid to geom and then parseFields() parsed schema incorrectly.
 * corrupted user_layer fields is like: [{"srid=3067":"avi"},{"String":"name"},{"the_geom":"MultiPolygon"}]
 * Empty corrupted user_layer fields. Front-end doesn't sort properties for corrupted userlayers.
 * */
UPDATE user_layer SET fields = '[]' WHERE fields::TEXT LIKE '%srid=%';
