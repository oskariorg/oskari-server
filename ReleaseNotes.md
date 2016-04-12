# Release Notes

## 1.36

### Generic

Geotools version has been updated to 14.2.
See MigrationGuide.md for details.

### service-map

A bugfix to legend image parsing when there were multiple styles with the same name.

### Database

Added indexes for oskari_resource and oskari_permission tables.

### service-search-opendata

New url parameter **&what3words** for positioning the map in startup
e.g. http://www.paikkatietoikkuna.fi/web/en/map-window?ver=1.17&zoomLevel=6&what3words=examine.flying.daytime&mapLayers=base_35+100+default&showMarker=true

### control-base

#### MapfullHandler 

WfsLayerPlugin config can now be configured with oskari-ext.properties if defaults are not working for your environment:
 
    oskari.transport.domain=http://localhost:9090
    oskari.transport.url=/mytransport

These will write the host and contextPath to the plugins config if they are not configured in database view.

Populate missing projection definitions for mapfull config projectionDefs. It uses the projection defs in: 
    control-base\src\main\resources\fi\nls\oskari\control\view\modifier\bundle\epsg_proj4_formats.json

Populate svgMarkers for mapfull config, it uses for populate SVG markers JSONArray in: 
    control-base\src\main\resources\fi\nls\oskari\control\view\modifier\bundle\svg-markers.json

#### GetWFSDescribeFeatureHandler

Now returns now exact xsd types for feature properties

Earlier version responsed generalized types (text or numeric).
New extra request parameter  `&simple=true` is available for the earlier response behaviour

#### ActionConstants

Changed PARAM_SRS value from "epsg" to "srs". This affects GetMapLayers which now assumes the projection is sent in srs-parameter. 
The parameter in most action routes for transmitting projection information is "srs" so this is a consistency improvement.

#### CoordinatesHandler

New action route. Transforms point-coordinates from projection to another. 
Transformation class can be configured with property `projection.library.class` (defaults to `fi.nls.oskari.map.geometry.ProjectionHelper`).
Takes `lan`, `lot`, `srs` and `targetSRS` parameters and returns a JSONObject with transformed result:

      {
          lan: 123,
          lot : 456,
          srs : "EPSG:789"
      }
      
#### GetReverseGeocodingResultHandler

New action parameter **channels_ids** for selecting a spesific reverse search channel(s) instead all available channels
e.g. &action_route=GetReverseGeocodingResult&lang=fi&epsg=EPSG:3067&lon=368978.93&lat=6670688.861&channel_ids=WHAT3WORDS_CHANNEL

#### CoordinateToolHandler

When coordinatetool bundle is part of the setup. And it has configuration to do client-side transforms the handler populates
 missing projection definitions for mapfull config projectionDefs. It uses the same mechanic as mapfullhandler and the 
 same projection defs in:
 
    control-base\src\main\resources\fi\nls\oskari\control\view\modifier\bundle\epsg_proj4_formats.json

#### AppSetupHandler

Coordinatetool is now allowed bundle for publisher.
    
#### GetPermissionsLayerHandlers

Additional permissions can now be configured with oskari-ext.properties:
 
    permission.types = EDIT_LAYER_CONTENT
    permission.EDIT_LAYER_CONTENT.name.fi=Muokkaa tasoa
    permission.EDIT_LAYER_CONTENT.name.en=Edit layer

These permissions will then be shown by the admin-layerrights bundle in Oskari frontend.

### transport && control-base

**WFS-T**  functionality is added to oskari-server package.
User roles can be granted a EDIT_LAYER_CONTENT permission that will enable them to edit features on a WFS-service.
This requires the content-editor bundle in Oskari frontend to provide the user-interface.

## 1.35.1

### generic

Apache commons-collections library upgraded 3.2.1 -> 3.2.2 for security reasons. 

### service-search-nls

Enabled reverse geocoding for ELFGeoLocatorSearchChannel.

### control-base

Openlayers3 sends WTMS-request parameters in camelCase while Openlayers2 always sends params in CAPS. 
GetLayerTileHandler has been modified to accept wmts-parameters in any letter case.

## 1.35

### service-search-nls

Added data identification date and type to metadata search results.

### servlet-map

Now expects UTF-8 input and writes UTF-8 as output.
Fixes an issue where user-generated my places with name containing non-ascii characters prevented IE11 from showing my places.

### control-base

MapfullHandler now fills in missing projection configurations for mapfull bundle/proj4js when a view is loaded:

    {
        "projectionDefs": {
            "EPSG:4326": "+title=WGS 84 +proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs"
        }
    }

These can still be configured to the database as part of mapfull-bundles config and database are used when configured.
The automation uses configurations from:

    control-base\src\main\resources\fi\nls\oskari\control\view\modifier\bundle\epsg_proj4_formats.json

Missing configurations can be added to the file or to the view in database.

### service-search

SearchableChannel interface changes:
- Removed deprecated method - handle channel specific config in channels code instead:

    void setProperty(String propertyName, String propertyValue).

- Added SearchChannel methods to SearchableChannel interface:
```
    void init(); // any initialization should be performed here
    Capabilities getCapabilities(); // COORD, TEXT or BOTH. Defaults to TEXT on SearchChannel
    boolean isValidSearchTerm(SearchCriteria criteria); // defaults true on SearchChannel
    void calculateCommonFields(final SearchResultItem item); // Setup zoomlevel based on item type on SearchChannel
    ChannelSearchResult reverseGeocode(SearchCriteria criteria) throws IllegalSearchCriteriaException; // reverse geocode impl
```
Any legacy SearchChannel implementation should implement these or inherit the SearchChannel class for defaults.
SearchChannels can now be used for text and/or reverse geocoding. The capabilities should be used to indicate if the
 implementation provides one or both.

### service-base

Added IOHelper convenience method for just adding one param to an URL:

    public static String addUrlParam(final String url, String key, String value)
    String url = IOHelper.addUrlParam("https://google.com", "q", "test");
    
### service-map

Data import is improved

1. Long files names do not break the import any more.
2. There is no empty my data layer any more, if feature geometry import fails.

### service-search-opendata

New maven module for any open data sources usable for searches.
New search channel implementation for What3Words service. Apikey is required:

1. Get it from here https://map.what3words.com/register?dev=true
2. Configure it to oskari-ext.properties:
```
    search.channel.WHAT3WORDS_CHANNEL.service.apikey=[YOUR APIKEY]
    actionhandler.GetSearchResult.channels=[Add "WHAT3WORDS_CHANNEL" to list]
```
The channel can also be used for reverse geocoding (for example with findbycoordinates frontend-bundle).

### control-base

Action route "GetReverseGeocodingResult" doesn't need the channels configuration any more:

    actionhandler.GetReverseGeocodingResult.channels=NLS_NEAREST_FEATURE_CHANNEL

The action route uses all search channels that have the reverse geocoding capability by default.
The channels-property can be used as whitelist search channels to pick which ones to use.

### control-example

OpenStreetMapSearchChannel moved to service-search-opendata which is now a dependency for control-example.

### servlet-transport

The init-command now uses layer visibility setting and prevents calls to wfs-service for layers that are hidden.

### Database changes

oskari_jaas_users tables login/password field types changed to text to not needlessly restrict the length of password (or username).

### ParamHandler interface

Parameter handlers now implement the Comparable interface and are sorted by priority order (prio 1 is executed before prio 500):
- The default priority is 500
- coord and address handlers have priority of 10 so by default they are executed before others
- showMarker and isCenterMarker have much lower priority since they use the map center information
 and any handler that modifies the map location need to be executed before these

### Analysis

Geometry clip method (IntersectionFeatureCollection2) improved in Geoserver wps methods

### service-csw

Fixed the datasource reference issue in CSWCoverageUpdateService. The scheduled job now gets datasource the same way as
 flyway db migrations.

## 1.34.1

### control-base

CreateAnalysisLayer now copies the DOWNLOAD permission from original layer as well as VIEW_LAYER and VIEW_PUBLISHED.

### servlet-map/webapp-map

JSP-files no longer link startup.js as it's no longer needed.

### servlet-map

View loaded by the viewId-parameter now works again for views that have disabled the only_uuid flag.

## service-webapp

Flyway-migration can now be disabled by using oskari-ext.properties with

    db.flyway=false

Using more than one server and starting them simultaneously might result in deadlock on flyway database tables.
You can use this as a workaroung to have one node migrate the database and the other ones configured to ignore
 the migration using this property.

## 1.34

### webapp-map

Moved override JSP-files from webapp/jsp to webapp/WEB-INF/jsp as this is the default override location for JSP-files.

### servlet-map

Disabled X-Frame-Options header set by spring security by default. It prevented published maps from loading when used
 directly from Jetty without front-proxy (for example localhost) to override this behaviour.

Published maps are now checked for referer domain correctly when opened.
 To disable check you can define unrestricted domains in oskari-ext.properties (* = allow all):

    view.published.usage.unrestrictedDomains=localhost, my.domain.com

### Transport

New property for wfs read timeout in transport-ext.properties

#### Wfs read response timeout default is 30000 ms (use together with oskari.transport.job.timeoutms)
oskari.wfs.read.timeout=20000
#### Transport Job execute timeout  default is 15000 ms
oskari.transport.job.timeoutms=25000

## 1.33.2

Re-run fixed version of a flyway migration pre-populating capabilities information in the database (oskari_maplayer.capabilities).
 The previous script didn't work correctly for layers having capital letters in the URL since the URL is normalized to
 lowercase in oskari_capabilities_cache.

## 1.33.1

Legend image handling fixed for layers that require credentials. Style-specific legends are now used correctly when proxying.

## 1.33

### service-routing (POC)

