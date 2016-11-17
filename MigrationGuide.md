# Migration guide

## 1.40.0

The sample application can be updated to include the new statistics UI. If you are using the sample-application flyway
module (have "sample" included in oskari-ext.properties "db.additional.modules" property) and would like to have 
this bundle included, you can add a property: 

    flyway.sample.1_0_8.skip=false 

This functionality is opt-in with the configuration since it needs additional manual configuration for datasources and 
maplayers to use as regionsets. The frontend minifierAppSetup.jsons for sample/servlet app include references to the
 frontend code so to optimize the filesize clients need to download you might want to remove them if not using the 
 statistics functionality. 

The sample application will also add more zoom-levels for the configuration. All views except published maps with
 EPSG:4326 will be have the resolutions (zoom-levels) set to: 
 
    [0.3515625,0.17578125,0.087890625,0.0439453125,0.02197265625,0.010986328125,0.0054931640625,0.00274658203125,0.001373291015625,0.0006866455078125,0.00034332275390625,0.000171661376953125,0.0000858306884765625,0.00004291534423828125,0.000021457672119140625,0.000010728836059570312,0.000005364418029785156,0.000002682209014892578]

You can opt-out of the resolutions change by adding a property in oskari-ext.properties:

    flyway.sample.1_0_9.skip=true

## 1.39.0

This update includes a forced migration of publish template to Openlayers 3 based map. The possible Openlayers 2 based 
publish template will be copied to a new ID and the log will show a message like "Previous publish template was
 saved as a backup with view id: 1234." The update is skipped if Openlayers 3 based template is already defined
  for publishing, but you can also skip the update entirely by adding a skip property to oskari-ext.properties:

    flyway.1_39_1.skip = true

*Note!* All new development is happening for Openlayers 3 based published maps and some features may be broken if
 migration is skipped. You should update the template manually if automation isn't working for you.  

The bundles in the template are changed based on this template: 
https://github.com/nls-oskari/oskari-server/blob/master/content-resources/src/main/resources/json/views/ol3-publisher-template-view.json

If you would like to use different page(JSP-file), application or path for the template you can override these by
 adding configuration to oskari-ext.properties: 

    flyway.1_39_1.application = servlet_published_ol3
    flyway.1_39_1.path = /applications/sample
    flyway.1_39_1.page = published

You can also specify a custom template to be used by providing a similar file in classpath with
 /json/views/my-template.json and using the property:

    flyway.1_39_1.file = my-template.json

The update keeps any state and configuration values for the template as they are, but updates the bundles to use and the 
startup-fragment (filepath/location of the bundle).

*Note!* You will need to update the minifierAppSetup.json to reflect the new template. This can be used with the default setup:
https://github.com/nls-oskari/oskari/blob/master/applications/sample/servlet_published_ol3/minifierAppSetup.json

Another update is used to migrate all published maps using Openlayers 2 based published maps to use the new publish template.
This will programmatically "republish" all the maps having OL2 with the current publish template. This means that if you 
skipped 1.39.1 update for the template AND haven't done anything for updating the template manually this will use an unmigrated
publish template. This is something to consider. You can skip the migration by adding a property to oskari-ext.properties:
 
    flyway.1_39_2.skip = true

*Note!* If you skip this update you might need to have a separate template for new published maps using OL3 and older 
published maps using OL2. The migration will take a while depending how many published maps there are in the system.
 It can also fail, usually if there is broken/invalid JSON in config or state columns for bundles. You should fix the
 JSON manually in the database and restart Jetty to run the migration again for the rest of the published maps.

*Tip!* If you are trying to make a custom template you can:
  1) Skip this migration
  2) Try using the custom template with the publishing functionality
  3) When you are ready to use the template: delete the row from database table oskari_status for 1.39.2 migration
  4) Restart Jetty to run the migration using the custom template

## 1.38.0

The default config for statsgrid-bundle has changed and is now part of the code. The default config in portti_bundle is 
updated to empty config. 

The storing of layer coverage geometries has changed in a way that might cause problems. 
If layer disappear from the browser UI a ~second after it has been added to the map you should
 wipe the oskari_maplayer_metadata table in the database:
 
         DELETE FROM oskari_maplayer_metadata; 

This is only relevant if you have the scheduled coverage job enabled.

## 1.37.0

### Forced view migrations - IMPORTANT!

Map publishing functionality has been implemented with a bundle called publisher. All current work toward improving 
the publishing feature has been done on a bundle publisher2. Since the old one is no longer maintained we are forcing an
update to any view having the publisher bundle to start using the publisher2 bundle. This shouldn't cause any problems 
if you are using the standard publisher bundle with no modifications. If you absolutely want to keep the current publisher bundle 
and will deal with the changes yourself you can add a property to oskari-ext.properties to skip this update:

    flyway.1_37_1.skip=true

You might also have ran this kind of update on your environment previously (through sample-module migrations for example).
In such case this migration does nothing.

### servlet-map/webapp-map - Openlayers polyfill

The latest Openlayers 3 doesn't work with IE9 since it lacks for example requestAnimationFrame()
 function: https://github.com/openlayers/ol3/issues/4865. A polyfill has been added to published.jsp for browsers <= IE 9.

You might want to do the same for any custom JSP for published maps if you need to support IE 9.

## 1.36.0

### Application changes to JSP-files

If you have a custom JSP in your Oskari installation you will need to modify it a bit:

Custom JSPs may have links to Oskari css and have global variables set for Oskari:

    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/icons.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/forms.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/portal.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/overwritten.css"/>

    <script type="text/javascript">
        var ajaxUrl = '${ajaxUrl}';
        var viewId = '${viewId}';
        var language = '${language}';
        var preloaded = ${preloaded};
        var controlParams = ${controlParams};
    </script>

These can be replaced with the following:

    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari/resources/css/forms.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari/resources/css/portal.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/icons.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/overwritten.css"/>

    <script type="text/javascript">
        var ajaxUrl = '${ajaxUrl}';
        var controlParams = ${controlParams};
    </script>
------------------------
Note that you need to remove any references to these globals (language, viewId, preloaded) in index.js as well (in the frontend)

See https://github.com/nls-oskari/oskari/blob/master/ReleaseNotes.md#Default-iconsets-for-applications for more details on 
 frontend changes.

You should also remove any links to oskari_lang_all.js in JSPs as it's no longer generated and its content is included 
 in all of the language files. 

### Geotools 14.2 upgrade (was 13.1)

There are some improvements in geojson parsing and new methods for parsing WMS/WFS capabilities.
Basic oskari-server source is not modified because of this upgrade, only some unit tests.

### toolbar in embedded maps

The configuration has changed for any published maps with toolbar. Automatic migration is done as part of the core 
migration.

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
