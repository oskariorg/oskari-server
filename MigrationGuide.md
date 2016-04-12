# Migration guide

## 1.36.0

### Geotools 14.2 upgrade (was 13.1)

There are some improvements in geojson parsing and new methods for parsing WMS/WFS capabilities.
Basic oskari-server source is not modified because of this upgrade, only some unit tests.


## 1.35.0

Update IntersectionFeatureCollection2-2.7.1.jar in your webapps/geoserver/WEB-INF/lib directory.
Build updated version in https://github.com/nls-oskari/oskari-server/tree/master/geoserver-ext/wps. 
This is improvement for analysis Geometry clip method.

## 1.34.0

A new publisher template has been added: content-resources/src/main/resources/json/views/ol3-publisher-template-view.json.
This can be inserted to database and configured as a publish template in oskari-ext.properties to use Openlayers 3 as the
map-engine for published maps:

    view.template.publish=[view id]

This will affect all new maps that are published after the change. The previous ones will remain the same. If a view 
 that is published earlier is edited and saved it will use the new publish template.

## 1.33.0

The sample application replaces publisher bundle with a refactored version named publisher2. This needs metadata to be
generated for appsetups/views created with the original publisher. The sample flyway module has 
an example (flyway/sample/V1_0_5__publisher2_migration.java) how to make the switch in your application when you are ready.
The script generates the metadata from existing published views and replaces the publisher bundle with publisher2 on all views
which have the original publisher.

In older Oskari instances users might have saved views that still reference old bundles: mapwfs and featuredata. 
Views of type USER (portti_view.type) that have these (others shouldn't have them anyways anymore) are automatically
 updated to replace these with the current implementations. The code for these outdated bundles have been removed from
 Oskari frontend code a while ago so we need to remove references to them so the personalized default view can work properly. 

Old saved views (that users can save) have the bundle setup that was used when the view was saved. Bundles added to
 default view after the view was saved are not part of those views. This results in personalized default views not
 having all of the functionality that is available in the default view.
 A workaround for this is for the user to click the saved view so the application state is changed based on the view and 
 then save the current view as a new one. Then use the new view as the personalized default and delete the old saved view.
To update all views of type USER to current bundle setup automatically, there is an example upgrade script in the sample flyway module
 (flyway/sample/V1_0_6__upgrade_saved_views_to_include_default_view_bundles.java). This update is not forced since 
 some Oskari installs might have used the USER-typed views for other purposes than views saved by users. 

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
