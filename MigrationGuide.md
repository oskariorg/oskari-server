# Migration guide

## 2.11.0

PostgreSQL 11 is now the minimum version supported (FlywayDB dependency).

OpenStreetMap and What3Words search channels were removed from control-example dependencies.
If you want to use these as your applications search backend you can add them in the app dependencies on pom.xml like this:

```
<dependency>
    <groupId>org.oskari</groupId>
    <artifactId>service-search-opendata</artifactId>
</dependency>
```

This fixes an issue where (most) applications that have their own search backend implementations either need to black list the OSM channel OR exclude this depedency. This makes the choice of using the channel more explicit.

Another common configuration that most instances would probably want to add to oskari-ext.properties is:
```
# Don't show metadata catalogue as option as it doesn't return locations as results
actionhandler.SearchOptions.blacklist=METADATA_CATALOGUE_CHANNEL
```

This removes metadata search channel from SearchPlugin channel listing.
As the channel is not returning locations it will never return anything useful for the location search UIs.

## 2.10.0

Sample-server-extension now includes JSTL dependency by default so the web app works out-of-the-box in for example Tomcat environment:
https://github.com/oskariorg/sample-server-extension/pull/39

A placeholder for favicon have been added on the sample-server-extension so it's easier to override by replacing the file with a custom one:
https://github.com/oskariorg/sample-server-extension/pull/36

### Base HTML changes!

Unncessary elements and CSS have been removed from the base HTML templates. The HTML that has been previously used _should_ still work as before with one minor adjustment. The `margin-left: 170px;` on `#contentMap` element should be removed from the JSP: https://github.com/oskariorg/sample-server-extension/pull/35

The frontend code now creates necessary elements for the map on it's own. The navigation block is still declared on the server side JSP but it will be moved to frontend in a future release.
You can take a look at the new samples in here:
https://github.com/oskariorg/sample-server-extension/tree/7f499fc51147be981108ef2536788c5cc811417c/webapp-map/src/main/webapp/WEB-INF/jsp

If you want to have the navigation bar on the right side of the map you will still need to declare the map element on the HTML (see `geoportal_white.jsp`).
The `geoportal_stylized.jsp` provides an example for adding additional elements around the space where Oskari renders itself (like service header etc).

### Theme-related updates

The navigation bar background color can now be configured in the theme JSON for application in the database (default color is the same as before). Navigation bar background-color CSS can be removed from JSP:
https://github.com/oskariorg/sample-server-extension/pull/38


## 2.8.0

### My data frontend implementation changed

A new bundle `mydata` has been added as (in most cases) drop-in replacement for `personaldata`. If you don't use any customized tabs in My data in your app (only using the ones included in oskari-frontend) you can safely update from personaldata to mydata. If you do have application specific tabs on personaldata see [frontend notes](https://github.com/oskariorg/oskari-frontend/blob/master/ReleaseNotes.md#280) for more details.

For most apps you can and should add this migration for your app:
```
UPDATE oskari_appsetup_bundles
 SET bundle_id = (select id from oskari_bundle where name='mydata'),
     bundleinstance = 'mydata'
 WHERE bundle_id = (select id from oskari_bundle where name='personaldata');
```

We might do a forced update on the next release when `personaldata` is removed.

## 2.5.0

### Userstyle bundle

Userstyle functionality has been moved from wfsvector bundle to own bundle. You will need to add an import to the frontend code for your geoportal apps main.js:
```
import 'oskari-loader!oskari-frontend/packages/mapping/ol/userstyle/bundle.js';
```
The required changes have been made as pull requests for our demo app `sample-application` version 1.18.0:
- https://github.com/oskariorg/sample-application/pull/17

## 2.0.0

The required changes have been made as pull requests for our demo app `sample-server-extension` version 1.4.0:
- https://github.com/oskariorg/sample-server-extension/milestone/5?closed=1

### Maven

GroupId on all Oskari artifacts is now `org.oskari` (previously a mixture of `org.oskari`, `org.oskari.service`, `fi.nls.oskari` and `fi.nls.oskari.service`).
ArtifactId on most Oskari artifacts changed as follows:

- `oskari-parent` (parent pom) is now `oskari-server`
- if artifactId started `oskari-control-*` it is now `control-*`
- if artifactId started with `oskari-*` (where `*` is NOT `control-*`) it is now `service-*`

### Database / Core migrations

