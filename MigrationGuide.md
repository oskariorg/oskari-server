# Migration guide

## 1.33.0

The sample application replaces publisher bundle with a refactored version named publisher2. This needs metadata to be
generated for appsetups/views created with the original publisher. The sample flyway module has 
an example (flyway/sample/V1_0_5__publisher2_migration.java) how to make the switch in your application when you are ready.
The script generates the metadata from existing published views and replaces the publisher bundle with publisher2 on all views
which have the original publisher.

## Geoserver

Update sld_muutos_n1.sld in Geoserver Styles (updated file in \oskari\oskari-server\content-resources\config\geoserver\data\styles)

## 1.32.0

### content-resources/flyway

Flyway checksum validation fails if line-endings change in files for example between development environments:
https://github.com/flyway/flyway/issues/253. 
To work around this, oskari-ext.properties can be used to autorepair the checksums: 

    db.flyway.autorepair=true

## 1.31.0

### servlet-map replacement

The servlet-map module has been replaced with spring-based packaging. This uses servlet 3 webapp initialization without 
 web.xml. If you are using the Jetty-bundle downloadable from oskari.org. Add this row to {JETTY_HOME}/start.ini. Take a look at 
 the new Jetty-bundle for example:
 
    etc/jetty-annotations.xml

HTML-loading and action route handling has been separated with HTML response coming from the root path (/) and action routes
are now handled with path /action. This propably requires a change in oskari-ext.properties for the property:

    # url path to call for ajax requests/action routes
    oskari.ajax.url.prefix=/action?

The Maven artifactId was changed to clearly signal the change:

    <groupId>fi.nls.oskari</groupId>
    <artifactId>servlet-map</artifactId>

## 1.30

### Java update

As the updated geotools version has dropped Java 6 support, Oskari now requires Java 7 as well.

### Geoserver 2.7.1 and geotools 13.1 upgrade

Keep your current Geoserver data dir as is
Replace your geoserver directory under webapps with geoserver dir in jetty-package http://oskari.org/build/server/jetty-8.1.16-oskari.zip

- Current Geoserver build is against Geotools 13.1
- know issue: RYSP WFS parser is not working and partly WFS transport tests fails, if older version of geotools is in use

## 1.29

### DB upgrades

#### Update bundlepath of routesearch bundle

Routesearch bundle has been moved from under framework to paikkatietoikkuna. Bundlepaths in db need to be updated. 
Run the node.js upgrade scripts under content-resources/db-upgrade:

    SCRIPT=1.29/01_update_routesearch_bundle_bundlepath node app.js
    SCRIPT=1.29/02_update_routesearch_bundle_view_bundlepath node app.js

### Update default configs for userguide and printout

Run on oskaridb:

    content-resources/src/main/resources/sql/upgrade/1.29/01_update_defaults.sql

### Create new table ( oskari_wfs_parser_config) for WFS 2.0.0 initial parser configs and insert initial values

    content-resources/src/main/resources/sql/upgrade/1.29/02_create_oskari_wfs_parser_config.sql
     
### webapp-transport

Now builds transport.war instead of transport-0.0.1.war as this is the default Oskari frontend uses.