Changed service to support OpenTripPlanner services. Service parses data to plan, requestParameters and success parameter.
If OpenTripPlanner response contains error block then success response param is false and there are no plan block

Service response is same as OpenTripPlanner response with these conditions:
- point lat and lon coordinates are transferred to map projection
- geoJSON presentation added to each itinerary (plan/itineraries/itenerary) . GeoJSON tells whole route each itinerary.
- geoJSON presentation added to each leg legGeometry (plan/itineraries/itinerary/legs/leg/legGeometry). GeoJSON tells leg route.

To be able to use this you need to have the following parameters defined in properties:
- routing.url (route service url)
- routing.srs (coordinate system used by route service provider)
- routing.default.maxwalkdistance (default max walk distance in meters)
- routing.default.mode (default mode)

Optional parameters in properties:
- routing.user (username required by the route service provider)
- routing.password (password required by the route service provider)
- routing.forceXY (force change XY axels)

### content-resources

Fixed earlier Java-based flyway migrations to use SQL instead of existing services. This enables smoother upgrade
 experience since SQL targets the versioned schema while services assume the most recent schema when used.

Removed outdated view description files from resources/json/views.

Flyway migration for Oskari core db is ran when setup files have the base database created (create, setup, bundles phases have been run).
 To disable the migration in "partial" setup scripts, these need to be tagged with "isPartial" : true on the JSON.

Added migration helper for handling the replacement of publisher bundle with publisher2. The sample flyway module has
an example V1_0_5__publisher2_migration.java how to use it in application installations.

Added a temporary setup-script for an app using Openlayers 3 components on published map (setup/app-tmp-ol3.json). This
will be modified and removed once the OL3 functionality reaches maturity. After that the original publisher template
 will be modified to use OL3.

### servlet-map

Now prevents view loading with id when onlyUUID-flag in on.

### control-base

#### GetMapLayersHandler

Now provides prefixed urls for maplayers is request.isSecure() or parameter ssl=[true|false] is provided.
The prefix is configurable in oskari-ext.properties (defaults to https://):

    maplayer.wmsurl.secure=/secure/

This handling was already present for selected layers and now it's used for GetMapLayers also.
The functionality removes the protocol part of layer url and servers the url prefixed by the value defined in properties.
This enables custom proxying for services that don't have https enabled.

#### GetLayerTile

Added handling for WMTS-layers with resourceURL.

#### GetWSCapabilities

When adding layers the capabilities parser now includes layer styles and infoformats correctly.

#### CreateAnalysisLayerHandler

Improvements in analysis methods:

 - Better management of unauthorized data

 - Aggregate, Spatial join and Difference methods improved

 - sld_muutos_n1.sld  style updated in Geoserver Styles / used in analysis method difference

### service-map

LayerJSONFormatterWMTS now includes tileUrl to JSON for layers with resourceURLs. The browser code uses this if present,
but defaults to the basic url. This means that proxying WMTS-layers with resourceURLs now work correctly.

GetGtWMSCapabilities now includes method to parse String into WMSCapabilities to make it work better with cached
 capabilities XMLs from CapabilitiesService. Also added styles parsing from capabilities.

WMS capabilities are now parsed when layers are added (previously when they are loaded by user). Pre-parsed capabilities
are saved to database table oskari_maplayer in the capabilities column. Flyway migration has been implemented for existing layers.

OskariLayer now has a field for pre-parsed capabilities JSON. This is generated when saving a layer and served to browser
 instead of parsed just-in-time when layers are loaded.

LayerJSONFormatter now has convenience methods to operate the "admin" only data.

### Default view functionality

Added functionality for saving / restoring a user defined default view.

Loading the system default view can be forced by using an additional URL-parameter 'reset=true'. This is useful if the
personalized view is faulty.

## 1.32.2

Fixed an issue where unexpected zip contents could result in an infinity loop in CreateUserLayerHandler.

Error handling improved in analysis functionality.

## 1.32.1

### database/flywaydb

1.32.4 script goes through all registered WMTS-layers and resolves resourceURL information for them.
Updates the options database column when needed.

### servlet-map

URL-parameters are now properly handled again (fixes the link tool).

### service-map

If a capabilities document is saved in the database, it will no longer be overwritten with an empty document when capabilities fetch
 timeouts or in other problem scenarios.

Capabilities fetch default timeout increased from 15 seconds to 30 seconds. Still configurable in oskari-ext.properties:

    # seconds for timeout
    capabilities.timeout=30

Improved feature id handling in query filters (fi.nls.oskari.wfs.WFSFilterBuilder)

### service-spatineo-monitor

SpatineoServalUpdateService now cleans up the datasource it uses correctly.

### control-base

SaveLayer now generates resourceURL information for WMTS-layers and saves them in layers options-field.

GetMapLayers now include the original legendimage urls for password protected layers for users that have permission to edit layers.
This fixes an issue where legend image was overwritten with the proxy url when editing layers.

GetLayerTile now supports style-specific legendimages.

### servlet-printout

Servlet-printout now uses options from layer JSON to get WMTS resourceUrl specific information (previously used the Openlayers2 specific JSON capabilities).

Added initial support for WMTS-layers using KVP urls.

## 1.32

### Geoserver REST client and setup webapp

geoserver-ext/geoserver-rest-client now has a simple REST client for Geoserver. It's used by content-resources
 GeoserverPopulator which can be used by a new webapp named "setup". This enables you to setup geoserver for myplaces,
 analysis and userlayers with the projection that is needed and also adds datastores with the credentials and url as
 configured in oskari-ext.properties.

 To use the setup webapp copy the setup.war under oskari-server/webapp-setup/target/ to {JETTY_HOME}/webapps. Then access
 Jetty in http://localhost:8080/setup (default url, modify host/port if needed). It shows the geoserver specific properties
 needed for by the client and asks for projection. When it completes it shows a message indicating success/error and
  properties that need updating. This also updates the baselayers in oskari_maplayer database table for projection,
   geoserver url and credentials.

The relevant properties are:

    geoserver.url=http://localhost:8080/geoserver
    geoserver.user=admin
    geoserver.password=geoserver

After running the setup you should delete the setup.war under {JETTY_HOME}/webapps since access is not restricted in any way.
Tested on Geoserver 2.7.1. Atleast on 2.5.2 the REST API is a bit different so this might not work correctly
 (namespace for datastore is handled with uri instead of prefix to be more specific).

### servlet-transport

Improved error handling when client disconnects before WFSJob finishes.
References for jobs were not properly cleared which resulted in memory leaks.

### control-example/OpenStreetMapSearchChannel

The search channel should now properly handle coordinate transforms even if coordinate order is forced in geotools using
the system property "org.geotools.referencing.forceXY". When using projection that is affected by this setting and have
the system property, you need to define an override in oskari-ext.properties:

    search.channel.OPENSTREETMAP_CHANNEL.forceXY=true

Using the system property might affect other parts of Oskari as well. We will fix the issues as they are noticed.

### Database initialization

Changed default views to show two OpenStreetMap layers. Also changed map coordinate reference system from EPSG:3067 to EPSG:4326.

### service-base

IOHelper now throws an IOException when getting a HTTP 401 response instead of return the string "401".

### service-map

fi.mml.map.mapwindow.service.db.CapabilitiesCacheService (and -IbatisImpl) has been moved to a new package: fi.nls.oskari.service.capabilities.
CapabilitiesCacheService.getCapabilities() returns cached capabilities from the db or if not present queries the service and updates the database.
The capabilities network request timeouts after 15 seconds by default. You can configure the timeout with oskari-ext.properties:

    # seconds for timeout
    capabilities.timeout=15

The database table portti_capabilities_cache is replaced with oskari_capabilities_cache table. Capabilities are
 mapped based on service url and type (WMS/WMTS) instead of layer ids to prevent duplication. The migration for 1.32 will
 take some time since the cache is prepopulated from the services as a flyway migration. This depends on the amount of layers that need to be fetched.

WMTS layer capabilities have been dropped from the oskari_maplayer table. The are now cached as the original XML in oskari_capabilities_cache.
 This makes Openlayers 3 migration easier since the JSON was OL2 specific.

Added time dimension support for WMS layers. WebMapService and JSONs for layers now include time parameter from layers getCapabilities.

### control-base

Removed fi.nls.oskari.util.GetWMSCapabilities. Functionality has been moved to
 fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceIbatisImpl in service-map.

New action route 'GetLayerCapabilities' returns the cached capabilities for a registered layer (if the user has
 permission for requested layer). This enables Oskari to get rid of Openlayers 2 specific JSON format for WMTS-layers.

GetLayerTile no longer tries to connect a service when url (usually legendimage) is null or empty.

### content-resources

Moved DataSourceHelper to service-base. It's now a singleton.

### servlet-transport

Fixed an issue where map click handling assumed metric coordinate units.

Improvements in the boundary tile check

### Analysis  / aggregate method

Resultset content format is changed. There is now one record for each property with aggregate function values

## 1.31.2

Fixes an issue where users myplaces layers were not tagged as "published" when used in an embedded maps. This
prevented using previously unpublished myplaces layers in embedded maps.

## 1.31.1

Fixed portti_view metadata column upgrade so it's part of the "empty db" setup so views can be inserted by DBHandler.
  Flyway SQL-script changed to a Java-upgrade which checks if the column is already part of the setup.

## 1.31

### control-admin

Added "Metrics" action route that is accessible by admins. This returns metrics in JSON format including metrics for:
 - processing times and call counts for action routes
 - processing times and call counts for proxied layer tiles
 - garbage collector
 - threads
 - memory

### service-map

BundleService can now be instructed to cache bundle templates.

Added a metadata field for Views. This will be used to store for example publisher specific information about the view.

### service-base

ConversionHelper.asSet(T... type) added as a convenience method to get arrays as sets.

### content-resources

DBHandler now allows further customization using command line parameters/env properties (thanks hatapitk):

1) Using env property 'oskari.resourceOverlayDir' one can override default setup files by providing an overriding file
 with the same name and in the same directory structure in the referenced 'overlay' directory.