All the database services now use MyBatis. Ibatis has been dropped from the dependencies.

In the core Flyway module we have dropped all of the 1.x Flyway migrations from oskari-server
 which lets us clean up some of the code regarding these.

For new installs the Flyway baseline version is set at 2.0.0. 
For existing installs the status table is dropped (to drop references to previous migrations
 that we have removed from oskari-server) as re-baselining is not allowed and the baseline
 is set at 2.0.4. This lets us skip new migrations that will initialize the database tables
 for empty database. 
 
There are new shared migrations for both new and old installs that will rename some of the tables
 in the database so naming is more consistent:

- https://github.com/oskariorg/oskari-server/pull/618
- https://github.com/oskariorg/oskari-server/pull/619

After this all the database tables in the core module are prefixed with `oskari_`. 
The user content modules (`myplaces`, `userlayer` etc) are not changed at this time.

#### Flyway migrations

The Flyway library has been updated to its latest version that includes an API change for all Java-based migrations.
This requires manual work for changes that have been described here: 

- https://github.com/oskariorg/oskari-server/pull/614

You also have an option to just drop the current application Flyway-module assuming everyone that wants to use your app
has a database dump for it. You can add a new module for future migrations with a different name (or same name but you will
need to manually drop the database table for oskari_status_[your module name]). If you are not planning on the migrations to 
work on an empty database this is the most cost-effective way to do this.

Even if you want to upgrade your current migrations to work for an empty database you might want to clean them up and 
combine them. It's possible to check in a migration if initial content needs to be inserted and only insert if needed etc.
   
#### Flyway Helpers

Helpers for common Flyway operations have been updated for consistency in naming and parameter order.
Setup-files are no longer supported (as it added unnecessary complexity). 
Instead you can use `org.oskari.helpers.AppSetupHelper` to insert any appsetups/views to the
 database directly that were previously referenced in setup-files.
 You can also use `LayerHelper` to add initial map layers to the database as before.
 The Java package has been changed from `fi.nls.oskari.db.LayerHelper` to `org.oskari.helpers.LayerHelper`
 but methods remain unchanged. 

Additional information:
- https://github.com/oskariorg/oskari-server/pull/615

1.56.0 allowed initializing content with "setup" files (not supported on 2.0+):
```
fi.nls.oskari.db.DBHandler.setupAppContent(conn, [ref to a file under "setup"]);
```
For 2.0.0 instead run SQL as normal Flyway migrations in application module and insert appsetups/layers with related helpers as Java-based Flyway migrations
```
org.oskari.helpers.AppSetupHelper.create(conn, [ref to a file under "json/views"])
```

1.56.0 -> 2.0.0 changes to migration helpers
```
fi.nls.oskari.db.ViewHelper -> org.oskari.helpers.AppSetupHelper
ViewHelper.insertView("appsetup.json) -> AppSetupHelper.create("/json/appsetup.json")
Inserts the appsetup like before but now you can give full path to the file instead of it being assumed to be prefixed.

fi.nls.oskari.db.BundleHelper -> org.oskari.helpers.BundleHelper
all method parameters with connection changed for consistency

fi.nls.oskari.db.LayerHelper -> org.oskari.helpers.LayerHelper

fi.nls.oskari.util.FlywayHelper -> org.oskari.helpers.AppSetupHelper
FlywayHelper.getUserAndDefaultViewIds() -> AppSetupHelper.getSetupsForUserAndDefaultType() 
FlywayHelper.viewContainsBundle() -> AppSetupHelper.appContainsBundle()
FlywayHelper.addBundleWithDefaults() -> AppSetupHelper.addBundleToApp()
FlywayHelper.getBundleFromView() -> AppSetupHelper.getAppBundle()
```

Added a new helper for easily adding a new bundle to the usual appsetup types (USER and DEFAULT):
```
AppSetupHelper.addBundleToApps(connection, bundlename)
```

### Spring framework upgraded

If you have application-specific code that uses Spring you might need to adapt them to the 5.x version.

Spring framework dependencies are now handled with a "Bill of materials" import to managed
 dependencies so it's easier to use the same version of Spring artifacts that are used in Oskari.
 This means you shouldn't have to (re)declare the version on any Spring artifacts on pom.xml of your application.

### GeoTools/JTS upgraded

We have updated the GeoTools library which introduces a change in JTS Java-packages.
If you have used JTS classes in your application specific code you will need to update to the new packages:

