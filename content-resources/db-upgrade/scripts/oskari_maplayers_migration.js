var _ = require("lodash-node");

module.exports = function(client) {


  client.connect(function(err) {
    if(err) {
      return console.error('could not connect to postgres', err);
    }

    // 1. clear any previous migrations, db constraints will cascade on theme links and maplayers
    var query = "DELETE FROM oskari_layergroup;";
    runSQL(client, query, copyLayerGroups, 'could not clear previous migration!');
  });

    // 2. Create layer groups (organisations)
    function copyLayerGroups(client) {
        var query = "INSERT INTO oskari_layergroup (id, locale) SELECT id, locale FROM portti_layerclass WHERE parent IS NULL";
        runSQL(client, query, copyNormalLayers, 'could not copy layer groups');
    }

    // 3. Copy independent/normal layers (that are not sublayers)
    function copyNormalLayers(client) {
        var selectSQL = "SELECT id, -1 AS parentId, layer_type, false AS base_map, layerclassid AS groupId, wmsname, wmsurl, " +
            "locale, opacity, '' AS style, minscale, maxscale, legend_image, dataurl, " +
            "tile_matrix_set_id, tile_matrix_set_data, gfi_type, xslt, " +
            "created, updated " +
            "FROM portti_maplayer WHERE layerclassid IN (SELECT id FROM oskari_layergroup);";

        var insertSQL = "INSERT INTO oskari_maplayer(" +
                "id, parentid, type, base_map, groupid, name, url," +
                "locale, opacity, style, minscale, maxscale, legend_image, metadataid," +
                "tile_matrix_set_id, tile_matrix_set_data, gfi_type, gfi_xslt, " +
                "created, updated) ";

        runSQL(client, insertSQL + selectSQL, copySubLayers, 'could not copy normal layers');

    }

    // 4. copy layers that are sublayers
    function copySubLayers(client) {

        // NOTE! sublayers will have groupId=1 and parentId as layerclassid
        var selectSQL = "SELECT id, layerclassid AS parentId, layer_type, false AS base_map, 1 AS groupId, wmsname, wmsurl, " +
            "locale, opacity, style, minscale, maxscale, legend_image, dataurl, " +
            "tile_matrix_set_id, tile_matrix_set_data, gfi_type, xslt, " +
            "created, updated " +
            "FROM portti_maplayer WHERE layerclassid NOT IN (SELECT id FROM oskari_layergroup);";

        var insertSQL = "INSERT INTO oskari_maplayer(" +
            "id, parentid, type, base_map, groupid, name, url," +
            "locale, opacity, style, minscale, maxscale, legend_image, metadataid," +
            "tile_matrix_set_id, tile_matrix_set_data, gfi_type, gfi_xslt, " +
            "created, updated) ";

        runSQL(client, insertSQL + selectSQL, fixIdSequence, 'could not copy sublayers');
    }

    // 5. update serial column sequence value since we inserted ids manually!!
    function fixIdSequence(client) {
        var fixSequenceSQL = "SELECT setval(pg_get_serial_sequence('oskari_maplayer', 'id'), (SELECT MAX(id) FROM oskari_maplayer));";

        runSQL(client, fixSequenceSQL, createNewBaseLayers, 'could not fix sequence for oskari_maplayer.id');
    }


    // 6. establish new rows to oskari_maplayers from portti_layerclass base/group layers
    function createNewBaseLayers(client) {

        var selectSQL = "SELECT -1 AS parentId, id AS externalId, 'collection' AS type, NOT group_map AS base_map, parent AS groupId, '' AS name, '' AS url, " +
            "locale, 100 AS opacity, '' AS style, -1 AS minscale, -1 AS maxscale, legend_image, dataurl AS metadataId, " +
            "'' AS tile_matrix_set_id, '' AS tile_matrix_set_data, '' AS gfi_type, '' AS gfi_xslt " +
            "FROM portti_layerclass WHERE parent IS NOT NULL";

        var insertSQL = "INSERT INTO oskari_maplayer(" +
            "parentid, externalId, type, base_map, groupid, name, url," +
            "locale, opacity, style, minscale, maxscale, legend_image, metadataid," +
            "tile_matrix_set_id, tile_matrix_set_data, gfi_type, gfi_xslt) ";

        runSQL(client, insertSQL + selectSQL, linkSublayers, 'could not establish new collection layers');
    }

    // 7. link sublayers to new baselayers
    function linkSublayers(client) {
        var linkSQL = "UPDATE oskari_maplayer SET parentId = m.id FROM oskari_maplayer m " +
            "WHERE oskari_maplayer.parentId != -1 AND oskari_maplayer.parentId = m.externalId::integer"

        runSQL(client, linkSQL, updateExternalIds, 'could not link sublayers');
    }

    // 8. update externalId with base_ prefix
    function updateExternalIds(client) {
        var prefixSQL = "UPDATE oskari_maplayer SET externalId = 'base_' || externalId " +
            "WHERE externalId IS NOT NULL";

        runSQL(client, prefixSQL, linkInspireThemes, 'could not prefix external ids');
    }

    // 9. link themes from old
    function linkInspireThemes(client) {
        var query = "INSERT INTO oskari_maplayer_themes (maplayerid, themeid) SELECT id, inspire_theme_id FROM portti_maplayer";

        runSQL(client, query, updateStylesForWMS, 'could not link inspire themes');
    }

    // 10. update default styles for wms layers
    // TODO: check if wmts needs this!!
    function updateStylesForWMS(client) {
        var updateSQL = "UPDATE oskari_maplayer SET style = substr(m.style, 1, 100) FROM portti_maplayer m " +
            "WHERE oskari_maplayer.id = m.id AND m.layer_type = 'wmslayer'"

        runSQL(client, updateSQL, allDone, 'could not update default styles for layers');
    }

    // 11. all done
    function allDone(client) {
        console.log("Upgrade complete, you can now remove portti_maplayer and portti_layerclass tables from database");
        client.end();
    }

    function runSQL(client, sql, callback, errorMsg) {

        client.query(sql, function(err) {
            if(err) {
                return console.error(errorMsg, err);
            }
            callback(client);
        });
    }
}