2) Maven assembly plugin can be used to create a bundled runnable jar for standalone use

3) Command line parameter can be used to reference an override properties-file to be used to f. ex. provide database credentials.

See oskari-server/content-resources/README.md for details.

### service-control

#### Added white-/blacklist capabilities for action handlers.

To blacklist/block action routes define existing route keys as comma-separated value for property:

    actioncontrol.blacklist=Users, ManageRoles

To whitelist (use only mentioned) action routes define existing route keys as comma-separated value for property:

    actioncontrol.whitelist=GetAppSetup, GetSupportedLocales

Note! This can also be used to replace existing handler with a custom implementation by blacklisting the existing one and
programmatically adding the custom implementation on startup by calling (true as third parameter to skip black/whitelist check):

    ActionControl.addAction("ActionKey", new MyActionHandler(), true);

#### ActionParameters - Added a convenience method for boolean type params

    boolean bln = actionParams.getHttpParam("booleanParamKey", true);

### Added metrics for ActionControl

Metrics for action route handling (processing time/call counts) are now recorded by default in ActionControl. To
 disable metrics gathering add this configuration to oskari-ext.properties:

    actioncontrol.metrics=false

The gathered metrics are available by calling ActionControl.getMetrics().

### control-base

GetAppSetupHandler now updates views usages to portti_view usagecount and used columns.
* usagecount column tells how many times view has been used
* used column tells the last time when view was used

CreateAnalysisLayerHandler can now be used to generate aggregated values without saving as analysislayer.

GetLayerTileHandler can now be used to get legendImages which need authentication via proxy.

GetLayerTileHandler now has default timeouts for connect (1 second) and read (5 seconds). These can be configured
 in oskari-ext.properties (defined in ms):

    GetLayerTile.timeout.connection=1000
    GetLayerTile.timeout.read=5000

GetLayerTileHandler now records metrics for proxied services by default. To disable metrics gathering add this
 configuration to oskari-ext.properties:

    GetLayerTile.metrics=false

### servlet-map

The servlet-map module has been replaced with Spring-based servlet (https://github.com/nls-oskari/oskari-spring/).
It uses programmatic initialization instead of a web.xml and can utilize SAML-security module (servlet-saml-config),
 but drops configurable database JNDI-names and JAAS-support. JNDI-names need to be configured in Ibatis SQLMapConfig.xml
 in addition to oskari-ext.properties if not using the defaults. Ibatis will be replaced with Mybatis in the future which
 will solve this issue.

 See MigrationGuide.md for further info.

Added localization support for server-side HTML/Login form.

TODO:
- Thymeleaf support
- LDAP login
- spring-boot setup (or otherwise try to restore standalone-jetty packaging)
- documentation of SAML features to oskari.org
- documentation about customizing the webapp
- Mybatis at least for Userlayers/Analysis (Myplaces already migrated)

### servlet-saml-config

New module providing SAML2 support for servlet-map. Add it to your webapp with servlet-map to gain SAML-functionality:

        <dependency>
            <groupId>fi.nls.oskari</groupId>
            <artifactId>servlet-saml-config</artifactId>
        </dependency>

### webapp-map

Has been updated to use the new servlet-map.

### standalone-jetty

Moved out of oskari-server modules and Jetty-bundle from oskari.org should be used instead.

### service-webapp

This new module has some common helper classes for webapps.

### service-myplaces

Myplaces services have been moved from service-map to service-myplaces.
The database access library has been updated from Ibatis to Mybatis.

### service-routing (POC)

New service requests route from the defined route service provider and parses data to geoJson and route instructions.
Uses OpenTripPlanner route interface by default.
To be able to use this you need to have the following parameters defined in properties:
- routing.url (route service url)
- routing.user (username required by the route service provider)
- routing.password (password required by the route service provider)
- routing.srs (coordinate system used bu route service provider)

### control-routing (POC)

New action route "RoutingHandler" added for handling route request.
Gets route parameters from frotend end returns route geometry as geoJson and route instructions as json.

### Library upgrades

Servlet-API upgraded from 2.4 to 3.1.0 in preparation of replacing current servlet-map/webapp-map with spring
 counterparts from oskari-spring repository.

Other updates:
* Jackson 1.9.11 -> 2.5.4
* Jedis 2.6.0 -> 2.7.2
* Axiom 1.2.14 -> 1.2.15
* org.codehaus.woodstox:stax2-api 3.1.1 -> 3.1.4
* com.fasterxml.woodstox:woodstox-core 4.4.1 -> 5.0.1

Note that both Jackson 1.x and 2.x are used currently. 1.x is mostly used in WFS/transport since CometD needs it.

### Property changes

db.additional.pools has been changed to db.additional.modules to better describe it.
The default value is the same (under servlet-map/src/main/resources/oskari.properties).
It is now used to keep track of DB-modules for the FlywayDB migration.

### Automated database upgrade

The database is now automatically upgraded using FlywayDB library. The default upgrade setup is configured in
servlet-map/src/main/resources/oskari.properties and the migration is triggered by
fi.nls.oskari.map.servlet.OskariContextInitializer. The database is separated to 4 modules: oskari, myplaces, analysis
 and userlayer. Each has its own status table for keeping track of the database.

Application specific update scripts can be added by adding a module in the property:

    db.additional.modules=myplaces,analysis,userlayer,myapplication

This will result a table called oskari_status_myapplication to the database and migration scripts will be searched
 from the classpath under the path /flyway/myapplication by default. The scripts are executed with the default Oskari
 datasource. To customize the used datasource, script locations, status table name in the database define these
 properties in oskari-ext.properties:

    db.myapplication.jndi.name=jdbc/MyApplicationDS
    db.myapplication.url=[db url]
    db.myapplication.username=[db user]
    db.myapplication.password=[db pass]
    db.myapplication.status_table=my_status_table
    db.myapplication.script.locations=/flyway/myapplication,/upgrade/scripts/in/here/also

For further information about script naming etc see http://flywaydb.org/

### servlet-transport

Session id is now always sent as cookie when getting layer permissions. The default cookie name is 'JSESSIONID' and can be
overridden in transport-ext.properties with 'oskari.cookie.session' as before.

### flyway migrates

* add coordinatetool bundle to portti_bundle table
* add used and usagecount for portti_view

## 1.30.1

### servlet-transport

No longer sends session id as part of url, but as cookie.

## 1.30

### Generic

Geotools version has been updated to 13.1.
The new Geotools version no longer supports Java 6 so Oskari now requires Java 7 as well.
Geoserver and WPS extensions have been upgraded for version 2.7.1.
See MigrationGuide.md for details.

ASDI application specific artifacts have been removed from oskari-server.
They can now be accessed in: https://github.com/arctic-sdi/oskari-server-extensions

### service-search-nls

ELFGeolocator can now be configured to other projections than the default EPSG:4258.
This is done with by providing the srs name as a property value with key 'search.channel.ELFGEOLOCATOR_CHANNEL.service.srs'

### service-base

PropertyUtil now has a convenience method for getting properties that might be localized:

    final Object urlObj = PropertyUtil.getLocalizableProperty("oskari.map.url", null);
    // single value configured
    if(urlObj instanceof String) {
        JSONHelper.putValue(config, "url", urlObj);
    }
    // localized values configured
    else if(urlObj instanceof Map) {
        Map<String, String> values = (Map<String, String>) urlObj;
        JSONHelper.putValue(config, "url", new JSONObject(values));
    }

This will result in { "url" : "single value" } or

    { "url" : {
         "en" : "en value",
         "fi" : "fi value"
      }
    }

With properties 'oskari.map.url' for single value and 'oskari.map.url.en' and 'oskari.map.url.fi' for multiple values

#### Mif/mid data import

If GDAL cannot determine CRS from the data, the import now assumes the current maps CRS (previously assumed EPSG:2393).

### control-base

MapfullHandler now populates map link and terms of use urls for LogoPlugin config based on properties if available:

    oskari.map.url=/
    oskari.map.terms.url=/terms

Properties can also have localized values with keys like 'oskari.map.terms.url.en' and 'oskari.map.terms.url.fi'. Existing
 config will NOT be overwritten, the values are only populated if they don't exist in the database for the view.

## 1.29

### service-control

ActionParameters now has a getAPIkey() method. Currently returns session id.

### control-base

GetAppSetup now includes an apikey in user data.

### webapp-transport

Now builds transport.war instead of transport-0.0.1.war as this is the default Oskari frontend uses.

### content-resources

Separated userlayers triggers to a separate file and created a setup.json for other userlayer related things.
The default setup (content-resources/src/main/resources/setup/app-default.json) now populates the database with
more content than before and creates tables for analysis and userlayers as well as myplaces.

It also setups 3 views with different levels of Oskari installations:
1) view that includes bundles that can be used with having just webapp-map (default-view.json)
2) publisher template (publisher-template-view.json)
3) view that includes bundles using webapp-map and transport (requires redis as well) (default-transport-view.json)
4) view that includes the whole stack: webapp-map/transport/printout/geoserver (requires redis as well) (default-full-view.json)

These can be accessed by adding url parameter viewId with value of the view number listed above (for example viewId=4).
The view definition files can be found in content-resources/src/main/resources/json/views.