- https://github.com/locationtech/jts/blob/master/MIGRATION.md

It might be as simple as:
```
import com.vividsolutions.jts.*' -> org.locationtech.jts.*;
import org.geotools.xml.* -> org.geotools.xsd.*
```

When compiling your application Java will let you know if compilation fails because of these.

### Updated Jetty and GeoServer

The download package from Oskari.org has an updated Jetty version. If you are using nginx and the 
configurations provided in sample-configs repository note that we have removed
the X-Forwarded-Port header from the config as it messed with for example the logout functionality with the new Jetty:

- https://github.com/oskariorg/sample-configs/commit/d3f36a33dd8dbaac475573a301b5d71af365b47d

Updating services based on the zip-download on https://oskari.org/download you can:

1) Replace the jetty-distribution folder with the new one (from for example the updated zip file): https://github.com/oskariorg/sample-configs/pull/8
2) Replace oskari-server/webapps/geoserver directory with the new one (from for example the updated zip file): https://github.com/oskariorg/sample-configs/pull/7
3) Update any systemctl or similar service/startup scripts to point to the new Jetty folder:

- Replacing `java -jar ../jetty-distribution-9.4.12.v20180830/start.jar`
- With:     `java -jar ../jetty-distribution-9.4.31.v20200723/start.jar`

## 1.54.0

### Bundle path changes related to OpenLayers map engine upgrade

In this version OpenLayers map engine is upgraded to version 6.
As a part of mentioned upgrade, version number is dropped from mapping bundle paths.
All mapping bundle imports from oskari-frontend containing 'ol3' folder in import path needs to be fixed as follows:

    // Example of old import containing 'ol3' in import path:
    import 'oskari-loader!oskari-frontend/packages/mapping/ol3/mapmodule/bundle.js';
    // Replace it with this row:
    import 'oskari-loader!oskari-frontend/packages/mapping/ol/mapmodule/bundle.js';


## 1.53.0

### Migration to the new WFS integration backend

This version includes a migration that will change the database migrating all apps to use a new WFS integration backend.
The change is described here https://github.com/oskariorg/oskari-docs/issues/109

You should backup your database before upgrading so you can restore it if you have some functionality in your app that is NOT
compatible with the new system. Add this to oskari-ext.properties to continue using the transport for WFS-layers on this version:

    flyway.1.53.wfs.optout=true

It is heavily recommended that you migrate and report any problems you might encounter at this point.
On this version you can still opt-out of the migration but the next version will force the migration and remove
 references to the transport webapp. You can consider the transport webapp deprecated.

Note! You also need to add an import to the frontend code for your apps main.js:

    // this should already be there:
    import 'oskari-loader!oskari-frontend/packages/mapping/ol3/mapwfs2/bundle.js';
    // Replace it with this row:
    import 'oskari-loader!oskari-frontend/packages/mapping/ol3/wfsvector/bundle.js';

### Disabled CSRF protection on webapp level

The previous cross-site request forgery protection used cookies and http headers to detect if a given request to the server
was legit. Because certain browsers block 3rd party cookies by default this didn't really work on embedded maps.
 Java libraries don't yet support adding SameSite-flag on cookies which is the current solution to protect against
 CSRF attacks. You should configure your reverse-proxy to modify cookies to have SameSite=lax flag.

Here's an example how to do this with nginx: https://github.com/oskariorg/sample-configs/commit/e3802ccd84d866dc1643f7dfc98f80bf6fe5cde9

### PermissionService migrated to MyBatis

Also class packages have been changed a bit so manual updates is required for server-extensions referencing PermissionService.

- fi.nls.oskari.permission.domain.Permission is now org.oskari.permissions.model.Permission
- fi.nls.oskari.permission.domain.Resource is now org.oskari.permissions.model.Resource
- fi.nls.oskari.map.data.domain.OskariLayerResource is now org.oskari.permissions.model.OskariLayerResource and is now deprecated.

See https://github.com/nls-oskari/kartta.paikkatietoikkuna.fi/pull/102 for example and
https://github.com/oskariorg/oskari-server/pull/271 for details about the change.

OskariLayerResource has been deprecated since it no longer serves any purpose. The mapping of layer permissions mapping
 has been changed from type+url+name to use the layer id. Note that this might break some old migrations that use it
 if run on an empty database

## 1.52.0

