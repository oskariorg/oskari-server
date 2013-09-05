# Modifying the demo data

The HSQLDB is created on first call of the portlet. It creates a data folder under oskari-server with the populated tables if it doesn't exist
(or the directory the server is started from).

NOTE! Any modifications to the initial data SQL statements dont have any effect if the data dir is not
removed and the server is restarted.

NOTE! All comment lines need to end with ; character or the next SQL statement will not be run!

The safe SQL file to edit for these is ´/oskari-server/servlet-map/src/main/resources/fi/nls/oskari/map/servlet/db/exampleLayersAndRoles.sql´

# Adding a maplayer

Add the following sql at the end of the file to register a new WMS layer in to Oskari-server

    -- Add a layer under 'National Land Survey' layer class;
    INSERT INTO portti_maplayer (layerclassid, namefi, namesv, nameen, wmsname, wmsurl, opacity,
           style, minscale, maxscale, description_link, legend_image, inspire_theme_id,
           dataurl, metadataurl, order_number, layer_type, locale)
    VALUES (3,'Maastokartta 1:50k','Terrängkarta 1:50k','Topographic map 1:50k','maastokartta_50k','http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',40,'',54000,26000,'','',2,'c22da116-5095-4878-bb04-dd7db3a1a341','',30,'wmslayer',
    '{ fi:{name:"Maastokartta 1:50k",subtitle:""},sv:{name:"Terrängkarta 1:50k",subtitle:""},en:{name:"Topographic map 1:50k",subtitle:""}}');

After registering the layer it isn't visible to the users yet (even admin)

## Adding layer rights for the layer

Add the following sql at the end of the file to make the new layer visible to users by role

Add permission for admin user:

    INSERT INTO portti_resource_user (resource_name, resource_namespace, resource_type, externalid, externalid_type) values
    ('maastokartta_50k', 'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',
    'WMS_LAYER', 3, 'ROLE');

    INSERT INTO portti_permissions (resource_user_id, permissions_type) values
        (SELECT id FROM portti_resource_user WHERE resource_name='maastokartta_50k' AND externalid = 3, 'VIEW_LAYER');

After this you can log in as admin on the browser and see the added layer. Guest or regular user shouldn't see it listed in the layerselector.

Add permission for logged in user
    INSERT INTO portti_resource_user (resource_name, resource_namespace, resource_type, externalid, externalid_type) values
    ('maastokartta_50k', 'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',
    'WMS_LAYER', 2, 'ROLE');

    INSERT INTO portti_permissions (resource_user_id, permissions_type) values
        (SELECT id FROM portti_resource_user WHERE resource_name='maastokartta_50k' AND externalid = 2, 'VIEW_LAYER');

After this you can log in as a regular user and see the added layer. Guest shouldn't see it listed in the layerselector.

Add permission for guest user

    INSERT INTO portti_resource_user (resource_name, resource_namespace, resource_type, externalid, externalid_type) values
    ('maastokartta_50k', 'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',
    'WMS_LAYER', 10110, 'ROLE');

    INSERT INTO portti_permissions (resource_user_id, permissions_type) values
        (SELECT id FROM portti_resource_user WHERE resource_name='maastokartta_50k' AND externalid = 10110, 'VIEW_LAYER');

After this the layer should be listed in layerselector for guests.

# Users management

### 1. Add new users and roles

* edit user.json and roles.json files in resource path ´oskari-server/servlet-map/src/main/resources/fi/nls/oskari/user/´

* role ids must match the ones referenced in portti_permission and portti_recource_user tables (see next subsection)

Sample ´user.json´

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

Sample ´role.json´

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
* The Java class handling user related operations is configurable in `oskari-server/servlet-map/src/main/resources/fi/nls/oskari/map/servlet/oskari.properties`

### 2. Add new user roles and permissions

Add a role with an unique id to ´role.json´

        {
            "id": 5,
            "name": "MyRole"
        }

Add a user to ´user.json´ and link the new role to the user

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


Edit script file ´oskari-server/servlet-map/src/main/resources/fi/nls/oskari/map/servlet/db/exampleLayersAndRoles.sql´

Add row to portti_resource_user table, e.g. (change [YOUR ROLE ID] to 5 as in role.json)

    INSERT INTO portti_resource_user (resource_name, resource_namespace, resource_type, externalid, externalid_type) values
    ('maastokartta_50k', 'http://a.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://b.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://c.karttatiili.fi/dataset/maastokarttarasteri/service/wms,http://d.karttatiili.fi/dataset/maastokarttarasteri/service/wms',
        'WMS_LAYER', [YOUR ROLE ID], 'ROLE');


Add row to portti_permissions table, e.g. (change [YOUR ROLE ID] to 5 as in role.json)

    INSERT INTO portti_permissions (resource_user_id, permissions_type)
        values (SELECT id FROM portti_resource_user WHERE resource_name='maastokartta_50k' AND externalid = [YOUR ROLE ID], 'VIEW_LAYER');

Remove the HSQLDB data directory and restart the server. You should now be able to login with "myuser"/"mypass" and see the layer