The myplaces/userlayer/analysis baselayers SQLs have been updated to point to a geoserver running on
http://localhost:8080/geoserver (previously the same, but port 8084).

#### New WFS 2.0.0 initial parser config table  (oskari_wfs_parser_config)
Look at MigrationGuide.md

### control-base
Improvements in Excel/csv export (metadata request url, expanding object column values, reforming jsonarrays)'

### service-feature-engine
Improved the new generic WFS path parser for complex featuretypes (WFS 2.0.0  services)
Instruction under oskari.org\md\documentation\backend\configuring-wfs-path-parser.md
Initial WFS 2.0.0 parser configs are now in new DB table oskari_wfs_parser_config

## 1.28.1

### content-resources

Fixed database create script to include the new column:

    content-resources/src/main/resources/sql/PostgreSQL/create-wfs-tables.sql

This fixes the initial database creation on new installs.

## 1.28

### DB upgrades

#### Generic attributes field added for maplayers

* Typed as 'text', contains JSON describing the layer.
* Enables heatmap-enabling parameters to be saved for layer:

    {
        geometryProperty : 'geom',
        heatmap : ['property1', 'property2', 'similar to wfs selected properties']
    }

Run on oskaridb:

    content-resources/src/main/resources/sql/upgrade/1.28/01_alter_table_oskari_maplayer.sql

#### Parse config field added for WFS FE generic path parser

Run on oskaridb:

    content-resources/src/main/resources/sql/upgrade/1.28/02_alter_table_portti_wfs_template_model.sql

#### Fixes findbycoordinates bundle registration

Previously Import-bundle statement had the value 'rpc'. Updates it to correct 'findbycoordinates'. Run on oskaridb:

    content-resources/src/main/resources/sql/upgrade/1.28/03_fix_findbycoordinates_bundle_registration.sql

### content-resources

New bundle: heatmap (see frontend notes for configuration options):

    content-resources/src/main/resources/sql/views/01-bundles/framework/037-heatmap.sql

Added an example SQL how to easily register a bundle and link it to a view:

    content-resources/src/main/resources/sql/example-bundle-insert.sql

### service-map

OskariLayers will now load the attributes value from DB and expose it with the same name in JSON-presentation.

GFI support is added for `arcgis93layer` type

### service-logging

Moved Log4JLogger from under servlet-transport to a new service so it can be used with other modules as well.

### service-base

Changed Job from abstract class to an interface and added AbstractJob to be a drop-in replacement for Job.

JSONLocalized class now tries to get the value with default language if requested language is not available. This
 helps when a language is added to Oskari installation and all data producers, Inspire-themes and maplayers lack
 the localized name.

JobQueue now removes any existing job with the same key when adding a job.

### control-base

GetReverseGeocodingResult configuration changed. Previously used search channel based properties for buffer and maxfeatures,
now they are configured for the actionhandler:

OLD:

    search.channel.<channel id>service.buffer=1000
    search.channel.<channel id>service.maxfeatures=1

NEW:

    actionhandler.GetReverseGeocodingResult.maxfeatures=1
    actionhandler.GetReverseGeocodingResult.buffer=1000

The maxfeatures is channel based currently so you will end up with a result count of (maxfeatures * channels configured).
This will most propably be changed to an enforced limit across channels in the future.


CreateUserLayer now support files whose names contains dot(s).


### control-example

GetArticlesByTag can now be configured to serve files in classpath. The handler gets a comma-separated list of tags as
parameter such as "userguide,en". This is used as filename, but characters ' ' (space), ',' (comma), '.' (dot), '/' (slash)
and '\\' (backslash) are replaced with '_'. Initially tries to load filename with .html extension then with .json extension.
To get an article (with example tags above) this way requires a file named "userguide_en.html" or
"userguide_en.json" in a classpath under a directory configured in oskari-ext.properties like this:

    actionhandler.GetArticlesByTag.dir=/articlesByTag/

### service-search

Removed deprecated classes: `MetadataCatalogueSearchCriteria` and `Csw202ResultsDoc`.
`SearchResultItem` now has toJSON() method to construct the response for searches in similar fashion all over Oskari.
`SearchWorker` now uses SearchResultItem.toJSON() to create the response.

### service-search-nls

Added a hook for custom result parser in `MetadataCatalogueChannelSearchService`. Result parser must extend/be assignable to
`fi.nls.oskari.search.channel.MetadataCatalogueResultParser` and can be configured with property:

    search.channel.METADATA_CATALOGUE_CHANNEL.resultparser=<fqcn extending fi.nls.oskari.search.channel.MetadataCatalogueResultParser>

`GetMetadataSearchHandler` now uses SearchResultItem.toJSON() to create the response.

MetadataCatalogueChannelSearchService now requests the output schema `http://www.isotc211.org/2005/gmd` instead of `csw:IsoRecord`.

### elf/geolocator search

Location type based scaling is available when locating the search item
Default setup is in ELFGEOLOCATOR_CHANNEL.json
Override setup will be set in oskari-ext.properties

