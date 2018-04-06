# Migration guide

## 1.46.0

### UserLayer to separate modules

In order to use UserLayers in the future in your application you'll have to add the following dependency to your webapp-map:

    <dependency>
      <groupId>org.oskari</groupId>
      <artifactId>control-userlayer</artifactId>
    </dependency>

Additionally, `userlayer.default.source.epsg` property is no longer used.
Instead `oskari.native.srs` (same property used by GeoServer populator, defaults to "EPSG:4326")
is used to determine the target projection to which all the UserLayers will be reprojected 
before they are stored in the database.

### jQuery update 1.7.x -> 1.10.2

The frontend has been updated to work with a bit newer jQuery library. We recommend updating any app-specific JSP-files
 to be updated to use it as this is the version that is used when testing features:
 
     -    <script type="text/javascript" src="/Oskari/libraries/jquery/jquery-1.7.1.min.js">
     +    <script type="text/javascript" src="/Oskari/libraries/jquery/jquery-1.10.2.min.js">

### New printout backend

A new printout backend has been added as part of the oskari-server. 
As of 1.46.0 (https://github.com/oskariorg/oskari-frontend/pull/288) the frontend uses the new backend so the old one can be removed.

Note! The new backend is part of the oskari-map.war so any installations using oskari-printout-backend.war can remove it from Jetty.

## 1.45.0

### myplaces, userlayer, analysis baselayers migration

Due to changes in the initialization the baselayer setup has been moved from core to functionality specific flyway modules.
The layers are now inserted with the same code used with the setup.war that initializes geoserver configuration. Configs
that are used include:

    # initialized the layer srs (also updated by setup.war if used to generate GeoServer config)
    oskari.native.srs=EPSG:4326
    # connection info for GeoServer hosting the myplaces etc user content
    geoserver.url=http://localhost:8080
    geoserver.user=admin
    geoserver.password=geoserver

### Oskari initialization

Initialization of Oskari on empty database has been revised. All content creation has been moved to application
 specific flyway modules. The core module only creates the base database, migrates the schema and transforms
  existing data when needed.
   
For existing databases this is a non-issue and everything works as before. For new applications and application specific
 initialization for the database (like initial layers and appsetups) this changes a few things.

The sample application now creates the appsetups, layers and users for demo-purposes. 
Any oskari-server-extension should modify the application init on empty db accordingly.

The template for oskari-server-extension has been updated to match this change: https://github.com/oskariorg/oskari-server-extension-template

Check the readme for details!

### GeoServer migration

If you have the bundled GeoServer (for myplaces etc) running on the same Jetty as oskari-map you should add this to oskari-ext.properties:

    # skip geoserver setting as its by default on the same server -> geoserver is not running when migrations are run
    flyway.1_45_0.skip=true

Otherwise migrations will stop at 1.45.0 as this migration cannot be run. You can manually add a memory restriction 
for the GeoServer so asking for very large image for myplaces etc won't cause the server to run out of memory.

### Database changes

There's at least a couple of database table that have been renamed and due to the order of code is running on server
 startup you will get an error with the first startup due to database migrations. The logs show some tables are missing.
 This is expected and you should just restart the server after migrations have completed.
 The latest version row in oskari_status database table should be at least 1.45.18 when the migrations have been completed. 

### Thematic maps

The release will force thematic maps (statsgrid bundle in frontend) to be updated to the new version.
This requires a migration for any appsetups (including published maps) using the old version of the bundle.
The configurations required for oskari-ext.properties are (based on the database contents):

    # datasource ids added for old indicators
    # [SELECT id FROM oskari_statistical_datasource where config like '%sotkanet.fi/rest%']
    flyway.1_45_4.sotkanet.ds.id=1
    # [SELECT id FROM oskari_statistical_datasource where plugin = 'UserStats']
    flyway.1_45_4.userindicators.ds.id=4

    # layer name mappings for old regionsets (layer name for matching statslayer)
    # [select name from oskari_maplayer where type = 'statslayer']
    flyway.1_45_4.layer.name.kunta=tilastointialueet:kunta4500k_2017
    flyway.1_45_4.layer.name.aluehallintovirasto=tilastointialueet:avi4500k
    flyway.1_45_4.layer.name.maakunta=tilastointialueet:maakunta4500k
    flyway.1_45_4.layer.name.seutukunta=tilastointialueet:seutukunta4500k
    flyway.1_45_4.layer.name.elykeskus=tilastointialueet:ely4500k
    flyway.1_45_4.layer.name.nuts1=dummy:nuts1
    flyway.1_45_4.layer.name.erva=dummy:erva
    flyway.1_45_4.layer.name.sairaanhoitopiiri=dummy:sairaanhoitopiiri

If your instance wasn't using the old thematic maps functionality you don't need to
configure these. If you have the old thematic maps functionality in your application
 you need to add maplayers corresponding the regionsets before running the migration
 and configure the layer-names in oskari-ext.properties.

To configure the layers see: http://oskari.org/documentation/features/thematicmaps/config

For more information about the migration see: https://github.com/oskariorg/oskari-server/pull/127

## 1.44.0

### Requirements change

*Oskari-server now requires Java 8* to run and compile due to (and in preparation of) library upgrades.

Common issues:

- Development environments need to be updated to compile using Java 8
- Servers need to be updated to run Jetty with Java 8
- Jetty start.ini needs to be updated (see https://github.com/oskariorg/sample-configs/commit/508e0ff5353515ead660d595c15870231c52714b)
- server-extensions might use older versions of dependencies which might cause version conflicts. Update the dependencies on extensions as well.

### Transparent fill & stroke on polygons

DefaultStyle SLDs needs to be manually updated on Geoserver from
https://github.com/oskariorg/oskari-server/tree/master/content-resources/src/main/resources/sld

### Users service

The database access library has been updated from iBATIS to MyBatis. DatabaseUserService now uses MybatisRoleService and MybatisUserService. If you are using old IbatisRoleService or IbatisUserService in your own Oskari server extensions, you have to update them to use MybatisRoleService and MybatisUserService implementations.

### Injected profile link (personaldata bundle)

Personal data (My data in UI) previously used the "edit profile" link from property 'auth.register.url' which is also
   used by other functionality as property that holds registration url.

This has been changed and personaldata now uses a more appropriate 'auth.profile.url' so registration and personaldata
 can co-exist without conflict.

## 1.42.1

### User registration feature

The feature has been significantly changed. See ReleaseNotes for details.

### Layer urls handling for https-services

Layers with http:// urls are now proxied using the GetLayerTile action route by default.
Previously the protocol was replaced with https:// and to preserve this functionality you can add a property
 for oskari-ext.properties:

    maplayer.wmsurl.secure=https://

## 1.42.0

### Thematic maps regionsets

Any statslayer rows in the database table oskari_maplayer should include a value in the srs_name column (like 'EPSG:4326').
This is required for creating GeoJSON for the regions and doing transforms on the geometry.

### New bundle "maprotator" for sample application

The new bundle allows published maps to be rotated. The bundle is added to sample application views automatically.
If you don't want it to be added to the sample you can add this property to oskari-ext.properties:

    flyway.sample.1_0_10.skip=true

### Openlayers 3 version for geoportal view for development

Initial Openlayers 3 geoportal view can be added for the sample application. To add it a property is required in oskari-ext.properties:

    flyway.sample.1_0_11.skip=false

The view is NOT added automatically if you don't opt in. The view to create can be further configured with optional properties:

    flyway.sample.1_0_11.file=ol3-geoportal-view.json
    flyway.sample.1_0_11.view=[id for view to use as config/state template]

File needs to point to a json-file similar to
https://github.com/oskariorg/oskari-server/blob/master/content-resources/src/main/resources/json/views/default-full-view.json.
The file should be located in the server classpath under /json/views/[filename].
Note that only bundle startup information is used while config/state is being copied from the default view of the Oskari-instance.
If you want to use a custom view as config/state template you can use the flyway.sample.1_0_11.view-property to point such view.
The log will show the uuid for the new view once it's added or you can check the database table
 portti_view for the uuid of the latest view in the system.


## 1.41.0

### Code refactoring

fi.nls.oskari.control.view.modifier.param.ParamHandler has been moved from control-base to
fi.nls.oskari.view.modifier.ParamHandler in service-control Maven module.
Please update any references to point to the new package.  

### Database migration

In preparation for Oskari 2.0 some of the code has been moved to more appropriate places. As a result some of the
 previously mandatory imports on mapfull bundle have been removed. Update any custom minifierAppSetup.json files
 by removing references to these packages: https://github.com/nls-oskari/oskari/commit/2861ebf4b51849aff7d8619a270ad7fe934fe8d3

There is a Oskari core Flyway-migration that removes the references from database:
https://github.com/nls-oskari/oskari-server/blob/develop/content-resources/src/main/java/flyway/oskari/V1_41_6__remove_empty_packages.java

### FlywayHelper method changes

There has been some changes to the FlywayHelper class. If you have used it in your applications Flyway-migrations you
 might need to change some of the existing Java-based migration classes. This might be the reason your previous
  Java-based migration classes don't compile after updating to new Oskari version. Java-based migrations are not
  tracked the same way as SQL-based ones so you can modify the existing migrations to accommodate this change.
Here are the changes to the FlywayHelper signature:

    FROM ArrayList<Long> getViewIdsForTypes(Connection connection, String... types) throws Exception
    TO   List<Long> getViewIdsForTypes(Connection connection, String... types) throws SQLException

    FROM ArrayList<Long> getUserAndDefaultViewIds(Connection connection) throws Exception
    TO   List<Long> getUserAndDefaultViewIds(Connection connection) throws SQLException

    FROM Bundle getBundleFromView(Connection connection, String bundle, Long viewId) throws Exception
    TO   Bundle getBundleFromView(Connection connection, String bundle, Long viewId) throws SQLException

    FROM Bundle updateBundleInView(Connection connection, Bundle bundle, Long viewId) throws Exception
    TO   Bundle updateBundleInView(Connection connection, Bundle bundle, Long viewId) throws SQLException


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
