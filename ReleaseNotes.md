# Release Notes

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

### service-logging

Moved Log4JLogger from under servlet-transport to a new service so it can be used with other modules as well.

### service-base

Changed Job from abstract class to an interface and added AbstractJob to be a drop-in replacement for Job.

JSONLocalized class now tries to get the value with default language if requested language is not available. This
 helps when a language is added to Oskari installation and all data producers, Inspire-themes and maplayers lack
 the localized name.

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

### service-feature-engine

Added new generic WFS path parser for complex featuretypes
Instruction under oskari.org\md\documentation\backend\configuring-wfs-path-parser.md
Sample complex wfs layer insert script in \elf-oskari-server-extensions\elf-resources\resources\sql\elf-nls_fi-lod0ad-Cascading-wfslayer.sql

### servlet-transport

Moved duplicated code from `FEMaplayerJob` and `WFSMaplayerJob` to common baseclass `OWSMaplayerJob`.
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

### webapp-transport

Added the Hystrix stream servlet for Hystrix Dashboard usage. HystrixMetricsStreamServlet can be removed from the web.xml
 to disable this.

Added StatusServlet to expose metrics as JSON. It can be removed from the web.xml to disable the functionality.


### servlet-map

Added functionality for additional response headers when serving jsp pages.

    oskari.page.header.X-UA-Compatible = IE=edge

Log4JLogger is now accessible to servlet-map (new dependency service-logging).

PrincipalAuthenticationFilter now always trims usernames before using them (removes leading and trailing whitespace).

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
