# Modifying the demo data

The HSQLDB is created on first call of the portlet. It creates a data folder under oskari-server with the populated tables if it doesn't exist
(or the directory the server is started from).

NOTE! Any modifications to the initial data SQL statements dont have any effect if the data dir is not
removed and the server is restarted.

Database recreation can be forced on startup by giving a system.property 'oskari.dropdb' with value 'true':

    mvn clean install -f servlet-map-pom.xml -Doskari.dropdb=true

NOTE! All comment lines need to end with ; character or the next SQL statement will not be run!

The safe SQL file to edit for these is `/oskari-server/servlet-map/src/main/resources/sql/exampleLayersAndRoles.sql`

# Adding a maplayer

Add the following sql at the end of the file to register a new WMS layer in to Oskari-server

    -- Add a layer under 'National Land Survey' layer class;
    INSERT INTO portti_maplayer (layerclassid, wmsname, wmsurl, opacity,
           style, minscale, maxscale, description_link, legend_image, inspire_theme_id,
           dataurl, metadataurl, order_number, layer_type, locale)
    VALUES (3,'maastokartta_50k','http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',40,'',54000,26000,'','',3,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
    '{ fi:{name:"Maastokartta 1:50k",subtitle:""},sv:{name:"Terrängkarta 1:50k",subtitle:""},en:{name:"Topographic map 1:50k",subtitle:""}}');

After registering the layer it isn't visible to the users yet (even admin)

## Adding layer rights for the layer

Add the following sql at the end of the file to make the new layer visible to users by role

Add layer as resource so we can map permissions for it (resource_mapping=wmsurl+wmsname):

    INSERT INTO oskari_resource(resource_type, resource_mapping) values ('maplayer', 'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms+maastokartta_50k');

    -- give view_layer permission for the resource to ROLE 3 (admin);
    INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
    ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '3');

After this you can log in as admin on the browser and see the added layer. Guest or regular user shouldn't see it listed in the layerselector.

Add permission for logged in user

    -- give view_layer permission for the resource to ROLE 2 (logged in user);
    INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
    ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '2');

After this you can log in as a regular user and see the added layer. Guest shouldn't see it listed in the layerselector.

Add permission for guest user

    -- give view_layer permission for the resource to ROLE 2 (logged in user);
    INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
    ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '3');

After this the layer should be listed in layerselector for guests.

# Users management

### 1. Add new users and roles

* edit user.json and roles.json files in resource path `oskari-server/control-example/src/main/resources/users/`

* role ids must match the ones referenced in portti_permission and portti_recource_user tables (see next subsection)

Sample `user.json`

    {
        "users": [
            {
                "id": 2,
                "firstName": "Antti",
                "lastName": "Aalto",
                "uuid": "some-generated-uuid-1",
                "user": "admin",
                "pass": "oskari",
                "roles": [
                    3,
                    4
                ]
            },
            {
                "id": 3,
                "firstName": "Oskari",
                "lastName": "Olematon",
                "uuid": "some-generated-uuid-2",
                "user": "user",
                "pass": "user",
                "roles": [
                    3
                ]
            }
        ]
    }

Sample `role.json`

    {
      "roles": [
        {
            "id": 3,
            "name": "User"
        },
        {
            "id": 4,
            "name": "Admin"
        }
      ]
    }

* Login handling is implemented in `fi.nls.oskari.user.StandaloneUserService`
* The Java class handling user related operations is configurable in `oskari-server/servlet-map/src/main/resources/oskari.properties`

### 2. Add new user roles and permissions

Add a role with an unique id to `role.json`

        {
            "id": 5,
            "name": "MyRole"
        }

Add a user to `user.json` and link the new role to the user

            {
                "id": 3,
                "firstName": "My",
                "lastName": "User",
                "uuid": "some-generated-uuid-3",
                "user": "myuser",
                "pass": "mypass",
                "roles": [
                    5
                ]
            }


Edit script file `oskari-server/servlet-map/src/main/resources/sql/exampleLayersAndRoles.sql`

Add row to oskari_permission table, e.g. (change [YOUR ROLE ID] to 5 as in role.json)

    -- give view_layer permission for the resource to ROLE 2 (logged in user);
    INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
    ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '[YOUR ROLE ID]');

Remove the HSQLDB data directory (or reset the database with oskari.dropdb=true system property when restarting) and restart the server. You should now be able to login with "myuser"/"mypass" and see the layer