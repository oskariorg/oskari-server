# Modifying the demo data

The HSQLDB is created on first call of the servlet. It creates a data folder under oskari-server with the populated tables if it doesn't exist
(or the directory the server is started from).

NOTE! Any modifications to the initial data SQL statements dont have any effect if the data dir is not
removed and the server is restarted.

Database recreation can be forced on startup by giving a system.property 'oskari.dropdb' with value 'true':

    mvn clean install -f servlet-map-pom.xml -Doskari.dropdb=true -Pjetty-profile

NOTE! All comment lines need to end with ; character or the next SQL statement will not be run!

The safe SQL file to edit for these is `/oskari-server/content-resources/src/main/resources/sql/exampleLayersAndRoles.sql`

# Adding a maplayer

Examples for adding a layer of type:

- WMS in `/oskari-server/content-resources/src/main/resources/sql/exampleLayersAndRoles.sql`.
- WMTS in `/oskari-server/content-resources/src/main/resources/sql/nlsfi-background-map-wmtslayer.sql`.
- WFS in `/oskari-server/content-resources/src/main/resources/sql/PostgreSQL/example-wfslayer.sql`.

Layers have reference to a layer group (oskari_layergroup db-table) which currently means the data producer, but it might become a more generic grouping table in the future. They also can have a link to a list of inspire themes (themes listed in portti_inspiretheme, links to maplayers via oskari_maplayer_themes).

For users to see a registered maplayer the layer needs to have permissions. Permissions are documented in [OskariPermissions.md](OskariPermissions.md).

# Users management

### 1. Add new users and roles

* edit user.json and roles.json files in resource path `oskari-server/control-example/src/main/resources/users/`

* role ids must match the ones referenced in oskari_permission (see next subsection)

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


Edit script file `oskari-server/content-resources/src/main/resources/sql/exampleLayersAndRoles.sql`

Add row to oskari_permission table, e.g. (change [YOUR ROLE ID] to 5 as in role.json)

    -- give view_layer permission for the resource to ROLE 2 (logged in user);
    INSERT INTO oskari_permission(oskari_resource_id, external_type, permission, external_id) values
    ((SELECT MAX(id) FROM oskari_resource), 'ROLE', 'VIEW_LAYER', '[YOUR ROLE ID]');

Remove the HSQLDB data directory (or reset the database with oskari.dropdb=true system property when restarting) and restart the server. You should now be able to login with "myuser"/"mypass" and see the layer