### Class renaming:

- BundleServiceIbatisImpl is now BundleServiceMybatisImpl
- ViewServiceIbatisImpl is now AppSetupServiceMybatisImpl
- DataProviderServiceIbatisImpl is now DataProviderServiceMybatisImpl
- OskariLayerServiceIbatisImpl is now OskariLayerServiceMybatisImpl

These might be used in app-specific migrations so you will need to update references to these.

### Userlayer SLD update

There was a small issue on the SLD for userlayers where polygon border style was rendered with line style.
To fix this you will need to manually update the SLD on the GeoServer Oskari uses to store userlayers. The SLD to use
can be found here: https://github.com/oskariorg/oskari-server/blob/master/content-resources/src/main/resources/sld/userlayer/UserLayerDefaultStyle.sld

### Optional: New WFS-integration system

This version includes a new implementation for interacting with WFS-based map layers that will replace the current "transport" webapp in the future.
It supports WFS 3.0 endpoints and hands the features as vectors instead of images to the frontend.
We would appreciate any feedback on the new implementation and testing it should not be a huge pain to setup.

Replace 'Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin' with 'Oskari.wfsvector.WfsVectorLayerPlugin' on mapfull bundle configurations.
You can do this by modifying the applications/[your app folder]/index.js on your applications frontend code to change the appsetup it gets from the server:

```
jQuery(document).ready(function() {
    Oskari.app.loadAppSetup(ajaxUrl + 'action_route=GetAppSetup', window.controlParams, function() {
        jQuery('#mapdiv').append('Unable to start');
    }, function() {
         // App started
    }, function(appSetup) {
        // modify the appsetup we got from server
        var plugins = JSON.stringify(appSetup.configuration.mapfull.conf.plugins);
        var pluginRemoved = plugins.split('Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin');
        var modifiedPlugins = pluginRemoved.join('Oskari.wfsvector.WfsVectorLayerPlugin');
        appSetup.configuration.mapfull.conf.plugins = JSON.parse(modifiedPlugins);
    });
});
```

OR you can use this Flyway migration as an example for your own instance to test it out:

https://github.com/nls-oskari/kartta.paikkatietoikkuna.fi/blob/develop/server-extension/src/main/java/flyway/paikkis/V2_36__use_wfs_vector_layer_plugin.java

You will also need to add "wfsvector" after "mapwfs2" bundle import in applications/[your app folder]/main.js on the frontend of your app:
```
import 'oskari-loader!oskari-frontend/packages/mapping/ol3/mapwfs2/bundle.js';
import 'oskari-loader!oskari-frontend/packages/mapping/ol3/wfsvector/bundle.js';
```

## 1.51.0

### Logging configuration changes required

Log4j was updated to version 2.x. 
- If your application only uses the Oskari logger (fi.nls.oskari.log.LogFactory), no changes are needed.
- If your application depends directly on the old 1.2.x log4j provided by Oskari, you should migrate to version 2
- If your application depends slf4j provided by Oskari, you should change the slf4j implementation library from `slf4j-log4j12` to

```
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
</dependency>
```

Log4j2 uses a new syntax for logger configuration. It will look for `log4j2.xml` named file on the class path (you can put it under Jetty directory `resources/`).
Example configuration can be found in the documentation:

- https://github.com/oskariorg/sample-configs/blob/master/jetty-9/oskari-server/resources/log4j2.xml
- https://logging.apache.org/log4j/2.0/manual/configuration.html

### SLDs need to be updated to work with current GeoServer version

The updated SLDs can be found in https://github.com/oskariorg/oskari-server/tree/master/content-resources/src/main/resources/sld

These need to be manually updated on the GeoServer that is used for providing the user generated content (the included GeoServer if not customized).

## 1.50.0

This release requires Jetty 9 to be used with the transport webapp. Jetty 8 no longer works as the CometD library has been updated.
For migrating from the previous Jetty 8 package to the new Jetty 9 package you can download the new Jetty bundle from oskari.org. 
The migration is pretty straightforward:

1) Download the new Jetty 9 package from oskari.org/download

2) Move any customized configuration from {Jetty 8}/start.ini to {Jetty 9}/oskari-server/start.d/oskari.ini

3) Build your app with Oskari 1.50.0+ version and replace the war-files under {Jetty 9}/oskari-server/webapps

