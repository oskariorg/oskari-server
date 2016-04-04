# Content editor

Tampere Oskari bundle

Extends Oskari functionality to support managing layers features and his geometries

## Prerequisites

These uses jetty-8.1.16-oskari version

### Front-end

This bundle needs new Oskari version (jetty-8.1.16-oskari or above)

### Back-end


##### Database

Oskari database is needed but nothing more need to be change

## Installation

Create base folder i.e. Tampere
```
mkdir Tampere
```

Change directory to Tampere

```
cd Tampere
```

Download Oskari (http://oskari.org/build/server/jetty-8.1.16-oskari.zip) and extract archive to current (new created) directory


Goto jetty folder

```
cd jetty-8.1.16-oskari
```

remove Oskari content and clone Oskari client

```
git clone https://github.com/sitooy/tampere-oskari.git oskari
```

Clone Oskari server to base directory (here Tampere)

```
git clone https://github.com/sitooy/tampere-oskari-server.git oskari-server
```

Enter to Oskari server directory

```
cd oskari-server
```

build

```
mvn clean install
```

and copy war files (site and transport)
```
cp webapp-map\target\oskari-map.war ..\jetty-8.1.16-oskari\webapps\oskari-map.war
```

```
cp webapp-transport\target\transport.war ..\jetty-8.1.16-oskari\webapps\transport.war
```

### Configuration

To configure new permission type add to oskari-ext.properties
```
permission.types = EDIT_LAYER_CONTENT
permission.EDIT_LAYER_CONTENT.name.fi=Muokkaa tasoa
permission.EDIT_LAYER_CONTENT.name.en=Edit layer
```

To add new content-editor bundle to Oskari configuration run [SQL file](https://github.com/sitooy/tampere-oskari-server/blob/master/content-resources/src/main/resources/sql/views/01-bundles/tampere/001-content-editor.sql) to add bundle configuration to database. 

Add bundle dynamically to correct roles in oskari-ext.properties. For example:
```
actionhandler.GetAppSetup.dynamic.bundles = admin-layerselector, admin-layerrights, admin-users, admin, content-editor
actionhandler.GetAppSetup.dynamic.bundle.content-editor.roles = Admin
```

Add content-editor bundle to portti_bundle table.

INSERT INTO portti_bundle(
            name, config, state, startup)
    VALUES ('content-editor', '{}', '{}', '{
        "title" : "content-editor",
        "bundleinstancename" : "content-editor",
        "fi" : "content-editor",
        "sv" : "content-editor",
        "en" : "content-editor",
        "bundlename" : "content-editor",
        "metadata" : {
            "Import-Bundle" : {
                "content-editor" : {
                    "bundlePath" : "/Oskari/packages/tampere/bundle/"
                }
            }
        }
    }');



If bundle should be loaded for all users, skip editing oskari-ext.properties and run following SQL:
```
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup) 
    VALUES ((SELECT id FROM portti_view WHERE type='DEFAULT'), 
    (SELECT id FROM portti_bundle WHERE name = 'content-editor'), 
    (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = (SELECT id FROM portti_view WHERE type='DEFAULT')), 
    '{}','{}', '{}');

-- update proper startup for view
UPDATE portti_view_bundle_seq set startup = '{
        "title" : "content-editor",
        "bundleinstancename" : "content-editor",
        "fi" : "content-editor",
        "sv" : "content-editor",
        "en" : "content-editor",
        "bundlename" : "content-editor",
        "metadata" : {
            "Import-Bundle" : {
                "content-editor" : {
                    "bundlePath" : "/Oskari/packages/tampere/bundle/"
                }
            }
        }
    }' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'content-editor') 
    AND view_id=(SELECT id FROM portti_view WHERE type='DEFAULT');
```

Enter to jetty directory and run
```
java -jar start.jar
```

Use case in Oskari
```
Find out a wfs layer, which is enabled to wfs-t or import your own layer to Oskari Geoserver (e.g. with shp2pgsql-gui.exe)
Add 'Edit' right  to that layer in Oskari Layer Rights method.
Then select the wfst layer in Oskari and the 'feature editor' link is available in layer selection.
```

Remarks
```
Editing is only available for geometry in EPSG:3067 CRS for time being.
Update SRID to to your wfst layer before editing, if it is 0 (default)
( UPDATE <table> SET <geometry>=ST_SetSRID(<geometry>,3067) )
WFS layer doesn't e.g. for MapClick, if there is mixed SRID in the <table>
select distinct(ST_SRID(<geometry>)) as srid, count(*) from <table> group by srid;
```