(#) Optional setup for location type based scaling - default is oskari-server\service-search-nls\src\main\resources\fi\nls\oskari\search\channel\ELFGEOLOCATOR_CHANNEL.json
(#) e.g.
search.channel.ELFGEOLOCATOR_CHANNEL.service.locationtype.json=/opt/jetty/webapps/root/setup/test.json

### service-feature-engine

Added new generic WFS path parser for complex featuretypes
Instruction under oskari.org\md\documentation\backend\configuring-wfs-path-parser.md
Sample complex wfs layer insert script in \elf-oskari-server-extensions\elf-resources\resources\sql\elf-nls_fi-lod0ad-Cascading-wfslayer.sql

### servlet-transport

Moved duplicated code from `FEMaplayerJob` and `WFSMaplayerJob` to common baseclass `OWSMaplayerJob`.
Moved helper methods from OWSMaplayerJob to JobHelper class.
Removed job validation from OWS/FE/MaplayerJobs - validation should now be done by creating a JobValidator
with the job and calling validator.validateJob(). This enables custom handling for validation errors.
Added initial merge for ArcGis REST-layer support. There are still some missing parts which needs to
be included with documentation to make it usable.
Layer scale limit of -1 is now handled as no limit like in other parts of Oskari.
FeatureEngine jobs http requests now respect the timeout limits set with properties (ms values):

     oskari.connection.timeout=3000
     oskari.read.timeout=60000

Moved OWSLayerJob.Type enum to own file as JobType.

Maplayer jobs are now managed as Hystrix Commands (https://github.com/Netflix/Hystrix/wiki) instead of the custom
threaded approach using JobQueue in service-base. This should put less strain on overloaded services as requests are
short-circuited when problems occur.

Properties to configure job execution are:

    oskari.transport.job.pool.size=100
    oskari.transport.job.pool.limit=100
    oskari.transport.job.timeoutms=15000

Where pool size is the thread pool size, limit is queue to keep when all threads are in use after which jobs will be
rejected until threads become available. Any job will be canceled after timeoutms milliseconds if it hasn't completed until then.
Any errors occuring on job execution will trigger a message to the websocket error-channel.

Added metrics indicators with https://dropwizard.github.io/. Metrics can be accessed as JSON by
adding fi.nls.oskari.transport.StatusServlet to the web.xml (requires admin user to access the servlet).

    <servlet>
        <servlet-name>status</servlet-name>
        <servlet-class>fi.nls.oskari.transport.StatusServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>status</servlet-name>
        <url-pattern>/status</url-pattern>
    </servlet-mapping>

The JSON-format for metrics is not set in stone and can change in the near future.

Added a new TransportResultProcessor class which injects a requestId (received from client) to all responses.

Some refactoring for websocket messsage parsing.

Added a new status-channel for websocket-communication. Messages are sent to browser when a job starts and when it completes.
Also added a requestId attribute for requests which is passed on in responses so client knows for which request the response is.

Errors while making requests/parsing features are now throwing exceptions instead of acting as everything is ok. This triggers
the correct error handling when tracking request status.

### webapp-transport

Added the Hystrix stream servlet for Hystrix Dashboard usage. HystrixMetricsStreamServlet can be removed from the web.xml
 to disable this.

Added StatusServlet to expose metrics as JSON. It can be removed from the web.xml to disable the functionality.


### servlet-map

Added functionality for additional response headers when serving jsp pages.

    oskari.page.header.X-UA-Compatible = IE=edge

Log4JLogger is now accessible to servlet-map (new dependency service-logging).

PrincipalAuthenticationFilter now always trims usernames before using them (removes leading and trailing whitespace).

### geoserver-ext  / wps / analysis

Improvements in analysis / in spatial join method when second layer is analysis layer
- build "mvn clean install" in \oskari-server\geoserver-ext\wps\IntersectionFeatureCollection2 directory
  and copy IntersectionFeatureCollection2-2.5.2.jar from target directory to [GeoServer home]\webapps\geoserver\WEB-INF\lib
  and restart Geoserver (jetty)

## 1.27.1

Fixed ZoomParamHandler in control-base to use parameter as is without any special handling. The code previously
changed zoomLevel 8 to 7 if parameter "ver" was not included in the url. This kind of application specific parameter handling
 should be implemented as param-preprocessor functionality:

    http://www.oskari.org/documentation/backend/adding-action-routes (search for preprocessor)

## 1.27

### generic

Manifest.mf inside jars now include version, git commit id and timestamp of build.
ELF-specific artifacts have been moved to https://github.com/elf-oskari/oskari-server-extensions

### DB upgrades

#### Allowing more data to be stored for users

* Usernames in external systems can be longer than current column size - adjusted to 128 characters.
* User UUID should be unique - added constraint.
* Added column for users email.
* User can now store additional attribute data as JSON.

    content-resources/src/main/resources/sql/upgrade/1.27/03_alter_table_oskari_users.sql

### service-users

DatabaseUserService now has a method (saveUser) to ensure the user and his/her roles are up-to-date in the database (inserting if missing).
Usable when combined with external login service which should populate users to Oskari based on external user data (for example SAML-message).
Added support for saving and loading additional user attributes to/from database.
User email is now saved to the database.

### service-base

IOHelper now has convenience method to write the Content-type header.
HTTP params can now be generated to properly encoded string for encoding POST payload or GET querystring with IOHelper.getParams().
User class now has methods to attach additional attributes for the user.
PropertyUtil.getNecessary() has a new overloaded version that takes an optional detailed message. The message is attached to the
exception so it is shown in the server logs. Message should be used to tell why the property is necessary.
Added User.toJSON()/parse() and Role.toJSON()/parse() methods.

### geoserver-ext/OskariMarkFactory

OskariMarkFactory now follows Oskari versioning so changes are updated correctly when building a new version.

### control-base

New action route "GetReverseGeocodingResult" works as a generic reverse geocoding handler that uses search implementation
for getting actual results. Configure the channel to use with property (supports multiple channels as comma-separated list):

    actionhandler.GetReverseGeocodingResult.channels=<search channel id e.g. NLS_NEAREST_FEATURE_CHANNEL>

GetViews action route now requires a non-guest user.
GetCurrentUser now returns user data in response in addition to the UUID in the header.

### service-search-nls

Added a new search channel "NLSNearestFeatureSearchChannel" that supports reverse geocoding.
Uses the service at https://ws.nls.fi/maasto/nearestfeature and should be configured with properties:

    search.channel.NLS_NEAREST_FEATURE_CHANNEL.service.url=https://ws.nls.fi/maasto/nearestfeature
    search.channel.NLS_NEAREST_FEATURE_CHANNEL.service.user=<username>
    search.channel.NLS_NEAREST_FEATURE_CHANNEL.service.pass=<password>
    search.channel.NLS_NEAREST_FEATURE_CHANNEL.service.buffer=5000
    search.channel.NLS_NEAREST_FEATURE_CHANNEL.service.maxfeatures=1

### servlet-transport

Fixed an issue with default SLD. Added geometry type rules.
Feature engine parser factories are now syncronized (WFS feature parsing failed in some cases).
Feature engine GFI search tolerance fix.

## 1.26

### control-admin

Added a new ActionHandler for route 'SearchChannel'. This lists all annotated SearchChannels and their debug data.
Fixed an issue with Users action route where receiving empty password would update the password.

### service-map

Added a helper class for projection transformations: fi.nls.oskari.map.geometry.ProjectionHelper.

### service-base

PropertyUtil now always trims property values for leading and trailing spaces.
PropertyUtil now has a convenience method to get numeric properties as double precision.
Cache now correctly removes oldest cached item when it's overflowing.

### control-base

GetStatsTile no longer passes SLD-parameters twice. This makes the geoserver URL significantly shorter.

Fixed an issue on password protected layers proxy GetLayerTileHandler where the layer resource was used as a
class member. This caused random errors for loading layer tiles on protected layers.

### service-search

#### search channel changes

Added support for search result scaling by type. Deprecated zoomLevel for search result item as level
is dependent on number of zoom levels and service can be called from multiple maps having different number of
 zoom levels available. Scale can be configured for search channel by search result item type

    search.channel.[channel id].scale.[type]=1234

 and a generic default scale

    search.channel.[channel id].scale=4321

If scale configurations are provided the search results will include scale hints for the frontend in it's response.
Scale setup is automatic for search channels extending fi.nls.oskari.search.channel.SearchChannel and is based on
 items type and properties mentioned above. To change how scale is calculated in custom channel
 override the SearchChannel.calculateCommonFields(SearchResultItem item) method.

#### search channel debugging

Added an interface method that can be used to query configuration for search channels. To add channel specific debug data,
override the getDebugData() method and append the channel specific debug data to the map returned from super.getDebugData().

#### search result improvements

SearchWorker now adds zoomScale for results if it's present and a bbox if westboundlon is present. Search channels should
set the bbox properties only if they make sense.

### service-search-nls

Removed the hardcoded zoom levels for channel REGISTER_OF_NOMENCLATURE_CHANNEL in service-search-nls.
To get similar zoom functionalities for the search results using the channel, configure oskari-ext.properties with:

    # Luontonimet, maasto
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.300=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.305=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.310=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.315=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.325=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.330=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.335=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.340=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.345=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.350=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.430=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.390=11300
    # Luontonimet, vesistö
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.400=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.410=56650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.415=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.420=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.425=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.435=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.490=5650
    # Kulttuurinimet, asutus
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.540=56650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.550=56650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.560=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.570=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.590=2800
    # Kulttuurinimet, muut
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.110=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.120=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.130=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.200=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.205=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.210=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.215=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.225=11300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.230=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.235=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.240=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.245=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.320=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.500=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.510=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.520=5650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.530=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.600=28300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.602=56650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.604=56650
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.610=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.612=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.614=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.620=28300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.630=28300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.640=28300
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.700=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.710=2800
    search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.scale.Tie=2800

Harcoded zoom levels were also removed from KTJ_KII_CHANNEL channel. To get similar zoom functionalities for the search results
using the channel, configure oskari-ext.properties with:

    search.channel.KTJ_KII_CHANNEL.scale=1400

### servlet-printout

New printout properties to support GetLayerTile action route for authorised map tiles

    mapproducer.localurl.match=/web (prefixes to match as local resources)
    mapproducer.localurl.prefix=http://localhost:8080 (prefix to be added to local resource urls)
    mapproducer.referer=referer info for maptile requests
    mapproducer.logo=<resource-name-without-path> (relative-to fi/nls/oskari/printout/printing/page)

### Analysis  / Sector and zones method

Sector processing added to the "Sector and zones" method
    - make build under ..oskari-server/geoserver-exp/wps/ZoneSectorFeatureCollection path; mvn clean install
    - copy new ZoneSectorFeatureCollection2-2.5.2.jar to your geoserver/WEB-INF/lib path from the
      oskari-server/geoserver-exp/wps/ZoneSectorFeatureCollection/target path

Check that analysis intersection methods have uptodate .jar  in geoserver/WEB-INF/lib path
    - IntersectionFeatureCollection2-2.5.2.jar is the current version
    - if not, build a new version oskari-server/geoserver-exp/wps/IntersectionFeatureCollection2 path as above
      and copy to ...lib path

## 1.25.4

Fixes an issues with caching. Issue only affects relatively large caches (over 1000 map layers etc)

### service-base/Cache

Fixed a concurrency issue when cache limit was reached. Also added support for configuring cache-limits with properties:

    oskari.cache.limit.[cachename]=[limit]

Limit configured with properties is always used if configured (prefer config over code-assumptions).
If configuration doesn't exist code can try detecting reasonable limits automatically or use the default (1000).
Also log-level changed to WARN when a cache overflows since it most likely affects performance negatively.

### service-map/InspireThemeService

Now calculates cache-limit automatically based on the amount of map layers/inspirethemes are present.

## 1.25.3

### control-base/GetWMSCapabilities

Fixed a problem with updating layer capabilities into database. This affected 1.25.0-1.25.2. Any layers added
with the affected versions will not have for example GFI-capabilities. Open the layer in admin-layerselector
and save the layer (nothing needs to be changed in the form) to update the database and fix this issue.

## 1.25.2

### control-base/PublishHandler

Fixed an issue where editing an embedded map gave the user wrong URL for the edited map.

## 1.25.1

### control-base/GetWFSLayerConfiguration

Now always writes response to Redis (for transport).
Returns WFS-layer configuration as response for admin-users only (for admin-layerselector)

## 1.25

### DB upgrades and new configurations

#### Control embedded maps in iframes from the parent page

Add RPC-bundle to the publish template and all new embedded maps will get the functionality:

    content-resources/src/main/resources/sql/upgrade/1.25/01-add-rpc-to-publish-template.sql

To add the functionality to existing embedded maps, add the bundle to all views of type 'PUBLISHED'.

#### Move common layer properties to oskari_maplayer table

Run SQLs:

    content-resources/src/main/resources/sql/upgrade/1.25/01_alter_table_oskari_maplayer.sql
    content-resources/src/main/resources/sql/upgrade/1.25/02_update_oskari_maplayer.sql
    content-resources/src/main/resources/sql/upgrade/1.25/03_update_oskari_resource.sql
    content-resources/src/main/resources/sql/upgrade/1.25/04_drop_columns_portti_wfs_layer.sql

#### Replace unused portti_maplayer_metadata with oskari_maplayer_metadata

The table is populated by scheduled job described in service-cws:

    content-resources/src/main/resources/sql/upgrade/1.25/05-create-maplayer-metadata-table.sql

Add a property for scheduling in oskari-ext.properties:

    oskari.scheduler.job.CSWCoverageImport.cronLine=0 1 * * * ?

The CSW service is configured by:

    service.metadata.url=http://www.paikkatietohakemisto.fi/geonetwork

#### Populate unique UUIDs for views

Oskari 1.25+ will reference views with their UUIDs rather than ids. Loading a view with id is still supported.
Run the node.js upgrade script under content-resources/db-upgrade:

    SCRIPT=1.25/01-generate-uuids-for-views node app.js

NOTE! This will replace any existing UUIDs (they haven't been used in Oskari before).
After this, you can add a constraint for portti_view by running the SQL in:

    content-resources/src/main/resources/sql/upgrade/1.25/06-add-uuid-constraint.sql

#### embedded maps urls

Publisher and personaldata bundles in frontend now use embedded map urls provided by backend. The URLs will now always use
 the views UUID instead of ID and the above database changes will generate unique UUIDs for all present views. PublishHandler
 will also generate UUIDs for all new published views. To configure correct urls based on your environment you can configure:

    view.published.url=http://myhost/${lang}/${uuid}

or for heavily language-specific urls:

    view.published.url.fi=http://myhost/kartta?uuid=${uuid}
    view.published.url.sv=http://myhost/kartfonstret?uuid=${uuid}
    view.published.url.en=http://myhost/map-window?uuid=${uuid}

Both accept also URLs without protocol (//myhost/map) or host (/map)- frontend will include the missing parts based on browser location.
If the above are not configured the URLs default to using:

    oskari.domain=http://localhost:2373
    oskari.map.url=/

The above property values are combined: oskari.domain + oskari.map.url + "?lang=${lang}&uuid=${uuid}

Note! Views added by 1.25.0 can only be loaded by it's uuid. To make a view available by viewId
change the boolean flag "only_uuid" in portti_view database table. Exception to this is any view defined as
default view.

#### Streamlining view tables in database

The portti_view and portti_view_supplement have had 1:1 relation. To remove complexity portti_view_supplement has now
been removed and the columns that are actually used have been moved to portti_view with same names (except pubdomain -> domain):

    07_alter_table_portti_view.sql

#### Published maps

Removed redundant marker button from published map tools.
Run the node.js upgrade script under content-resources/db-upgrade:

    SCRIPT=1.25/02-remove-marker-buttons-from-published-views node app.js

This will remove marker-tool from previously published maps.

### servlet-map/PrincipalAuthenticationFilter

AuthenticationFilter can now be configured to use lowercase usernames when querying database for users and
adding users to database. To enable this add a property to oskari-ext.properties:

    auth.lowercase.username=true

Notice that when using this check that the existing usernames in database are in lowercase format. This can be useful if
the authentication module (like JAAS-LDAP) handles usernames case-insensitively.

### standalone-jetty

Fixed an issue with user logout functionality.

### service-cws

Moved CSW related code from service-map to a new module.

Includes a scheduled job to update coverage data for layers with metadata-identifier. Coverage data is stored in
oskari_maplayer_metadata as WKT in EPSG:4326 projection.

### service-scheduler

Added basic scheduler functionality as a common service package. See README.md in service-scheduler for details.

### service-spatineo-monitor

Added a new scheduler task for utilising a service availability functionality provided by Spatineo.
Not included by default in servlet. See README.md in service-spatineo-monitor for details.

### service-map

GetGeoPointDataService now uses credentials for layer when making a GetFeatureInfo request to a WMS service.

Improved GPX data import.

Added a simple helper class for projection transforms and WKT handling: fi.nls.oskari.map.geometry.WKTHelper

Layer coverage data is now loaded from oskari_maplayer_metadata based on metadataid
(previously from portti_maplayer_metadata based on layerId). Coverage data is transformed to requested projection
when layers are loaded.

### service-base

fi.nls.oskari.domain.map.view.View now has a method getUrl() which returns URL pointing to that view. It uses properties to
contruct the url with placeholders for view uuid and language:

    view.published.url = http://foo.bar/${uuid}?lang=${lang}

If 'view.published.url' is not defined uses a default by combining 'oskari.domain' and 'oskari.map.url'.

Added a common base class that can be extended for scheduled tasks 'fi.nls.oskari.worker.ScheduledJob'. Note that a
scheduler such as the one provided in module service-scheduler needs to be included for scheduling to actually happen.

fi.nls.oskari.domain.Role now has a static method to determine default role for logged in user as well as admin role.
The role names can be configured with properties and such match the role names in the database:

    oskari.user.role.admin = Admin
    oskari.user.role.loggedIn = User

Moved common annotation processing classes from service-control to service-base.

Added a new custom annotation @Oskari("key"). This can be used as a common way to mark classes extending OskariComponent.
To get a map of annotated classes (key is annotation value):

    Map<String, OskariComponent> allComponents = fi.nls.oskari.service.OskariComponentManager.getComponentsOfType(OskariComponent.class);

Service-search currently triggers the annotation processing. To use annotations without using service-search use a similar META-INF/services
setup that service-search includes.

IOHelper now has a method getConnectionFromProps("prefix") which gives a HttpURLConnection based on properties prefixed with given string:
    - [propertiesPrefix]url=[url to call for this service] (required)
    - [propertiesPrefix]user=[username for basic auth] (optional)
    - [propertiesPrefix]pass=[password for basic auth] (optional)
    - [propertiesPrefix]header.[header name]=[header value] (optional)

### service-search

Search channels can now be added to Oskari by extending fi.nls.oskari.search.channel.SearchChannel and annotating the implementing class
with @Oskari("searchChannelID"). Channels are detected with:

        final Map<String, SearchChannel> annotatedChannels = OskariComponentManager.getComponentsOfType(SearchChannel.class);

The legacy way of providing classname in properties is also supported but discouraged.

SearchableChannel.setProperty() has been deprecated and will be removed in future release. SearchChannels should use
PropertyUtil or other internal means to get configuration.

SearchChannel baseclass has getConnection() method which returns a HttpURLConnection based on properties prefixed with 'search.channel.[channel id].service.'.

### service-search-nls/servlet-map - search channels

Migrated search channels to use annotated approach and getConnection() from baseclass so credential handling is consistent.

### content-resources

New bundle registration: rpc. Enables postMessage communication with embedded map. Added to publish template.

### service-control

ActionControl now catches exceptions on ActionHandler.init() and teardown(). A single faulty ActionHandler no longer breaks the initialization.
The same errorhandling was added for ViewModifierManager.

Enabled customized HTML string cleaning.

Moved common annotation processing classes from service-control to service-base.

### control-base

GetViewsHandler now returns view URLs in response.

Added new action route for fetching CSW metadata. Requires a geonetwork base URL in properties under service.metadata.url.

Enabled customized HTML tags for GFI content.

Added service-csw as a new dependency, it has code that was previously part of service-map.

### servlet-transport

Excluded specific GML properties from parsed features.

## 1.24.5 - security patch

### service-control/RestActionHandler & control-admin

The preprocess-method call was commented out on Oskari versions 1.24.0-1.24.4. This is a serious security issue as
 many of the admin functionalities depend on preprocess() to check that the user is an admin. On affected versions even
 Guest-users can use the admin functionalities.

## 1.24.4

### content-resources/publish template

The 'publishedstatehandler' bundle was missing from publish template. As a result any published maps with history-tools
are broken. Updated publish template is provided in content-resources/src/main/resources/json/views/publisher-template-view.json
and an SQL to fix existing template is in content-resources/src/main/resources/sql/upgrade/1.24.4/add_publishedstatehandler_to_published_maps.sql.

Notice that these won't fix previously published maps with history-tools. To fix them you need to add 'publishedstatehandler' bundle to views of type 'PUBLISHED'. Also
make sure you have the 'publishedstatehandler' bundle included in minified JS.

## 1.24.3

### control-base/PublishHandler

Now checks if publish template view has 'publishedmyplaces2' included and removes it from the view if user isn't allowed to add draw tools for map.

## 1.24.2

### servlet-transport

Changed feature property values handling so empty value maps are treated as [null] and not as empty JSON object.

## 1.24.1

### servlet-transport

Changed feature property values handling so empty values are treated as [null] and not as empty JSON object. Also added some debug logging for property handling.

## 1.24

## lib and db changes

Geotools have been updated to version 11.2.

The UUID column in PORTTI_VIEW changes its type to UUID instead of varchar. It's still blank by default, but a view can be referenced by uuid in addition to id.
The upgrade SQL has a commented section to autogenerate UUID values.

Username/password fields have been added for OSKARI_MAPLAYER. If used the maptile requests are proxied through oskari-server to the actual service.

### geoserver-ext

Extensions for Geoserver have been updated to compile for Geoserver 2.5.2 and matching Geotools 11.2.
No code changes was required so extensions are still usable with Geoserver 2.4.2/Geotools 10.2 (by reverting pom.xmls to previous versions).

### control-base

GetWFSLayerConfiguration now does additional handling for layers created by users. As result those layers should now get feature data in published maps.

PublishHandler will now delete layer data from Redis for layers created by users when such layer is published.
This will result in transport getting the updated data about layer being published and ultimately end up serving feature data in published maps.

CreateUserLayer now prevents Guest-users to import data.

### service-control

Added convenience method to ActionParameters: requireLoggedInUser() and requireAdminUser() which will throw ActionDeniedException if user is guest/not an admin.

### service-base

Added new base-class for layers created by users: fi.nls.oskari.domain.map.UserDataLayer. Analysis, UserLayer and MyPlaceCategory extend this.

### service-map

Implemented GPX and MIF/MID dataset import with GeoTools OGR plugin. To be able to use it, the GDAL library file needs to be included in the environment variable PATH (Windows) or LD_LIBRARY_PATH (Linux).

The Windows .dll file and dependencies can be downloaded e.g. at http://www.gisinternals.com/sdk/. Actual .dll file needs to be renamed as gdal.dll or referenced in the environment variable GDAL_LIBRARY_NAME. The Linux version of GDAL including the needed .so file is probably available in the package repository of your distribution. It can also be compiled from the source code, see http://trac.osgeo.org/gdal/wiki/DownloadSource. The import functionality was tested with GDAL 1.11.0.

For further reference, see the OGR GeoTools plugin instructions at http://docs.geotools.org/stable/userguide/library/data/ogr.html.

## 1.23

### New admin bundles

Check that you have these registered in portti_bundle database-table or check the upgrade 1.23 README.md for details.
The bundles are now part of the default configuration for admin user:

* admin/admin: will be used as generic admin bundle in the future and at this time allows to manage default views location and layers
* framework/admin-users: for managing users

### Maven repository @ oskari.org

Precompiled versions of code is now available. Only 1.23-SNAPSHOT currently available, but versions start to pile up on releases.

Add these to your pom.xml to use the pre-compiled Maven-artifacts today:

    <dependencies>
        <dependency>
            <groupId>fi.nls.oskari</groupId>
            <artifactId>map-servlet</artifactId>
            <version>1.23-SNAPSHOT</version>
        </dependency>
    </dependencies>
    <repositories>
        <repository>
            <id>oskari_org</id>
            <name>Oskari.org repository</name>
            <url>http://oskari.org/nexus/content/repositories/releases/</url>
        </repository>
        <repository>
            <id>oskari_org_snapshot</id>
            <name>Oskari.org snapshot repository</name>
            <url>http://oskari.org/nexus/content/repositories/snapshots/</url>
        </repository>
    </repositories>

oskari_org_snapshot is only needed if using SNAPSHOT-versions (github develop branch version)

### control-admin

New action handler 'Cache' for admin users to check the status of caching.

New action handler 'SystemViews' can be asked to list configured default views (returns global and role-based) and update location/maplayers of given view.
Used by generic admin bundles default views functionality.

### content-resources

DBHandler can now insert layers described in JSON format (under src/main/resources/json/layers). The support is minimal at the moment but will improve.

DBHandler can now process "selectedLayers" property in view.json. The property should be an array with layer.json file references that will be inserted
to the database if (or matching layer in DB will be used if such exists with same url and name). The views mapfull-bundle will have these layers automatically
mapped in it's selected layers.

DBHandler has been split to some helper classes for inserting views and layers.

### service-users

IbatisRoleService now has findRoleByName(name) method

### webapp-map

Added external folder and moved jetty-jaas.xml, and jndi-login.conf files there.
Therefore these files need to be manually modified to match the system configuration. These files are unnecessary inside the war package and therefore removed.
Added jetty9-jaas profile to address jetty version related differences.
Added jetty9-ldap-jaas profile to address jetty version related differences.

### service-base

Added remove(name), getSize(), getKeys() methods to fi.nls.oskari.cache.Cache

### service-control / RestActionHandler

The basehandler for Rest-type requests now checks for header 'X-HTTP-Method-Override' in request and prefers it over request method when determining
which method to forward execution to.

### service-map

InspireThemeService now has findMaplayersByTheme(themeId) method

InspireThemeService and LayerGroupService now have findByName(name) method that is used to map themes/layergroups for layer
when automatically inserted. The implementation is not solid enough to be used otherwise.

OskariLayerService now has findByUrlAndName(url, name) method that returns layers matching given url and name

LayerJSONFormatter has initial (and minimal) implementation for parsing JSON to an OskariLayer object.

ViewService now has a method to update config/state/startup of single bundle in a given view.

### control-base

New action handler 'InspireThemes' implements a Rest type approach for managing inspire themes: insert/update/delete/listing.

## 1.22

### servlet-map/OskariRequestFilter

Should now setup language correctly when an existing cookie is found.

### control-base/service-map

GetWSCapabilities action route now supports WMTS-layers for parsing capabilities in a JSON structure similar to WMS-layers.

SaveLayer action route now support WMTS layers.

### service-base

IOHelper now has a constructUrl(baseUrl, paramsMap) method that can be used to create URLs safely with URL-encoded param values.

## 1.21

### control-base

GetWSCapabilitiesHandler now accepts type parameter and by default parses WMS capabilities as before, but with type 'wmtslayer' proxies the response XML to
client as is. Also 'wmsurl'-parameter has been changed to 'url'.

SaveLayerHandler now accepts WMTS-layers and has some changed parameters:
* wmsName is now layerName
* wmsUrl is now layerUrl

SaveLayerHandler propably will see some changes for WMTS-layer in near future.

### database

Changed capabilities cache table data size from 20000 characters to text to enable bigger capabilities documents.

Added keyword tables that are required by admin-layerselector when adding new layers.

Changed role mapping for users to be based on user id instead of username.

### control-admin

Added new module for administration

### control-admin/UsersHandler

New handler for listing, adding, editing and removing users

### service-base

ConversionHelper.getBoolean(null, true) now works correctly and returns the defaultValue instead of false with null parameter.

ConversionHelper, XmlHelper and JSONHelper have some additional helper methods.

### webapp-map/standalone-jetty/servlet-map

Many customizable operations have been moved out of the MapfullServlet code and into ServletFilters and ServletContextListener.

Now uses OskariContextInitializer to setup the environment for the servlet-map:
checks connection pools, populates properties and database connection on context initialization.
See the server log for initialization messages.

Now uses configurable OskariRequestFilter to setup the httpRequest for servlet:
* locale (based on http-param/cookie)
* login form url/fieldnames and logout url for logged in users
* userprincipal (should be disabled by setting property oskari.request.handlePrincipal=false in oskari-ext.properties
    if your servlet container handles user principal with JAAS (ldap or other authentication)

JAASAuthenticationFilter is now PrincipalAuthenticationFilter:
* handles login/logout functionality for users based on request.getUserPrincipal().
* adds users to oskari database based on request.getUserPrincipal() and request.isUserInRole()
* automatical user insertion can be disabled with property auth.add.missing.users=false
* external role names can be mapped to Oskari role names with new table oskari_role_external_mapping with role_id
    pointing to Oskari role and name having the value of the external role name

### standalone-jetty

Added request logging support. Tries to write them into logs directory and prints out a message if it doesn't exist.

Removed src/main/webapp (JSPs) from under standalone-jetty. Build now uses the JSPs from under webapp-map so there's no need
to copy/paste them on changes.

### service-feature-engine

New custom-parser option for transport to handle complex services. Example groovy-scripts for handling some services.

### transport (now servlet-transport and webapp-transport)

Split into servlet and webapp packages to be more in line with map-packages. The deployable war-file is now located webapp-transport/target.

No longer sets system property for geotools (org.geotools.referencing.forceXY) so it's safe to use with other webapps in the same JVM.

fi/nls/oskari/transport/config.properties has been renamed transport.properties and some of the property keys have been renamed to match the ones used in oskari.properties:

* serviceURL-> oskari.domain
* serviceURLParam -> oskari.ajax.url.prefix
* serviceURLSessionParam-> oskari.cookie.session
* oskari.cookie.route is newly configurable, defaults to ROUTEID
* serviceURLLiferayPath is now obsolete and any additional parameters for API url should now be added to oskari.ajax.url.prefix as on oskari.properties
* redisHostname -> redis.hostname
* redisPort -> redis.port

Transport now initializes by reading properties files in this order:
* oskari.properties
* transport.properties
* oskari-ext.properties
* transport-ext.properties

Moved JobQueue/Job from transport into service-base. Added teardown() hook for Job.

Added ResultProcessor interface for transport. WFSLayerJobs don't need reference to TransportService anymore,
but instance of ResultProcessor so they can be used elsewhere also. TransportService implements ResultProcessor by forwarding
the messages to cometd.

WFSLayerStore now extends WFSLayerConfiguration instead of copy-paste methods. Also cleaned wfs configuration a bit by removing
 obsolete fields like testlocation/testzoom etc.

Removed build profiles, custom resources for transport can now be given with maven property "transport.resourceDir" (via maven profile etc)

### servlet-transport (feature-engine)

WFS/feature-engine Fixed map click to return features to frontend.
WFS: 1st Attempt to use GeoTools forceXy for CRS only when drawing PNG result images.
WFS/feature-engine Finished feature engine groovy script configuration from database.
ELF: Included INSPIRE SLD resources to servlet-transport/src/main/resources.
ELF: Included a PoC groovy scripts for AU and GN reading to servlet-transport/src/main/resources.
ELF: Added database setup JSON and SQL scripts for 3 GN and 1 AU layer
ELF: SLD, groovy and db setup script placement may change to some app specific resources module in the future.

## 1.20

### service-users

A new module has been added for user management. Oskari now has database tables for users and roles and a new UserService
implementation utilizing them (DatabaseUserService). The DatabaseUserService is now configured as default in oskari.properties
 but can be overridden by oskari-ext.properties.

### servlet-map/webapp-map restructuring

Servlet for map application has been separated into servlet code and webapp with JSPs and webapp configuration (webapp-map).
Building the webapp-map is essentially the same as building servlet-map before this.

Servlet aggregate pom.xml (servlet-map-pom.xml) has been removed since the parent pom now builds the servlet, webapp and standalone
so you can use mvn clean install on the oskari-server root to build the modules.

### servlet-map/JAAS authentication/user login

The user login handling has been taken out of the servlet implementation and a reference JAAS configuration is now available in webapp-map.
The login form field names have changes to reflect this.

Servlet no longer handles login but expects a fi.nls.oskari.domain.User class object to be present in http session with key
'fi.nls.oskari.domain.User' if the user is logged in. The User object should be added to session in a servletfilter handling the
login as in servlet-map/fi.nls.oskari.map.servlet.JaasAuthenticationFilter.

Building the webapp-map with mvn clean install -Pjetty-jaas will create a war file that has JAAS enabled and login working out of the box.

### standalone-jetty

The separation of servlet-map to wepapp/servlet allows for new packaging "standalone-jetty" that uses preconfigured embedded
jetty to serve basic oskari-map functionality without the need to install any server software. Some default values for properties
containing URLs have been changed in oskari.properties to make the configuration easier and some new properties describing the
run environment has been added to configure the running environment (domain/path to map application).

### content-resources

The database used should now have postgis extension enabled to successfully run the whole default setup!
The default setup will now do a whole lot more (creates also myplaces, users and JAAS tables etc).
Lots of setup files have been merged and renamed since some have become irrelevant.
Many SQL-files have been renamed and some have been separated into smaller pieces.

Property keys in db.properties have been changed to use the same ones as utilized by other properties:

* datasource -> db.jndi.name
* url -> db.url
* user -> db.username
* pass -> db.password

Also oskari.properties and oskari-ext.properties now override db.properties so each component can be configured to use the same
credentials/urls with single properties file.

### transport

The `property_json` feature property of userlayers now gets parsed to json before sending to clients.

All feature requests now include the geometry property, even if configured in the database not to request map tiles.

## 1.19

### control-base/Myplaces2Handler

Now validates WFS-T from frontend a bit more thorougly. Also lets Guest users to insert features to a myplaces layer which is marked as "draw layer" ie. is published as public drawable layer.


### External libs

External libs are now handled as an in-project repository. The location of libs is defined in oskari-server/pom.xml as a repository.
The files can still be installed into local repository as before but it's not mandatory.

### WMTS layer support

No longer formats style/styles array with hard coded "default". Instead uses oskari_maplayer tables style to create the JSON values.

### Documentation

Docs has been removed from oskari-server repository and they are now available in http://www.oskari.org/documentation and https://github.com/nls-oskari/oskari.org/tree/master/md/documentation along with frontend documentation

### geoserver-ext/OskariMarkFactory (also affects transport WFS custom style)

Fixed resource leaking when loading font. Tmp-files were being created recklessly, now caches the font after loading it.

Also enabled the use of another font in classpath, previously font was hardcoded into dot-markers. Now the font specified in SLD is used with a fallback to dot-markers if specified font can't be loaded.

### service-search

SearchCriteria no longer has reference to MetadataCatalogueSearchCriteria. SearchCriteria.addParam() can be used to provide search channel additional criterias.

SearchResultItem now has addValue() that can be used to provide calling component additional search result values.

### service-base/Caching

CacheManager is now available and can be used to provide simple in-memory caches. This will most likely be developed further to allow configurable custom cache implementations that can be used to wrap functionality used by caching libraries (similar to UserService and Logger).

### servlet-map

Jetty-maven-plugin is no longer started automatically on install step. To start jetty on install you can use profile jetty-profile:

mvn clean install -Pjetty-profile

### content-resources/DBHandler

Setup-files can now refer to another setup-file. This removes much boilerplate for registering bundles and should make the files simpler.

Myplaces trigger has been updated to do initial update timestamp on insert as well (thanks posiki).

### Analysis functionality

CreateAnalysisLayer action route now returns a proper analysislayer json (same as GetAnalysisLayersHandler)

JSON for analysislayer is now created based on Analysis object with the help of AnalysisHelper. This will propably be refactored in the future to use the LayerJSONFormatter and the misleading AnalysisLayer class will propably be removed in favor of the Analysis class.

AnalysisDataService refactored a bit and to

### control-base/PublishHandler

Now handles publish permissions correctly (previously checked layer id for 'base_' prefix and used deprecated portti_layerclass db table).

Now allows selected roles to publish maps with drawtools. Roles allowed to publish draw tools is configured with property "actionhandler.Publish.drawToolsRoles".

### service-map/MyPlacesService

Now has a method for creating myplaces layer as wmslayer (used in published maps)

Now has a method for checking permissions based on category id/place id

### control-base/MapfullHandler

MapfullHandler now uses the MyPlacesService for creating json for myplaces layer.

Now handles layers with non-numeric ids correctly (same fix as with PublishHandler and 'base_' prefix on layer name)

## 1.18.1

### control-base/PublishHandler

Now uses template view as basis for existing maps as well. Merges plugin configs sent by client with existing bundle configs. Overrides user configs with template configs on merge so client can't change terms of use URLs etc. Note! Publish template plugins shouldn't have any configurations that can be overridden by user.

### service-map/GetGtWMSCapabilities

Geotools ScaleDenominatorMin is now set to OskariLayer as max scale and vice versa since this is how Oskari components expect to have these values.

## 1.18

### General

Most maven modules under oskari-server now share the maven parent defined in oskari-server/pom.xml. Properties are injected at
compile time and a custom build profile for tomcat has been added (mvn -f servlet-map-pom.xml install -P tomcat-profile).
See [docs/Customizing property values](docs/CustomizingPropertyValues.md) how to customize build for your own properties.

Updated GeoTools version 10.2. The version is now the same all over Oskari modules (previously 2.7.5 and 9.1).

Updated GeoTools can't parse empty Abstract-tags for WFSLayer SLDs. Script to update any existing SLDs in
database (portti_wfs_layer_style table) can be run with the command SCRIPT=remove-empty-abstract-from-SLD node app.js in content-resources/db-upgrade
(check the config.js first for database settings).

Removed some hardcodings:
* fi.nls.oskari.control.view.modifier.bundle.MapfullHandler

Previously hardcoded myplaces layer wmsurl: "/karttatiili/myplaces?myCat="
can now be configured with property 'myplaces.client.wmsurl'

* fi.nls.oskari.control.view.GetAppSetupHandler

Previously hardcoded prefix for secure urls (prefix to make easier proxy forwards) "/paikkatietoikkuna"
can now be configured with property 'actionhandler.GetAppSetup.secureAjaxUrlPrefix'

### Service-map/Layer JSON formatting

LayerJSONFormatterWMS now checks if the layer already has a legend image url configured (by admin) instead of always overwriting it based on capabilities.

### Service-OGC and control-wfs

Have been deprecated. The required parts have been moved to service-map and the currently recommended backend component for WFS-functionality is the transport-servlet.

The deprecated modules can still be found inside oskari-server/deprecated folder.

### Transport

Added override properties handling. Tries to search for file 'transport-ext.properties' in classpath and if found, overrides default values loaded from config.properties if

### GetAppSetup/ParamHandlers

It's now possible to add preprocessors for ParamHandlers used in GetAppSetup. Check [service-control/README.md](service-control/README.md) for more
info about preprocessing parameters.

## PublishHandler/SaveViewHandler/View and bundle handling

Refactored actionhandlers a bit for cleaner implementation for saving views.
Views and Bundles can now be copied with clone() method.
In the process also ViewService includes methods addView(View) and updatePublishedMap(View) and the old versions with second parameters have been deprecated.

## service-permission/UserService

Moved to service-base so it can be used by Role-class to load information about admin role.

The Platform-specific map parameter has been changed from Map<String, Object> to Map<Object, Object> and a convenience method has been added to main UserService to call getRoles without
parameters.

## service-base/Role

Now has a method getAdminRole() which returns a role reference. Replaces the getAdminRoleName() which has been deprecated.
Now uses UserService to load information about admin role, but still gets the admin role name from properties as before.

## 1.17.2

### GetFeatureInfoHandler

Styles will now longer be sent with value "null", but an empty string

### Transport

MapClick will now send an empty list as response when done so client knows that any data gotten for WMSlayers can be shown.

Default highlight style for lines now doesn't use "fill" and areas  50%

## 1.17.1

### ZoomParamHandler

Now uses parameter correctly again (not trying to get a property with param value).

## 1.17

### service-permission

Added getGuestUser() method for UserService. Implementations should override it and return a Guest user with a Guest role so permission mappings can be done correctly.

### service-map

Added ibatis caching for Inspire-themes, views, wfs-layers and backendstatus operations.

### servlet-map

Added oskariui style classes to index.jsp to fix layout.

Removed Guest user role hardcoding and now uses UserService.getGuestUser() to create a guest user.

### Massive maplayer refactoring

Maplayer DB structure and JSON formatting has been simplified so all layers are now located in oskari_maplayer database table and all JSON formatting should be done with
fi.mml.map.mapwindow.util.OskariLayerWorker instead of former MapLayerWorker. All layers should now be referenced with OskariLayer instead of (Map-)Layer classes and
they should be loaded using OskariLayerService instead of MapLayerService. Additional upgrade is required - [instructions can be found here](docs/upgrade/1.17.md).

### ParamHandler/ViewModifier/ModifierParams

ParamHandlers now have access to the ActionParams instance for the request. This means they can determine how to handle a parameter depending on other parameters.

## 1.16

### content-resources

Added upgrade SQLs that can be run to update database structure on version update. See [Upgrade documentation for details](docs/Upgrading.md)

Added a new commandline parameter "-Doskari.addview={file under resources/json/views}" to add a view without setup file

Added setupfiles:
* to initialize an empty base db with -Doskari.setup=create-empty-db
* to register bundles with -Doskari.setup=postgres-register-bundles

These can be used to initialize the database step-by-step. Also added an example setup for parcel application (-Doskari.setup=postgres-parcel).


### service-base / IOHelper

Added debugging methods to ignore SSL certificate errors. This can be used for existing code by adding properties (NOTE! This is global setting for all connections through IOHelper):

	oskari.trustAllCerts=true
	oskari.trustAllHosts=true

Individual connections can use these by calling IOHelper-methods:

    public static void trustAllCerts(final HttpURLConnection connection) throws IOException
    public static void trustAllHosts(final HttpURLConnection connection) throws IOException

### servlet-map

Now reads an oskari-ext.properties file from classpath to override default oskari.properties values.

### control-base GetSotkaData

Action handler has been refactored and indicators/regions listing now uses Redis for caching if available

## 1.15

### User domain object

Now has a convenience method to check if user has any of the roles in given String array (containing rolenames)

### GetWSCapabilities action route

Permitted roles for executing the action is now configurable with property 'actionhandler.GetWSCapabilitiesHandler.roles'
