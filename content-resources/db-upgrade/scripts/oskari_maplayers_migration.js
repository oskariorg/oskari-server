var _ = require("lodash-node");

module.exports = function(client) {


  client.connect(function(err) {
    if(err) {
      return console.error('could not connect to postgres', err);
    }

    // 1. clear any previous migrations, db constraints will cascade on theme links and maplayers
      //
    var query = "DELETE FROM oskari_layergroup; DELETE FROM oskari_resource WHERE resource_mapping LIKE '%_migrated+collection';";
    runSQL(client, query, copyLayerGroups, 'could not clear previous migration!');
  });

    // 2. Create layer groups (organisations)
    function copyLayerGroups(client) {
        var query = "INSERT INTO oskari_layergroup (id, locale) SELECT id, locale FROM portti_layerclass WHERE parent IS NULL";
        runSQL(client, query, fixGroupIdSequence, 'could not copy layer groups');
    }

    // 3. update serial column sequence value since we inserted ids manually!!
    function fixGroupIdSequence(client) {
        var fixSequenceSQL = "SELECT setval(pg_get_serial_sequence('oskari_layergroup', 'id'), (SELECT MAX(id) FROM oskari_layergroup));";

        runSQL(client, fixSequenceSQL, copyNormalLayers, 'could not fix sequence for oskari_layergroup.id');
    }

    // 4. Copy independent/normal layers (that are not sublayers)
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

    // 5. copy layers that are sublayers
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

    // 6. update serial column sequence value since we inserted ids manually!!
    function fixIdSequence(client) {
        var fixSequenceSQL = "SELECT setval(pg_get_serial_sequence('oskari_maplayer', 'id'), (SELECT MAX(id) FROM oskari_maplayer));";

        runSQL(client, fixSequenceSQL, createNewBaseLayers, 'could not fix sequence for oskari_maplayer.id');
    }


    // 7. establish new rows to oskari_maplayers from portti_layerclass base/group layers
    function createNewBaseLayers(client) {

        var selectSQL = "SELECT -1 AS parentId, id AS externalId, 'collection' AS type, NOT group_map AS base_map, parent AS groupId, " +
            "id || '_migrated' AS name, 'collection' AS url, " +
            "locale, 100 AS opacity, '' AS style, -1 AS minscale, -1 AS maxscale, legend_image, dataurl AS metadataId, " +
            "'' AS tile_matrix_set_id, '' AS tile_matrix_set_data, '' AS gfi_type, '' AS gfi_xslt " +
            "FROM portti_layerclass WHERE parent IS NOT NULL";

        var insertSQL = "INSERT INTO oskari_maplayer(" +
            "parentid, externalId, type, base_map, groupid, name, url," +
            "locale, opacity, style, minscale, maxscale, legend_image, metadataid," +
            "tile_matrix_set_id, tile_matrix_set_data, gfi_type, gfi_xslt) ";

        runSQL(client, insertSQL + selectSQL, linkSublayers, 'could not establish new collection layers');
    }

    // 8. link sublayers to new baselayers
    function linkSublayers(client) {
        var linkSQL = "UPDATE oskari_maplayer SET parentId = m.id FROM oskari_maplayer m " +
            "WHERE oskari_maplayer.parentId != -1 AND oskari_maplayer.parentId = m.externalId::integer"

        //runSQL(client, linkSQL, updateExternalIds, 'could not link sublayers');
        runSQL(client, linkSQL, copyBaseLayerPermissions, 'could not link sublayers');
    }

    // 8.5 setup base/group layer permissions
    function copyBaseLayerPermissions(client) {

        var selectGroupsSQL = "SELECT id, name, url, externalId FROM oskari_maplayer WHERE type='collection'";

        client.query(selectGroupsSQL, function(err, groupLayers) {
            if(err) {
                return console.error("Couldn't find collection layers", err);
            }
            console.log('got collection layers', groupLayers.rows.length);
            var count = 0;
            var resources = [];
            function permissionsCopied(err) {
                /*
                if(err) {
                    // if previous layer had no permissions -> an error will occur,
                    // so skipping any errors since this _should_ work :)
                    console.log("Permissions with resource ids:", resources);
                    return console.error("Couldn't insert permissions for resource", err);
                }*/
                count++;
                // after all sqls executed -> go to next step
                if(count == groupLayers.rows.length) {
                    console.log("Inserted new resources/permissions with resource ids:", resources);
                    updateExternalIds(client);
                }
            }
            _.forEach(groupLayers.rows, function(layer) {
                //console.log('Handling layer:', layer);
                // insert as new resources
                var insertResource = "INSERT INTO oskari_resource (resource_type, resource_mapping) " +
                    "VALUES ('maplayer', '" + layer.url + "+" + layer.name + "') " +
                    "RETURNING ID;";

                client.query(insertResource, function(err, insertResult) {
                    if(err) {
                        count++;
                        return console.error("Couldn't insert grouplayer as resource", layer, err);
                    }
                    var resourceId = insertResult.rows[0].id;
                    resources.push(resourceId);
                    // copy permissions from matching layerclass
                    var copyPermissionsSQL = "INSERT INTO oskari_permission (oskari_resource_id, external_type, permission, external_id) " +
                        "SELECT " + resourceId + ", p.external_type, p.permission, p.external_id FROM oskari_resource r, oskari_permission p " +
                        "WHERE r.id = p.oskari_resource_id AND r.resource_type='layerclass' AND r.resource_mapping = 'BASE+" + layer.externalid + "'";
                    runSQL(client, copyPermissionsSQL, permissionsCopied, 'Could not copy permissions for layer: ' + JSON.stringify(layer));
                });
            });
        });
    }

    // 9. update externalId with base_ prefix
    function updateExternalIds(client) {
        var prefixSQL = "UPDATE oskari_maplayer SET externalId = 'base_' || externalId " +
            "WHERE externalId IS NOT NULL";

        runSQL(client, prefixSQL, linkInspireThemesForNormalLayers, 'could not prefix external ids');
    }

    // 10. link themes from old db table for layers that exist there (non baselayers)
    // TODO: check collection layers inspire themes!
    function linkInspireThemesForNormalLayers(client) {
        var query = "INSERT INTO oskari_maplayer_themes (maplayerid, themeid) SELECT id, inspire_theme_id FROM portti_maplayer";

        runSQL(client, query, linkInspireThemesForCollectionLayers, 'could not link inspire themes');
    }

    // 11. Add inspire theme links to base/grouplayers
    function linkInspireThemesForCollectionLayers(client) {

        var selectSQL = "SELECT DISTINCT m1.id AS baseId, t.themeid FROM oskari_maplayer m1, oskari_maplayer m2, " +
        "oskari_maplayer_themes t WHERE m1.id = m2.parentId AND t.maplayerid = m2.id;"

        var linkSql = "INSERT INTO oskari_maplayer_themes (maplayerid, themeid) ";

        runSQL(client, linkSql + selectSQL, updateStylesForWMS, 'could not link inspire themes');
    }

    // 12. update default styles for wms layers
    // TODO: check if wmts needs this!!
    function updateStylesForWMS(client) {
        var updateSQL = "UPDATE oskari_maplayer SET style = substr(m.style, 1, 100) FROM portti_maplayer m " +
            "WHERE oskari_maplayer.id = m.id AND m.layer_type = 'wmslayer'"

        runSQL(client, updateSQL, allDone, 'could not update default styles for layers');
    }

    // 13. all done
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