- Note! Update the web.xml under {your server extension}/webapp-transport/src/main/webapp/WEB-INF/web.xml to
 match the changes in {oskari-server}/webapp-transport/src/main/webapp/WEB-INF/web.xml
- Namely the servlet-class for transport has changed from org.cometd.server.CometdServlet to org.cometd.server.CometDServlet
 as seen here https://github.com/oskariorg/oskari-server-extension-template/commit/88ffa45bd649b1967f07dff0470e2fd044f7a35a

4) Copy everything under {Jetty 8}/resources to {Jetty 9}/oskari-server/resources
5) Start Jetty 9 with:
- work directory as {Jetty 9}/oskari-server
- Run:

    java -jar ../jetty-distribution-9.4.12.v20180830/start.jar

See https://github.com/oskariorg/sample-configs/blob/master/jetty-9/Howto.md and oskari.org for more details.

You will also need to update the frontend for Oskari 1.50.0+ as the CometD client library has been updated and
 the old one doesn't work with the new server or vice versa. 

## 1.49.0

### JSP-files modified to match the new frontend build

There are some changes required for any customized JSP-pages:

- jQuery is now part of oskari.min.js - remove script tag for jQuery
- bundles/bundle.js has been removed - remove reference to it
- resources/portal.css and forms.css are now part of oskari.min.css - remove references to them
- app/overwritten.css is now part of oskari.min.css - remove reference to it
- the "preloaded" variable is now always true - remove any logic using it

The frontend code is now always minified/bundled as oskari.min.js even on development environment.
See the example app in oskari-server for template JSP in custom installs.

### search-service-nls

Due to being specific to NLS Finland services the code has been moved to nlsfi/oskari-server-extras#1 and but it's still
 available in oskari.org/nexus. For drop-in replacement change:

     <dependency>
        <groupId>fi.nls.oskari.service</groupId>
        <artifactId>oskari-search-nls</artifactId>
        <version>${oskari.version}</version>
    </dependency>

to:

     <dependency>
        <groupId>fi.nls.oskari.extras</groupId>
        <artifactId>oskari-search-nls</artifactId>
        <version>2.1</version>
    </dependency>

## 1.48.0

### Changes to JSP-pages

#### Cross-site request forgery protection

Security features in Oskari has been improved by enabling cross-site request forgery protection.
Any requests done with HTTP-methods other than GET is required to include a token as header or parameter to be accepted. 

There are some changes required for any customized JSP-pages:

- Logout must be done with HTTP POST (https://github.com/oskariorg/oskari-server/commit/3aecfdd6c983c840e4d268f32d85c010041c5752)
- Any additional customized pages/calls need to include the token for example user registration (https://github.com/oskariorg/oskari-server/commit/9d7440f08b73c8d033b8eab9562a2ca1ed036718)

Oskari frontend code will automatically include the token by default on any action route calls made by it
 (https://github.com/oskariorg/oskari-frontend/blob/e42481ac6c4bf273cb1c55aa0857cb3b94482703/src/oskari.app.js#L9-L31).
Most of the action routes (ones doing write operations) have been changed to only respond to non-GET requests(POST/PUT/DELETE).

#### jQuery update

The default jQuery version has been updated from 1.10.2 to 3.3.1 (https://github.com/oskariorg/oskari-server/commit/0dc08057a91282f09999f7d21f29d935b2664ece).
The functionality in oskari-frontend has been modified to work with the new version, but if you have customized bundles
 you might want to take a look at the official upgrade guide: https://jquery.com/upgrade-guide/3.0/

Here are most of the changes done for oskari-frontend: https://github.com/oskariorg/oskari-frontend/pull/468 

## 1.47.0

### AppSetup migration (OpenLayers 4)

Oskari 1.47.0 drops support for OpenLayers 2 (https://github.com/oskariorg/oskari-docs/issues/63). Existing AppSetups
 of type 'DEFAULT' and 'USER' are automatically migrated to use the OpenLayers 4 implementations of the bundles. 
The other types (PUBLISH and PUBLISHED) have been using OL 4 for a long time already and don't need the migration.

Note! Any app using custom view types needs to make an app-specific migration for the custom types. 
Copy-pasting the migration (content-resources/src/main/resources/flyway/oskari/V1_47_3__migrate_appsetup_to_ol4.sql) and
 adding the types to the where-clause at the beginning should suffice: 
 
    type IN ('DEFAULT', 'USER') AND -- is right view type

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
