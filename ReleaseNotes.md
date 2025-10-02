# Release Notes

## 3.1.0

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/54?closed=1

### Changes

- Improved parsing for MIF/MID files for userlayer import
- Added created and last login timestamp for getting user info for the frontend
- Removal of code and migrations related to the analysis functionality. The functionality has been removed from oskari-server for now.
- Coordinate order change for GetWfsFeatureHandler (https://github.com/oskariorg/oskari-server/pull/1167)

### Updated libraries

- Spring framework 6.2.2 -> 6.2.9
- Spring security 6.4.2 -> 6.5.3
- Spring session 3.4.1 -> 3.5.2
- Postgresql JDBC 42.7.4 -> 42.7.7
- GeoTools 32.2 -> 33.2
- Jedis 5.2.0 -> 6.1.0
- org.json 20240303 -> 20250107
- commons-lang 3.17.0 -> 3.18.0
- commons-text 1.13.0 -> 1.14.0
- commons-codec 1.16.0 -> 1.19.0
- commons-fileupload 2.0.0-M2 -> 2.0.0-M4
- commons-io 2.18.0 -> 2.20.0
- commons-csv 1.13.0 -> 1.14.1
- jackson 2.18.2 -> 2.19.2
- tomcat 10.1.39 -> 10.1.46
- flywaydb 11.1.1 -> 11.12.0
- hikaricp 6.2.1 -> 7.0.2
- Quartz scheduler 2.3.2 -> 2.5.0
- JUnit 5.11.4 -> 5.13.4
- xmlgraphics-fop 2.10 -> 2.11
- poi-ooxml 5.4.0 -> 5.4.1
- jsoup 1.18.3 -> 1.21.2
- jackson 2.19.2 -> 2.20.0
- mockito 5.15.2 -> 5.19.0
- log4j 2.24.1 -> 2.25.1
- slf4j 2.0.11 -> 2.0.17
- dropwizard metrics 4.2.30 -> 4.2.36

## 3.0.2

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/55?closed=1

- Migrate featuredata and metadatasearch in database automatically for applications (sice featuredata2 and metadatacatalogue were removed in 3.0)
- DigiTransit API: signal an error if SRS-param is not sent with the request
- Fixed an error where setting visibility of a my places layer to public failed (publishing new my places layers as layers on embedded maps couldn't be done)
- Made errors for proxying layer legend image and CSW coverage data less noisy in the log
- Fixed an issue where clustered server environment messaging was not re-established after loosing Redis connection.

## 3.0.1

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/53?closed=1

- DigiTransit API: add headsign and stopindex to response
- Tomcat 10.1.34 -> 10.1.39 to match the download zip
- Disabled some xml-reader features for capabilities validation

## 3.0.0

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/52?closed=1

The minimum Java version for Oskari 3.0 has been upgraded from 8 to 17 and with that comes a long list of dependency upgrades and manual migration requirements for Oskari-based applications.

### Spring update 5.x -> 6.x

Requires migration on Oskari-based applications (see [Migrationguide.md]):
- packages have changed from javax.servlet to jakarta.servlet
- Spring security configs have been migrated for the new Spring version and you can now use `OskariSpringSecurityDsl` on your custom configs: https://github.com/oskariorg/oskari-server/pull/1124
- JSTL-library updated
- org.json update updated. JSONObject.getString() now throws an exception if the value is for example integer. Might require changes in applications to change getString() to optString().

### Routing adapter changed

Routing service was built with an implementation for digitransit API service. This service has released a version 2 implementation of the API with version 1 being shutdown shortly. The routing implementation has been changed to map the version 2 response to match the response that is accessible through Oskari RPC API.

### Dependency updates (see [Migrationguide.md])

- GeoTools 28.5 -> 32.2
- Spring 5.x -> 6.2.2
- JUnit 4.x -> replaced with org.junit.jupiter 5.x
- flywaydb: 9.22.3 -> 11.1.1
- hikaricp from 4.0.3 to 6.2.1
- h2database from 2.2.224 to 2.3.232
- PDFBox 2.0.32 to 3.0.3
- commons-lang3: 3.14.0 -> 3.17.0
- commons-text: 1.11.0 -> 1.13.0
- commons-io: 2.16.1 -> 2.18.0
- commons-csv: 1.10.0 -> 1.13.0
- poi-ooxml: 5.3.0 -> 5.4.0
- jsoup: 1.17.2 -> 1.18.3
- jackson: 2.18.1 -> 2.18.2
- mybatis: 3.5.16 -> 3.5.19
- metrics: 4.1.0 -> 4.2.30
- jedis: 3.8.0 -> 5.2.0

#### Changed dependencies

- Hystrix replaced with Resilience4j: https://github.com/oskariorg/oskari-server/pull/1135
- org.json implementation removed from service-base, replaced by version that is used by Jedis (https://github.com/oskariorg/oskari-server/pull/1127)
- axiom removed as Java now has decent built-in XML handling: https://github.com/oskariorg/oskari-server/pull/1109
- Jetty dependencies removed (just used for getting vulnerability notifications)
-> tomcat added as replacement to get vulnerability notifications
- vecmath
- PowerMock replaced by Mockito
- xom
- commons-dbcp2
- commons-collections

### Removal of old code

Generic WFS-T related code has been moved from `service-myplaces` to `service-wfs-client`. My places used WFS-T with GeoServer and for lack of better place at that time the generic code was placed under my places functionality.
Nothing changed on code level: https://github.com/oskariorg/oskari-server/pull/1102

Removed proxy-functionality that was designed to pass messages between Oskari-frontend and a GeoServer handling user generated content. Now that the GeoServer has been removed, these have been removed as well:
 - https://github.com/oskariorg/oskari-server/pull/1098
 - https://github.com/oskariorg/oskari-server/pull/1101

Analysis functionality used GeoServer as well for doing the analyses. That functionality has been deprecated before and now much of the code related to analysis layers has been removed as well:
- https://github.com/oskariorg/oskari-server/pull/1100

Once upon a time, Oskari was used to render vector features into raster images using SLDs. Functionality that used these have been removed some time ago so the SLDStore class was now removed:
- https://github.com/oskariorg/oskari-server/pull/1099

Removed old action routes where replacements are now used by the frontend maintained:
- https://github.com/oskariorg/oskari-server/pull/1105
- https://github.com/oskariorg/oskari-server/pull/1106

Removed the adapter module for reading an old Eurostat statistics API: `service-statistics-eurostat`. These were never used in production and looks like the API has changed over the years enough that it's probably easier to start fresh if we ever need reading them.

## 2.14.0

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/49?closed=1

### Support for WMS-T intervals added

Added parsing for WMS-T time dimension intervals. Layers with this kind of timeseries information should now work like ones that have explicit time dimension list. Note that these kinds of layers might have very short time period and might require updating very frequently. An example could be a weather forecast. The time dimension is parsed as part of the capabilities and stored on the database soon these kinds of layers you should make the layer _automatically update capabilities on the layer admin UI_ AND look into running UpdateCapabilitiesJob more frequently than the default (once per day). If you have these types of layers, here's a cronline for oskari-ext.properties to run the capabilities updating scheduled job every 10 minutes:

```
oskari.scheduler.job.UpdateCapabilitiesJob.cronLine=*/10 * * * * ?
```

Note! These two settings work separately but together to achieve updating capabilities automatically. The scheduled job runs at certain pace that has been set to check any layers where the difference between current time and the timestamp for last capabilities update is more than the value set for that layers capabilities update interval. In that case the capabilities are updated on the database and the timestamp is updated. Any value on the layer capabilities update that is smaller than the frequency of the scheduled job won't have a meaningful result as the capabilities update frequency is ultimately decided by when the scheduled job is run.

### Statistical data processing improvements

- Statistical data indicator list parsing for SotkaNet now skips faulty indicators instead of stopping processing entirely when encountering one.
- Statistical regionsets are now processed by removing any duplicated regions: https://github.com/oskariorg/oskari-server/pull/1084
- Statistical regionsets from resource files now support mixed geometries: https://github.com/oskariorg/oskari-server/pull/1085

### Performance and maintenance improvements

- Refactored layer permission query on map startup. Instances with lots of layers may see significant boost on page loading time (GetAppSetup might go from seconds to milliseconds: https://github.com/oskariorg/oskari-server/pull/1067).
- Added a hook that allows applications to use custom code for running migrations like using a different user to run them etc: https://github.com/oskariorg/oskari-server/pull/1061
- Reduced noise in the logs by removing proxying errors from audit-logging.

### Updated libraries

- Jetty (in the download package zip) 9.4.51.v2023021 -> 9.4.56.v20240826
- Postgresql JDBC 42.7.2 -> 42.7.4
- Spring framework: 5.3.35 -> 5.3.39
- Spring security: 5.7.12 -> 5.7.13
- mybatis 3.5.15 -> 3.5.16
- log4j 2.22.1 -> 2.24.1
- Geotools 28.4 -> 28.5
- org.apache.xmlgraphics/fop 2.9 -> 2.10
- org.apache.poi/poi-ooxml 5.2.5 -> 5.3.0
- commons-io 2.15.0 -> 2.16.1
- Jackson 2.16.1 -> 2.18.1
- pdfbox 2.0.30 -> 2.0.32
- h2database 2.2.220 -> 2.2.224

Notes:
- GeoTools 29.x requires Java 17+
- Flyway 10.x requires Java 17+
- HikariCP 5.x requires Java 11+
- h2 v2.3.230+ requires Java 11+ 

## 2.13.1

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/50?closed=1

- Improved Redis credentials handling: https://github.com/oskariorg/oskari-server/pull/1065
- Fixed an issue with csw-metadata fetching
- Added a flag for server to follow redirects automatically
- Updated libraries:
    - Spring framework: 5.3.32 -> 5.3.35
    - Spring security: 5.7.11 -> 5.7.12
    - commons-compress: 1.25.0 -> 1.26.1

## 2.13.0

For a full list of changes see:
https://github.com/oskariorg/oskari-server/milestone/48?closed=1

### Bundled GeoServer removed

- `myplaces` and `userlayer` (= user-generated) content is now managed directly through the database connection without a need for a GeoServer to be run with oskari-server.
- `analysis` functionality still uses GeoServer to make the analysis, but most instances don't use the analysis functionality so the bundled in GeoServer has been dropped from the download package.
- Removed code related to initializing the bundled in GeoServer like the `setup.war` webapp and `geoserver-rest-client`.
- See [Migration guide](MigrationGuide.md) for details about removing the GeoServer from an existing Oskari instance.

### New functionality

- Printouts can now include coordinates.
- Added support for reverse geocoding for the OpenStreetMap search channel.
- URL-parameter `swipe=true` can now be used to active layerswipe functionality on startup (on applications using layerswipe).

### Statistical data sources

- Statistical data adapter for SotkaNet now uses `absValue` for the indicator data from the API if any result doesn't have a `value` so user can get some data on screen instead of an error.
- Improve region set matching for SotkaNet adapter for detecting data for `hyvinvointialue/wellbeing services counties`.
- Made cache keys for statistical data consistent so it's easy to flush cached data from redis by using key-prefix `oskari:stats:[data source id]*`.
- Removed the unused `service-statistics-kapa` module from statistics-data plugins. It was developed as PoC for allowing multiple data sources to be read using adapters but there is no known data source that would use this (developed against mock service) and we have actual data sources with `PXWeb` and `SotkaNet` that are used daily and demonstrate this.

### New bundles

Drop-in React-based replacements for jQuery implementations:
- `metadatasearch` -> `metadatacatalogue`
- `featuredata2` -> `featuredata`

Both are allowed to be part of embedded maps by the `publisher` functionality and we also added `layerswipe` and `announcements` bundle to the default allowed list for publishing.

### Improved error handling

- Users now have created and last login timestamps on the db. Previously the user attributes was used to store these with instance specific code, but to make querying easier its best to have them in columns in the database. Custom login implementations should be updated to use these database columns as we now show the last login for the end user. For more details: https://github.com/oskariorg/oskari-server/pull/1000/files
- Improved identifier headers that are sent as part of requests made by oskari-server. Previously the `User-Agent` header could result as `Oskari/null`. Now defaults to `Oskari/0.0` if we are unable to determine the version.
- Fixed srid value in postgis for userlayer features (https://github.com/oskariorg/oskari-server/pull/1005).
- Improved error handling while parsing layer capabilities to prevent an issue where the server might not start properly when running migrations that add layers from services that are unavailable during the migration (https://github.com/oskariorg/sample-server-extension/pull/55).
- When querying for GFI, the server now checks if the user has permission to see the layer. Previously the GFI request was passed to the server without a check so knowing a layer id and a coordinate where features on that layer would be could possibly be used to handcraft a request that by-passes authorization for that layer.

### Library updates

Note! Apps will need to update these dependencies as well (See [Migration guide](MigrationGuide.md)).

- Now uses `HikariCP` as built-in connection pool instead of `BasicDataSource` from `commons-dbcp2`
- Spring 5.3.28 -> 5.3.32
- Spring security 5.7.9 -> 5.7.11
- Spring session bom 2021.2.2 -> 2021.2.3
- org.apache.commons.commons-lang3 3.13.0 -> 3.14.0
- org.apache.commons.commons-text 1.10.0 -> 1.11.0
- org.apache.commons.commons-dbcp2 2.9.0 -> 2.11.0
- org.apache.pdfbox.pdfbox 2.0.24 -> 2.0.30
- org.apache.xmlgraphics.fop 2.3 -> 2.9
- org.apache.poi.poi-ooxml 4.1.2 -> 5.2.5
- commons-csv 1.8 -> 1.10.0
- commons-codec 1.15 -> 1.16.0
- jsoup 1.16.1 -> 1.17.2
- jackson 2.15.2 -> 2.16.1
- mybatis 3.5.13 -> 3.5.15
- flywaydb 9.12.0 -> 9.22.3
- Postgres jdbc 42.6.0 -> 42.7.2
- SLF4J 1.7.36 -> 2.0.11
- Log4J2 2.20.0 -> 2.22.1

## 2.12.0

For a full list of changes see: 
https://github.com/oskariorg/oskari-server/milestone/47?closed=1

### User management

- Added param for `Users` route to get users by role.
- Added paging for user queries to make user management usable on instances with a lot of users
- `GetAllRoles` route has been deprecated since a more recent `ManageRoles` route basically does a better job for the same thing.
- Frontend now receives a simple boolean flag if user is admin or not. Role name for admins can change between instances and now frontend has easy way of detecting admins.

### Layer permissions

- Added new route for getting and changing layer permissions `LayerPermission`.
- Replaces `GetPermissionsLayerHandlers` that is now deprecated and will be removed in future release.
- Replaces `SaveLayerPermission` that is now deprecated and will be removed in future release.
- Output format/more details: https://github.com/oskariorg/oskari-server/pull/973

### GetAppSetup route

- The instance default vector style is now sent as part of the `GetAppSetup`` response.
- Markers are now sent as templates with placeholders instead of hardcoded colors. This removes duplicated processing of markers in server / frontend.
- Now accepts url-parameter `mobile` with boolean value. The server can be configured to add or remove bundles depending on this value to make mobile experience more user-friendly by removing functionality that doens't work well on mobile/small screens. Configuration described in: https://github.com/oskariorg/oskari-server/pull/995

### DescribeLayer route

- Added metadata about attributes available on WFS layers.
- Added configuration of attributes like renaming, hiding and reordering properties and for formatting attribute values.
- Added a simple type for layer geometry that allows optimizing frontend based on the geometry type (point/line/polygon).

### Other changes

- Fixed an axis order issue on VectorFeatureWriter: https://github.com/oskariorg/oskari-server/pull/976
- Reduced unnecessary logging when a vector service doesn't respond with JSON/GML.
- Allowed WMTS tile matrices to be passed to frontend even if there is no exact match for CRS. This allows OpenLayers to reproject the layer with proper configs: https://github.com/oskariorg/oskari-server/pull/991
- Added order attribute for custom annotated components to control the initialization order: https://github.com/oskariorg/oskari-server/pull/992. Can be usedto fix issues where one component needs to be initialized before another.
- Added new bundle registrations:
    - `featuredata` (React.js replacement for current jQuery impl `featuredata2`)
    - `admin-permissions`  (React.js replacement for current jQuery impl `admin-layerrights`)

### Updated dependencies

- Jetty 9.4.48.v20220622 -> 9.4.51.v20230217
- GeoTools 28.2 -> 28.4
- Spring 5.3.27 -> 5.3.28
- Spring security 5.7.8 -> 5.7.9
- Spring session 2021.2.0 -> 2021.2.2
- Jackson 2.13.4 -> 2.15.2
- JSoup 1.15.3 -> 1.16.1
- Log4J 2.17.1 -> 2.20.0
- SLFJ2 1.7.32 -> 1.7.36
- commons-lang 3.12.0 -> 3.13.0
- MVT 1.3.22 -> 1.3.23
- h2 2.1.210 -> 2.2.220
- GeoServer extensions updated 2.19 -> 2.22.4 

## 2.11.0

For a full list of changes see: 
https://github.com/oskariorg/oskari-server/milestone/45?closed=1

### Vector layer styles

- End-users can now store styles for vector layers!
- The styles users create are public/anyone can reference them. This makes them work with embedded maps in a way that users expect them to work. This doesn't mean that users styles are listed to other users in the geoportal UI.
- New table for storing styles: `oskari_maplayer_style`
- Styles have been migrated from `oskari_maplayer`.`options`
- Instance default style is also stored in the table: https://github.com/oskariorg/oskari-server/pull/932
- Styles have been removed from layer options from the layer listing functionality. Frontend is expected to fetch them by calling DescribeLayer route.
- Printing functionality has been migrated to use styles from the database

### User/role management

- User listing is now fetched in pages so instances with lots of users can use this without crashing the browser
- Users can now be searched as well as listed
- Role names can now be edited
- Admin user management now uses the same rules to validate users as end-user registration
- Additional metadata about roles is sent for admin user interface to make admins life easier (can be used to hide guest role/disable edit and delete for built-in roles etc)
- Instance admins can now configure oskari-ext.properties to disable editing user data. This is convenient if the user data in Oskari is updated using some external system. User roles can still be assigned for users (https://github.com/oskariorg/oskari-server/pull/944):
```
oskari.user.external=true
```

### Search channel options

Search channels with code based adapters (non-WFS search channels) can now be configured with a localized name and desciption on oskari-ext.properties: https://github.com/oskariorg/oskari-server/pull/953 Localization is not required/these can be configured with or without specific language:
```
search.channel.OPENSTREETMAP_CHANNEL.label=OpenStreetMap
search.channel.OPENSTREETMAP_CHANNEL.desc.fi=Hae paikkoja ja/tai osoitteita
search.channel.OPENSTREETMAP_CHANNEL.desc.en=Search addresses and/or points of interest
```

### Other changes
- DescribeLayer route has been improved so it can function as a source for frontend to get additional metadata for layers that are added on the map. This enables us to remove unnecessary data from the layer listing response. This allows us to remove some of the layer specific routes that have been previously created and now returns:
    - coverage area for the layer data
    - styles for vector layers
    - vector layer properties
    - tile matrix information for wmts layers

- Fixed an issue with email validation. The original change was meant to add support for domains with dashes, but this prevented users from registering if their email did NOT have a dash in the domain.
- Fixed an issue with proxying WMTS-layers through GetLayerTile
- Fixed an issue where searching for metadata with filters but not giving a free text query resulted in an error
- Fixed an issue in capabilities parsing where the service declared scale limit of 0
- Capabilities parsing now allows adding layers where the capabilities header and XML content declares conflicting character encoding
- Missing page (HTTP 404) is now handled in consistent way (and the page can be overridden in instances)

- Dependency updates:
    - Geotools 27.1 -> 28.2
    - JTS 1.18.2 -> 1.19.0
    - FlywayDB 6.5.7 -> 9.12.0 (Note! PostgreSQL 11 is now min supported version)
    - Spring 5.3.20 -> 5.3.27
    - Spring security 5.7.0 -> 5.7.8
    

## 2.10.1

For a full list of changes see: 
https://github.com/oskariorg/oskari-server/milestone/46?closed=1

- An issue with caching in clustered environment was fixed.
- Updated commons-fileupload dependency 1.4 -> 1.5
- Updated java-vector-tile 1.3.16 -> 1.3.22

## 2.10.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/44?closed=1

### Service customization

Appsetups can now have a `theme JSON` in the database table `oskari_maplayer.metadata` under the key `theme`. This enables embedded maps with user defined theme and it can also be used to customize the geoportal views. On the same note, the map tools no longer have "toolStyle" or "font" config and the previous selections made in publisher for embedded maps have been migrated to theme JSON in the database.

The theme can now be provided as part of an initial appsetup JSON when initializing an Oskari-based application programmatically: https://github.com/oskariorg/oskari-server/pull/909 
(See sample-server-extension https://github.com/oskariorg/sample-server-extension/blob/7f499fc51147be981108ef2536788c5cc811417c/app-resources/src/main/resources/json/apps/geoportal-3067.json#L12-L50).

See migration notes for more information about theming changes.

A centralized helper class `org.oskari.util.Customization` was added for getting markers and service logo and make customization easier.
The logo can be customized for Oskari instance using `oskari-ext.properties` and we could enable overriding the markers in a similar way in the future.
Previously markers were duplicated on the server code for frontend and printouts and service logo was also usually duplicated in frontend code as well.
Added a new action route `Logo` that is used by frontend to get the service logo from server so customizing it doesn't require frontend modifications and CSS overrides.
See details: https://github.com/oskariorg/oskari-server/pull/901

Added initial implementation for frontend code versioning. The main audience for this are maintainers of RPC-based applications, but it can be used on geoportal as well.
This allows the maintainer of an Oskari instance to provide parallel frontend code support on an Oskari instance that can be toggled with url-parameter.
This could be used to provide more flexible update schedule for RPC-based applications.
See: https://github.com/oskariorg/oskari-docs/issues/32 & https://github.com/oskariorg/oskari-server/pull/897

The domain (/cdn where static code is hosted) for client code can be configured using `oskari-ext.properties`. 
This enables running the same server-side code in environments using frontend code from a CDN/another domain and for example local dev environment:
https://github.com/oskariorg/sample-server-extension/pull/33

The link back to geoportal on the embedded map logo can now be disabled by configuring `plugin.logo.geoportalLink=false` in `oskari-ext.properties`.

### Metadata search improvements

The search is now requesting the result (`ElementSetName`) as `summary` instead of `full` so it contains less data to parse (and for the CSW-service to respond with).
Also the query fields can now be configured to make the queries even lighter for the CSW-service (`csw:anyText` seems to be very heavy if there is a lot of data on the service):
https://github.com/oskariorg/oskari-server/pull/912
```
# Valid values: summary, brief, full (defaults to "summary")
search.channel.METADATA_CATALOGUE_CHANNEL.queryType=summary
# comma-separated list - defaults to csw:anyText
search.channel.METADATA_CATALOGUE_CHANNEL.queryFields=Title, Abstract
```
The `brief` setting is even lighter, but by using it the search results won't include the date or organization on them as it's not included in that data set.
Setting the query fields seems to be more effective way of getting more performance if it is an issue for the search.

The search configuration has been streamlined with just `service.metadata.url` in `oskari-ext.properties` used to configure the CSW endpoint.
Many of the duplicated properties can be cleaned up/removed from `oskari-ext.properties` as listed in https://github.com/oskariorg/oskari-server/pull/910

The search result parsing code has been rewritten to reduce dependencies and improve maintainability.
Sanity checks have been added for data as some services might have invalid data for example on the bounding boxes:
https://github.com/oskariorg/oskari-server/pull/917

### Cloud compatibility improvements

The database connection/pool is no longer using JNDI by default. Connection pool can still be passed through JNDI, but the code doesn't try creating a context if it's not available.
Previously Oskari tried adding the database JNDI config to the context if it was not there to begin with. This led to problems with for example Tomcat environments where the JNDI-context is read-only.
This forced using JNDI configuration on environments using Tomcat. Using JNDI in context required the JDBC driver to be on Tomcat classpath and the driver could not be bundled inside the webapp war-file
which makes cloud deployment in for example Azure App Service more painful than it needs to be.

Now the JDBC driver can be bundled in the war-file and the database configuration passed in `oskari-ext.properties` can be used to connect to the database even on Tomcat:
https://github.com/oskariorg/oskari-server/pull/916

New Redis integration configuration options have been added for `oskari-ext.properties`:
```
# Can be used to set password if connection requires one
redis.password={your password or remove config to leave empty}
# When using SSL-connections (defaults to false like previously)
redis.ssl=true
# Timeout for connection (2000ms is the default timeout in Jedis)
redis.timeout.connect=2000
```

Redis-config `redis.pool.size` is now passed to connection pool `max-total` as expected. Previously it was only used to calculate and set the `max-idle`.

Caching in clustered environment has been improved by implementing the "flush all" command for cluster cache communication: https://github.com/oskariorg/oskari-server/pull/902. 
This fixes an issue where updates to map layers were not always reflected on all the nodes of a cluster until reboot.

### Reduced noise on logs

OskariComponent subclasses can now use `org.oskari.component.ComponentSkippedRuntimeException` (in `service-base` module) on the init method instead of using some other exception to signal that they are not configured properly to work/need to be skipped based on the instance config. This is used in `What3WordsSearchChannel` (in `service-search-opendata` module) as most instances don't use the channel (or don't have an apikey for it) it's unnecessary to log the stack trace for the channel just so we can disable it based on missing configuration. Now the startup of oskari-server has less misleading stack traces that the admin doesn't need to care about. See https://github.com/oskariorg/oskari-server/pull/888 for details.

Also moved some common/spammy informational logging from log level `info` to `debug`.

### Other improvements

- Service logo configured for printouts can now be non-square. The scalebar in printout makes space for logo when required.
- Fixed an issue with map legends for proxied map layers without legends
- Fixed an issue with WTMS coordinate order: https://github.com/oskariorg/oskari-server/pull/920
- Allow hyphen/dash in email domain name https://github.com/oskariorg/oskari-server/pull/924
- Automatically disable the "no status available" tooltip for layers when the statuses are not available at all on an instance:
https://github.com/oskariorg/oskari-server/pull/922

- Allow list of bundles that can be part of an embedded map is now fully on server-side code. This makes it easier to enable custom bundles as part of publisher options:
https://github.com/oskariorg/oskari-server/pull/921

- Library updates:
    - Spring 5.3.18 -> 5.3.20
    - Spring security 5.6.2 -> 5.7.0
    - Spring session BOM 2021.1.2 -> 2021.2.0
    - commons-lang3 3.11 -> 3.12.0
    - commons-text 1.9 -> 1.10.0
    - commons-code 1.7 -> 1.15
    - commons-dbcp2 2.8.0 -> 2.9.0
    - PostgreSQL JDBC 42.5.0 -> 42.5.1
    - MyBatis 3.5.7 -> 3.5.11
    - Jackson 2.13.2 -> 2.13.4
    - Jackson databind 2.13.4 -> 2.13.4.2 

- Removed unused dependencies from managed dependencies (code using these have been removed/rewritten):
    - org.apache.xmlbeans.xmlbeans
    - org.codehaus.woodstox.stax2-api
    - com.fasterxml.woodstox.woodstox-core

## 2.9.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/42?closed=1

- Allow users to include metadata flyout in embedded maps.
- Allow admins to override capabilities based max features limit for vector sources with layer attributes.
- Handle missing legend url for proxied layer to reduce noise in server logs.
- Enable region mapping for search results in WFS search channels: https://github.com/oskariorg/oskari-server/pull/883
- Handle timeseries layer with single time value and no resolution part as normal layer.
- Pass user generated statistical indicators for frontend even when they don't have data (since user can add data for them later).
- Library updates:
  - GeoTools: 25.1 => 27.1
  - JTS: 1.18.1 => 1.18.2
  - MVT (no.ecc.vectortile): 1.3.15 => 1.3.16
  - JSoup 1.14.2 -> 1.15.3
  - postgres JDBC 42.3.3 -> 42.5.0
  - Jetty 9.4.44.v20210927 -> 9.4.48.v20220622

## 2.8.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/43?closed=1

Fixed an issue with WMTS-layers that define limits on capabilities

## 2.8.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/40?closed=1

### My data frontend implementation changed

A new bundle `mydata` has been added as (in most cases) drop-in replacement for `personaldata`. See [MigrationGuide](https://github.com/oskariorg/oskari-server/blob/master/MigrationGuide.md#280) for details.

### Map layer capabilities rewrite

All capabilities parsing have now been fully migrated to the rewritten implementation for all layer types that previously used layer capabilities parsing. The `oskari_capabilities_cache` database table has been dropped and all capabilities data is now stored per layer on `oskari_maplayer` table capabilities column as JSON. The `service-capabilities` Maven module now holds the logic for parsing capabilities and all functionalities use it when querying a service for its capabilities data.

`org.oskari.capabilities.CapabilitiesService` has methods to query a service for layers including their capabilities per layer, update a single layer capabilities from the service, serialize the capabilities to JSON for `oskari_maplayer` and deserialize it from `oskari_maplayer` to get for example WMTS-layer matrix sets for printing.

WFS/OGC API Features layers now also have the properties described in DescribeFeatureType included in the capabilities JSON.

### Layer listing changes

Data providers are now included in the layer list response similar to layer groups. Most if not all localizable values are now only returned in the current users language. All current admin tools can query a single editable item separately to get the localized values so this simplifies the code and makes admins use the same code for normal layer operations as other users.

### Date handling

Initial steps have been taken to make date handling more consistent on the database and server-side. For now the decisions for these have been documented in [oskari.org FAQ](https://oskari.org/documentation/faq) under "Handling dates and timestamps". In short this means using timestamps with time zone in database, java.time package in server code and ISO strings in JSON.

Timestamp columns in database and server-side code have been updated for these for app setups and user generated content.

### Metadata service integration

When parsing capabilities for layers it is assumed that the metadata url/id referenced in capabilities is a link to the same service that the Oskari instance uses as metadata service (like GeoNetwork or other CSW-compliant service). This is not always the case and we added a new config for `oskari-ext.properties`. You can configure a comma-separated list that you want to enable metadata ids to be used from capabilities:

```
service.metadata.domains=paikkatietohakemisto.fi, geonetwork.nls.fi
```

If the url referenced in capabilities doesn't match the domain the metadata id parsing is skipped for that layer. If not configured this functions the same as before. If the list is configure, the value of `service.metadata.url` property is automatically added to the list.

### Hiding mydata views/account info tab

These can be configured in `oskari-ext.properties` with:
```
mydata.tabs.showUser=false
mydata.tabs.showViews=false
```

### Other changes

- Removed deprecated routes: `DeleteFeature`, `InsertFeature`, `SaveFeature` (replaced by `VectorFeatureWriter`)
- Printing vector features with fill pattern is now visually closer to what is shown on the browser

## 2.7.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/41?closed=1

Bumped dependencies:
- Spring 5.3.14 -> 5.3.18
- Spring Security 5.6.1 -> 5.6.2
- Spring Session 2021.1.1 -> 2021.1.2
- Jackson 2.12.1 -> 2.13.2 (databind to 2.13.2.2)

## 2.7.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/39?closed=1

Capabilities parsing rewrite:
- new module: service-capabilities
- currently WMS and WMTS-layers have been migrated to the new parser/format
- WFS/OGC API will be migrated in future/probably next release
- admin functionality, programmatic initialization of layers and scheduled update for layers have been migrated to use the new service 
- More details: https://github.com/oskariorg/oskari-server/pull/796

Other fixes and improvements:
- OGC API Features client now sends "Accept" header in requests to fix issues with services that respond with HTML if not requesting json
- Added initial filtering capabilities for OGC API Features client: https://github.com/oskariorg/oskari-server/pull/816
- TIFFReader improved in service-wcs: https://github.com/oskariorg/oskari-server/pull/813 
- SaveLayer action route removed. LayerAdmin is used by the current admin functionalities.
- Enabled userlayer layers to use localized value for name, description and source
- Enabled myplaces layers to use localized value for name
- Added layer analytics backend code for tracking problematic layers: https://github.com/oskariorg/oskari-server/pull/817
- Improved MID-file parser to handle empty strings
- Default vector feature stroke width for printing is now consistent with frontend map
- Statistical data plugin for PXWeb has been refined regarding external metadata. See configuration doc at https://oskari.org/documentation/features/thematicmaps/config
- Fixed an issue where saving statistical indicator while using UI language other than the instance default resulted in an error.
- Download basket functionality now supports credentials for SMTP configuration: https://github.com/oskariorg/oskari-server/pull/814
- Bumped h2 version 2.0.206 -> 2.1.210
- PostgreSQL 42.2.16 -> 42.3.3

## 2.6.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/35?closed=1

- Configuration can now be passed with ENV variables to override values in `oskari-ext.properties`: https://github.com/oskariorg/oskari-docs/issues/262
- Removed most restrictions for search queries. Programmatic usage can now request more results than the default limit. Introduced a config to define a hard limit (defaults to 10x default limit https://github.com/oskariorg/oskari-server/pull/765).
- Allowed additional parameters to be passed from frontend to search channels through the common search API. Usage of the additional parameters are documented on the RPC as there's no user interface for these but allows more options for programmatically initiated searches.
- Changed MVT encoder implementation (for WFS-features as MVT) to fix JTS version mismatch with the library and enable polygon/linestrings features to be encoded as MVT once again.
- Statistical indicator metadata improvements:
  - Generic API to allow additional metadata to be passed to frontend for indicators
  - PXWeb specific implementation to allow a JSON-file to be used to provide metadata like localized names and descriptions, start/end years and information about last/next update for data: https://oskari.org/documentation/features/thematicmaps/config
- Action routes can now be called with a cleaner URL. This is now enabled by server but the frontend still uses the longer URL. More details: https://github.com/oskariorg/oskari-server/pull/767
- Updated libraries:
  - Jetty 9.4.42.v20210604 -> 9.4.44.v20210927
  - Log4J 2.16.0 -> 2.17.1
  - Spring 5.3.11 -> 5.3.14
  - Spring Security 5.5.3 -> 5.6.1
  - Spring Session 2021.0.3 -> 2021.1.1
  - Jedis 3.6.3 -> 3.8.0
  - com.h2database.h2 1.4.199 -> 2.0.206

## 2.5.3

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/38?closed=1

- Upgrade Log4J 2.15.0 -> 2.16.0
- Upgrade slf4j-api 1.7.30 -> 1.7.32
- Fixed an issue that on rare cases prevented layers from being removed from the system due to misconfigured timeseries layer  

## 2.5.2

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/37?closed=1

- Upgrade Log4J 2.13.3 -> 2.15.0

## 2.5.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/36?closed=1

- Add error handling when parsing GetFeatureInfo from WMS capabilities
- Upgrade MyBatis 3.5.6 -> 3.5.7

## 2.5.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/34?closed=1

- Layerlisting response from server has been streamlined and some of the layer 
   metadata has been removed like coverage geometry. The frontend handles this
   internally by fetching the data when needed instead of having it all in the listing response.
   This reduces the size of the listing response dramatically and improves performance.
- New action route DescribeLayer that is currently just used for returning coverage geometry
   for layer when available. There are plans for doing more with this endpoint.
   Possibly combining it responses from GetWFSLayerFields and GetLayerCapabilities to a single generic endpoint.
- WTMS capabilities are parsed and now stored in JSON format for layers. 
   This enables giving frontend only the tile matrix information it needs for 
   showing the layer for the current projection that is used on the browser.
   This reduces network traffic and can vastly improve performance for WMTS-services with large capabilities documents.
   Note! This requires all WMTS-layer capabilities to be refreshed from the server and this is done automatically with an upgrade script as a Flyway-migration.
- The common search functionality no longer overrides a zoom scale hint with generic config IF it has been set by a channel.
- Default style options for printing vector features have been synced with defaults used on the frontend code.
- GetWFSLayerFields route now recognizes WFS attributes of types
   MultiLineStringPropertyType and MultiSurfacePropertyType as geometries
   instead of "unknown" type.
- New action route VectorFeatureWriter has been added as a replacement for combining 
   existing routes: InsertFeature, SaveFeature and DeleteFeature.
   The previous ones have been deprecated for removal at a later version.
   The new endpoint takes GeoJSON as request payload for the feature to be inserted/updated.
   This route is used by the content-editor bundle under oskari-frontend-contrib repository.
- Libraries updated:
  - Jetty 9.4.39.v20210325 -> 9.4.42.v20210604
  - jsoup 1.13.1 -> 1.14.2
  - pdfbox 2.0.16 -> 2.0.24

## 2.4.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/30?closed=1

- Cache layer listing results to improve performance as most users get the same result
- Remove unused parts from layer listing response to improve performance
- Added dataprovider identifiers for layers in listing response
- Reduce unnecessary logging to remove clutter from logs
- Improve MIF/MID parsing for imports
- Add region filter to queries for PXWeb API 
- Add support for HDEL and HINCRBY commands on Redis integration 
- Upgrade libraries:
    - GeoTools 24.2 -> 25.1
    - JTS 1.17.1 -> 1.18.1

## 2.3.3

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/33?closed=1

Fixed an issue with object data/feature properties which were not shown correctly when there were multiple userlayer/analysis map layers on the map at the same time.

## 2.3.2

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/32?closed=1

- Small changes for userlayer/analysis migrations to make them compatible with Postgres 9.4 (previously required 9.6)

## 2.3.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/31?closed=1

- Small change to my places migration to make it compatible with Postgres 9.4 (previously required 9.6)
- Make WFSclient accept features without geometries.
- Enable additional parameters from oskari_maplayer.params to be passed to OGC API Features services.

## 2.3.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/29?closed=1

- Added support for GeoPackages for userlayer import (the UI doesn't say so but importing these should work).
- When layer is loaded for editing the capabilities are now refreshed before the data is written out for frontend.
- Metadata UUID is now included in the layer capabilities response
- Added support for a new control parameter "timeseries" to pass URL-parameter as part of frontend state.
- GetWFSLayerFields now returns "filter" and "geometryType" as part of it's response.
- Fixed an issue where bundle states were not written for appsetups on saved views listing when bundle config was missing. Fixes for example saved map rotation on views. 
- GFI requests now include identifier headers declaring that an Oskari instance is requesting the data.
- Added a route that can be used to request a listing of users "my places" features as GeoJSON with a filename header.
- Added support for localized layer names for user generated data.
- Fixed an issue with global search result limit (defaulted to 100 because of timing issues instead of configured limit).
- Added support for updating capabilities for layers based on data provider or group (previously available options were only by layer or all layers).
- Improved user layer import parsing to allow folders inside zip for the imported files.
- GeoServer artifacts updated for GeoServer 2.19 (updated bundled GeoServer as well/requires manual updating to update, but older installs work as well so update isn't necessary).
- Fix for setup.war webapp (didn't work properly after some internal layers got removed).
- Compiling oskari-server with latest Java 11 update works again.
- Fixes for legend url proxying.
- Added support for AWS ElastiCache Redis setup: https://github.com/oskariorg/oskari-server/pull/716
- Updated dependencies:
  - GeoTools 23.2 -> 24.2
  - Spring 5.3.3 -> 5.3.5
  - Spring-security 5.4.2 -> 5.4.6
  - Session 2020.0.3 -> 2020.0.4
    
## 2.2.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/28?closed=1

- Added server side handlers and registrations for new functionality called `announcements`.
- Reduce logging for common errors like client disconnecting during response writing etc.
- Fixes for analysis functionality.
- Added a new handler for getting configured metadata for a Vector feature source/WFS/OAPIF:
  https://github.com/oskariorg/oskari-server/pull/670.
  Metadata includes things like:
  - attribute names and value types
  - localized names for attributes
  - name of the geometry field

  Note! There are multiple previous implementations that return parts of the same metadata
  (listed in the PR) that will be deprecated and the frontend will be migrated to use the new one
  on future releases.

- Added logic for an option to show a WMS-layer with timeseries without the time controls (as regular layer).
- Added support for GFI response type/info-format `application/json`.
- The group hierarchy depth for hierarchical layer listing can now be configured in
  `oskari-ext.properties` with `layerlist.hierarchy.maxdepth` instead of being hard-coded (defaults to 2).
- Changed my places label from `Link` to `More information` to match the form label when adding/editing features.
- Improved legend image handling for map layers:
  - Admin can now override/add a legend image url for each style for the layer (previously there was just one field)
  - Legend-related configs are now stored in `oskari_maplayer` table `options` column under `legends` key

- Added a plugin mechanism for tracking proxied requests to map layer services.
  This enables adding access audit logging on application code:
  https://github.com/oskariorg/oskari-server/pull/687
- Add initial implementation for checking if a map layer is used by some functionality so we
  can notify admin about it when removing the layer etc.
  Currently only checks if layer is used as a timeseries metadata layer, but this can be improved
  in the future to include notifying usage in embedded maps, region sets in thematic maps etc.

- Changed the Maven repository path for Oskari artifacts under oskari.org (The old one is redirected to the new one and works as well but we migrated away from Nexus so it makes sense to change the path to not reference it).
- Upgraded libs:
  - GeoTools 23.2 -> 24.2 fixing an issue with some Shapefile imports
  - JTS 1.16.1 -> 1.17.1
  - Spring framework 5.2.8.RELEASE -> 5.3.3
  - Spring security 5.3.4.RELEASE -> 5.4.2
  - Spring session Dragonfruit -> 2020.0.3
  - Flyway 6.5.5 -> 6.5.7
  - Jedis 3.3.0 -> 3.5.1
  - MyBatis 3.5.5 -> 3.5.6
  - commons-dbcp2 2.0.1 -> 2.8.0
  - Removed unused com.googlecode.lambda/lambdaj
  - Removed unused com.github.jsonld-java/jsonld-java

## 2.1.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/26?closed=1

WFS-layers can now have a filter for fetching features:
- Admin users can set filter to layer "attributes" 
- https://github.com/oskariorg/oskari-docs/issues/228
- https://oskari.org/documentation/examples/oskari-filter

Log levels can now be updated programmatically:
- Admin users can set log levels at runtime (no UI for this yet)
- Helps debugging issues in production
- For details: https://github.com/oskariorg/oskari-server/pull/658

Improved support for clustered server environment:
- Caches communicate removals/flushes between cluster nodes
- Programmatically setting log level is communicated between cluster nodes
- Documentation available in https://oskari.org/documentation/features/server/clustering

Redis PubSub integration rewrite:
- fi.nls.oskari.cache.JedisSubscriber has been deprecated
- Use org.oskari.cluster.ClusterManager instead

Identifying Oskari instance in HTTP requests:
- Added `IOHelper.addIdentifierHeaders(connection)` for identifying Oskari to services. Adds headers:
  - User-agent header with Oskari/[version] based on metadata in jar-file
  - Referer with value from oskari.domain in oskari-ext.properties
- OSM Search channel and layer tile proxying now sends client identifiers to the service

Other improvements and changes:
- Improvements on WMS capabilities parsing
- Improvements on MIF/MID file reading when importing datasets
- Fix for feature data export on Excel format
- Logging reduced in layer tile proxy and printing for HTTP 404 answers from service
- Other improvements

Library updates:
- Updated commons-lang 3.8.1 -> 3.11
- Dropped commons-lang 2 (updated code to use version 3)

## 2.0.1

Changes coordinates type from string to double on search channels and search results.
Frontend assumes numbers on 2.0 and most of the code casts numbers to strings just for the SearchResultItem setter. 

## 2.0.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/25?closed=1

### Important note for the release!

If you are migrating an older instance to 2.0.0 make sure you migrate to 1.56.0 first
 and start your server at least once with a version based on Oskari 1.56.0. This is a required step as 
 the database needs to be migrated to a specific state before migrating to 2.0.0 and beyond.

This release aims to be functionally same as 1.56.0 but we have included a couple of bug fixes.

### Maven naming changes
 
GroupId for all Oskari Maven artifacts in oskari-server is now `org.oskari`.
Much of the artifactIds have also been changed to match the folder they are in.
Details can be found in [Migration Guide](https://github.com/oskariorg/oskari-server/blob/master/MigrationGuide.md). 

### Library updates

We have updated a bunch of libraries that require changes to applications based on Oskari. 
The changes required are detailed in the [Migration Guide](https://github.com/oskariorg/oskari-server/blob/master/MigrationGuide.md)
and you can also take a look at changes for the [sample-server-extension](https://github.com/oskariorg/sample-server-extension/milestone/5?closed=1)
 to give you a better idea what these mean for your app.

Bigger upgrades include:

- GeoTools 19.2 -> 23.2
- Java Topology Suite (JTS) 1.14.0 -> 1.16.1
- Spring Framework 4.3.26 -> 
- Spring Security etc 4.2.14 -> 5.2.8
- Mapbox Vector Tile library 2.0.0 -> 3.1.0
- Flyway 4.2 -> 6.5.5
- GeoServer 2.13.2 -> 2.17.2 (WPS-modules updated in pre-built package)
- Dropped Ibatis (everything now uses MyBatis instead)

Spring is now managed with "Bill Of Materials" so it's easier for an application to use the same version as Oskari-server 
without re-declaring the version (spring artifacts are in managed dependencies so you don't have to declare version for them on your app).
 
The updates introduce a bunch of code changes that are required to be matched on applications based on Oskari.
These include Java-package changes and even some API changes for Java classes. 
Details can be found in [Migration Guide](https://github.com/oskariorg/oskari-server/blob/master/MigrationGuide.md). 

### Applications and initial content

Support for setup.json files have been dropped. While this might feel like a downgrade it actually makes creating 
initial content for applications simpler and easier to understand. Everything is now done using Flyway-migrations and
everything you could do with a setup.json you can do with migrations. For the oskari-server core modules this includes
 creating database tables for empty database and populating them with initial content. We have dropped all the 1.x
 migrations and with that have been able to cleanup the codebase a fair bit.

We also improved the helpers that can be used with application specific Flyway-modules to make registering bundles, 
map layer and appsetup inserts/updates easier in applications. We also renamed some of the database tables so table names are
now consistent. 

Because of Flyway API changes you will need to modify your application specific migrations.
Details can be found in [Migration Guide](https://github.com/oskariorg/oskari-server/blob/master/MigrationGuide.md). 

### Bug fixes

- Line color for printing has been fixed (now uses defined style again)
- Fixed a caching issue when printing user generated content after changing styling
- Fixed an issue with creating PDF having solid lines  

## 1.56.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/23?closed=1

### Important note for the release!

This release migrates the database migration status tables in preparation for updating to latest version of the Flyway library. This will bring Postgres 12 support for Oskari in the next release. This is also a required version step between versions so you will need to upgrade to this version before upgraging to the next one. We are currently planning on labeling the next version 2.0 as it will have some manual steps that are required when upgrading to it. We are also planning on keeping functional changes low or none for 2.0 to minimize the chance of something breaking as the main focus for the next release is massive library updates.

### User generated content styling, imports (userlayer) and server installation change

User generated content styling has been harmonized at DB level. The styles are now stored similar to styles for WFS-layers enabling more flexible styling in the future. This also adds clustering support for my places etc.

The my places API has been partially changed (=layer metadata) from WFS-T to direct DB access. This gives us more flexibility and works towards removing the "internal" GeoServer we currently use with Oskari. The user content is/has been loaded through db -> GeoServer -> Oskari-server -> browser which adds more serialization/deserialization steps than it needs and creates unnecessary overhead for the functionality. Also we don't really need an internal GeoServer on Oskari for making this work as we are doing very simple things with it and it's one component more that is harder to update automatically with releases/bundled setup. Unfortunately we can't remove it for the 2.0 release but it's something we are working towards when we can for some future Oskari version.

While we are working on removing the internal GeoServer there will be some layers configured automatically for the internal GeoServer that might not work properly. They don't hurt but it might look a bit messy. We will try removing the deprecated configuration for 2.0 version as well. These deprecated layers were used by Oskari when user generated content was loaded as WMS-layers to the browser. Now that they are used as WFS-layers we have cleaned up some views on the database but not the configuration on the GeoServer that tries using them. Again, not a problem but it might look a bit messy if you take a look inside the "internal" GeoServer.

#### GDAL dependency removed

Added MIF/MID-parser implementation! GDAL no longer needs to be installed and configured for Oskari to support this import file type and it was the last one to use it so it doesn't need to be installed at all anymore.

Added GPX 1.0 support for userlayer import (previously only 1.1)

### Other changes

- GeoTools/GeoServer Maven repository has been updated. Builds should now work properly out of the box again (without configuring mirrors for geotools repositories yourself).
- Improved styling support for printout
- Added support for vector tile layers requiring credentials (proxy support)
- Improved support for "capabilities"/describe feature type update for OGC API Features
- OGC API Features conformance URL updated for the WFS-client
- Fixed an issue with WMS-layers capabilities parsing where style was missing.
- Capabilities caching improvements
- Fix for analysis publish permission
- Fix for "Clipping" analysis
- Added configuration for my places GFI-formatting for making it work with WFS-formatters
- Instance domain is now always included in embedded maps permitted domains
- Library updates: Jackson, Log4j2, Flyway

## 1.55.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/24?closed=1

- Added bundle registrations for new bundles: admin-layereditor and layerlist
- Fixed metadata id and legend image handling on server-side for admin-layereditor
- Fixed user content/temporary features input for analysis
- Additional permissions checks added for analysis

## 1.55.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/21?closed=1

WFS integrations:
- Improvements for error handling on WFS integrations (missing schemas and handling unexpected output formats etc)
- Transport related code has been mostly removed from the server code base and will continue to be removed
- Database tables used by transport have been removed with relevant content migrated: https://github.com/oskariorg/oskari-server/pull/509

Layer admininistration:
- new helpers and action route (LayerAdmin) added for the new map layer admin functionality UI (Old ones will be deprecated and removed in a future release)
- Adding layers with LayerHelper from Flyway migrations now uses the same JSON-format, validations and functionality as the new admin UI
- We will continue refining these so they are subject to change in the near future still (at least capabilities handling needs some cleanup)
- GetHierarchicalMapLayerGroups action route can now be used to fetch layers based on id (instead of always returning whole set of layers)
- GetAllRolesAndPermissionTypes action route has been renamed LayerAdminMetadata

Imported datasets (userlayer):
- GPX imports are now read using custom parser implemented in Java (working towards removing an extra step of installing GDAL for Oskari-server)
- Error handling improved for invalid userlayer imports, inconsistent data and styling improvements
- Feature attributes are now sorted based on the imported dataset

Statistical data integrations:
- Multiple improvements for parsing statistical data from PXWeb data sources
- Improvements on handling cached data for indicator lists

Technical changes for enabling server clustering:
- Allow persistent user sessions with Redis. Configuration: https://github.com/oskariorg/oskari-server/pull/491
- Add health and status urls with customizable health checks: https://github.com/oskariorg/oskari-server/pull/493

Other:
- Bundle registrations and link param handlers for 3D related bundles added (enabling apps to use these more easily)
- Fix properties configuration and added some URL-parsing methods for IOHelper
- Layer coverage information improvements. We identified some problem points and added error handling.
- Unnecessary logging removed
- Removed dependencies that were no longer used
- Updated libraries

## 1.54.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/22?closed=1

Fixes an issue with permissions handling and removes all EDIT_LAYER permissions from database. If you ARE using this permission
you can skip the migration but its heavily recommended to run it. All layers with VIEW_LAYER permission have had this permission
so it's unlikely that it is used properly at this moment.

## 1.54.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/19?closed=1

- Fixed an issue where cached data was not flushed after changes in permissions
- Improved background processing for statistical maps functionality
- The forced migration to the new WFS-system has been added (transport is no longer used)
- Fixed an issue with filters in the new WFS-system that caused problems with GeoServer stability
- Added support for arcs and surface geometries on the new WFS-system
- Improved parsing for WFS-services that use mixed geometry types
- Reduced logging for WFS-system on common error scenarios
- Fixed an issue with user content label styling
- Fixed an issue in data provider renaming
- Enabled setup-scripts to insert appsetups that can be loaded with id-reference (instead of uuid)
- Added a default schedule for background capabilities update worker (so configuring automatic updates as admin actually does something)
- Added new action routes to get instance roles, permission types and layers in preparation for new admin functionality

Note! The transport code has not been removed from this release but it hasn't been tested either.
If you must and are willing to put in the effort to go around the forced migration you can try using it but know that it will be removed in the near future.

## 1.53.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/20?closed=1

- Fix WFS-based search channels permission checks
- Fix backend for the content-editor functionality for services requiring authentication
- Fix an issue with empty userlayer tiles
- Allow admins to force the new WFS-backend to use GML as outputFormat instead of preferring JSON (usable for services that don't handle outputFormat parameter properly)

## 1.53.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/18?closed=1

- New WFS-integration backend is now the default replacing the transport webapp! Requires manual updates. See [migration guide](MigrationGuide.md)
- Lots of improvements over 1.52 to the new WFS-integration backend
- CSRF protection has been removed from the webapp as the implementation was problematic with 3rd party cookie blocking.
  Instructions for configuring nginx to handle CSRF has been added.  See [migration guide](MigrationGuide.md)
- Permissions system has been rewritten. Modifying layer url or technical name no longer loses permissions set for the layer
- Added frontend request priority queue implementation using Service Worker (Service Worker code is in the server repository)
- Audit log implementation has been added
- AppSetups can now use specific publish template to override the global template
- Printing WFS-layers now use true vector features for PDFs
- Improvements on printing WMTS-layers
- Oskari now has a default favicon (customizable in oskari-ext.properties)
- More services migrated from Ibatis to Mybatis
- Fixes to analysis functionality
- User session timeout handling improvements
- OpenStreetMap search channel now supports limiting queries to certain bbox
- Server-side localizations are now in UTF-8 to support more languages
- Improvements on parsing indicators from the thematic datasource UN stats (https://unstats.un.org/home/)
- Improvements for handling problematic regions from statistical region sets
- Library updates

## 1.52.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/16?closed=1

- WMS-layers can now be printed with non-default styles
- CSV/Excel export of feature data now supports numeric data properly
- Login now redirects to the referrer page (page where the login was submitted from) instead of root page to allow views like embedded maps have login functionality.
- Fixed and issue with userlayer SLD to show correct line styles for polygons. Requires manual migration for transport based service to work properly (SLDs are not used by the new WFS-system).
- Fixed an issue where having 3rd party cookie support disabled might lead to embedded maps not starting properly
- Fixed an issue where the users default my places layer was shown without a name in embedded maps if it hadn't been renamed from the default
- Added initial support for thematic datasource UN stats (https://unstats.un.org/home/)
- Most of the database operations have been moved from Ibatis to Mybatis. The remaining ones will be migrated at a later date and any new ones should use Mybatis.
- Spring configurations/annotations are now scanned from org.oskari.* packages in addition to fi.nls.oskari.*
- Compilation of codebase is now tested with OpenJDK 8 & 11 and Oracle Java 11
- Removed support for double line style for features as it hasn't been implemented
- French localizations added
- Library updates

This is the first version including the new WFS-integration system:
- Due to replace the current "transport" webapp removing Jetty requirement as server software
- Reads in WFS 1.1.0 and WFS 3.0.0
- Outputs GeoJSON or Mapbox vector tiles (user selectable by admin to optimize for service content)
- My places, userlayers and analysis (user generated content) are supported by the new system
- Supports feature caching (and protects services with circuit breaker like transport)
- Configuration options added to enable custom tile grids to be used with MVT (Works for EPSG:3857 and EPSG:3067 out of the box)
- Doesn't support printing WFS-layers yet
- Doesn't support WFS 2.0.0 yet
- See migration guide for details how to test it out (not enabled by default on this release)

## 1.51.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/17?closed=1

- Fixes for my places and userlayer line/border styles
- Fix metadata search/CSW integration

## 1.51.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/15?closed=1

- Changed logging implementation to log4j version 2.x. See MigrationGuide.md for configuration changes.
- Statistical data sources, region sets and indicators can now fallback to unlocalized name instead of assuming each language having a localized value.
- Statistical region sets are now sorted based on the order number in oskari_maplayer_group_link database table.
- Statistical data source URLs can now have a placeholder for language. When used the indicators are loaded with all languages configured to Oskari to get localized names.
- Layer coverage can now be ignored by adding ignoreCoverage: true in oskari_maplayer.attributes for misconfigured services. Workaround for an issue where layers from misconfigured services are hidden from the map when user browses to a viewport out of the layers coverage area described in GetCapabilities response.
- Upgraded JTS to match GeoTools version to prevent duplicated versions and conflicting classes.
- Improved KML parsing for userlayer imports.
- Improvements on the WFS-MVT/GeoJSON client/server implementation GetWFSVectorTile/GetWFSFeatures action routes.
- Added new action route for getting localized attribute names for WFS-layers "GetLocalizedPropertyNames".
- Improvements to userlayer style handling.
- Fixed SLDs for user generated layers (myplaces, analysis, userlayer) to work with the new GeoServer version. See MigrationGuide for details.
- Fixed GeoServer REST API client and injecting SLDs when using the setup app and the new GeoServer version now works properly.
- Improved the setup app https://github.com/oskariorg/oskari-server/pull/331
- The database tables now have some comments added on them. These are used to generate documentation but are also there to help navigating the database.
- Enabled support for asynchronous controllers for Spring. These can be used for creating action routes supporting long polling XHRs.
- Added initial WFS 3 client.

## 1.50.0

Adds Jetty 9 support for oskari-server.

Note! This version is no longer compatible with Jetty 8. See migration notes!

## 1.49.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/13?closed=1

- JSP-files modified to match the new frontend build. See migrationguide for details.
- search-service-nls removed from oskari-server as it's adapters for NLS Finland specific search channels. See migrationguide for details.
- Added initial support for vectortile based layers
- URLencoding added for some outgoing requests to improve parameters handling
- Improved SearchChannel.isValidSearchTerm() error handling so single erratic search channel doesn't prevent search results from showing.
- Applications now halt on startup if migrations are not successfully completed. You can use the old functionality (force startup after bad migration) by having db.ignoreMigrationFailures=true on oskari-ext.properties.
- Statistical regionsets that are hosted as resource-files can now have numeric IDs and are read as UTF-8 to support wider range of characters
- The server now filters out statistical indicators without names so the UI doesn't list empty/undefined indicators.
- Error handling improved for CSW metadata parsing
- Customized labels fixed for myplaces when a new/empty database is populated
- Fix an issue with XML-parsing when Xalan library is in the classpath (For example SAML has Xalan)
- Fix for layer group matching on database populator/layers now get proper groups
- Removed "startup" info for bundles from database. All required bundles now have to be included in the frontend build.
- oskari-server now has a new "example" app that replaces the previous "sample" app to make it clearer it's an example that can be used to demo the software, but will change in releases so you shouldn't use it as your own geoportal if you wish to have control over content and/or functionalities included in it.
- There's a new URL parameter for skipping the guided tour: showIntro=false
- Print-functionality now supports ArcGIS REST raster layers.
- PropertyUtil.getNecessary() now throws NoSuchElementException instead of plain RuntimeException so misconfigurations are easier to separate from actual errors.
- Library updates

## 1.48.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/11?closed=1

- A new flag for "internal layers" has been added for map layers. Internal layers are not sent to the frontend in map layer listings.
   Baselayers for user generated content and layers for statistical regional divisions have been flagged as "internal".
- "Global" forced proxy: mapfull config and GetHierarchicalMapLayerGroups action route now support a flag to force all layers to be proxied via oskari-server with forceProxy=true
- Scale line calculation in printouts (PDF) has been corrected
- Added cross-site request forgery protection (login/logout and any POST-requests need to include a token).
    Note! Any customized JSP pages might need to be modified to include the token.
- jQuery has been updated to 3.3.1 from 1.10.2.
    The old version works as well but remember to update the script tag on any customized JSP.
- Printout now supports user generated content layers (my places, userlayer, analysis)
- Improved security for user generated statistical datasets.
- User registration now shows the users username on password reset page.
- Axis order issue has been fixed on MyPlaces WFS-T payload (previously features could end up with the wrong axis order on certain map projections).
- Regional divisions for statistical map that are used from resource-files (not from WFS-service) now support different map projections.

## 1.47.1

Fix for determining coordinate systems when importing userlayers/imported datasets.

## 1.47.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/8?closed=1

- Migration from OpenLayers 2 to OpenLayers 4 (see MigrationGuide.md)
- Improvements to userlayer/imported datasets handling
- Statistical maps PXWeb adapter improvements
- Statistical datasource/indicator listings now include regionset and time-variable metadata where available
- Statistical maps error handling improvements
- Layer order modification in hierarchical layers have been fixed
- GetAppSetup can now include common urls as environmental metadata (like login/register) configured in oskari-ext.properties
- Maplayer capabilities caching improvements
- Added Admin route for flushing caches (/action?action_route=Cache&name=[cache name])
- Domain check for published maps is now case-insensitive
- Added server functionalities for handling user generated statistical indicators
- Print functionality now handles layers that require credentials properly
- Saving a view/state of the geoportal now uses the current view as base instead of the system default (Fixes an issue with projection support and saving views)
- Timeseries control can now be included in embedded maps
- Added a concept of "internal layers" that are part of oskari_maplayer table, but are not intended for public view (like baselayers for my places etc)
- Clean up deprecated code

## 1.46.2

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/10?closed=1

- Imported dataset/userlayer coverage/extent is now transformed properly for embedded maps. Fixes an issue where the layer was immediately hidden by the frontend on embedded map.
- Fixed an issue where layer/group sorting order wasn't saved properly on the hierarchical layer listing admin functions
- Added more detailed ordering information for layers as hierarchical layer listing. Layers and groups can now be sorted as a mixed set instead of subgroups always being before any layers in the group.

## 1.46.1

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/9?closed=1

- Fixed secure flag so unsecure services (e.g. WMS) are proxied like before on secured Oskari instances
- GetHierarchicalMapLayerGroups action route response changed to return layers as a flat array beside the groups structure
- Layers can now belong to multiple groups (previously caused layer listing to fail)
- Userlayer/dataset import extent information is now properly transformed so coverage area can be sent for the frontend
- Appsetups without mapfull.conf.mapOptions (relying on defaults) couldn't be used to publish maps. MapOptions config is now optional for publishing.
- Fixed multiple issues for adding, editing and deleting dataprovider/groups via the admin user interface (both hierarchichal admin and the classical one work now).
- Layer ids are now always used in layer JSON sent to the frontend (previously it could be an "external id" when configured)
- oskari_maplayer no longer has the external_id column. It has been moved to another table, made unique and only the
 mapLayers URL-parameter handling cares about it anymore. Migration for database have been provided to replace external ids in
 mapfull.state.selectedLayers and configs for BackgroundLayerSelectionPlugin and LayerSelectionPlugin.
- PermissionService changed to return permissions as a Set instead of a List

## 1.46.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/7?closed=1

Also check the [MigrationGuide.md](MigrationGuide.md) for actions required on upgrade

### Projection support

- The layer JSON now includes information about supported projections.
- Layers can have "forced SRS" which are included to the supported projections even if the capabilities response doesn't include it.
- Supported projections are no longer stored as a long list in a separate database table, but are filtered to only
 include projections supported by the Oskari instance (only include ones that are used in appsetups) and saved as part of the layer information.
- New action route has been added for triggering capabilities update on layer(s)
- ViewService now includes a method to find out projections used in the appsetups.
- GetAppSetup response now includes more information about the Oskari environment like the default appsetups.
- Publish functionality now uses the map options (like projection information/zoom levels etc) from the appsetup where the user is publishing the embedded map
- Listing users saved views and embedded maps now include the projection information so frontend can handle different projections properly.
- WMTS-layers TileMatrixSetId is now sent for the frontend based on the projection the frontend is using.

### New printout implementation

The new printout implementation has been deemed production ready and as such the old one has been removed.
The implementation is baked into oskari-map.war so server setup is a bit more admin-friendly.

### Layer handling

- Database tables around maplayers have been renamed to be more in line with the group/organization naming and
 accommodate the new folder-like structure of layer groups (previously referred to as Inspire-themes).
- Layers can now belong to multiple groups.
- The layer listing action route "GetMapLayers" have been replaced with "GetHierarchicalMapLayerGroups".

### Misc

- Added error handling for layer capabilities parsing where layer didn't have any keywords.
- Added a new service-module for parsing and generating GeoJSON.
- Regionsets for thematic maps can now be added as resources files in GeoJSON format (in addition to reading the regions from a WFS-service).
- Fixed an issue where having myplaces layers as initial selected layers prevented the feature from working properly on OpenLayers 3+ based maps.
- Updated the linked jQuery version from 1.7.2 on Geoportal and 1.7.1 on published maps to 1.10.2 on both.
- Handling user imported datasets (userlayer) have been improved.
- Added serverside functionality for updating userlayer style and other information after it has been imported.
- Improved layer metadata (CSW) parsing.

## 1.45.0

For a full list of changes see: https://github.com/oskariorg/oskari-server/milestone/5

### Statistical maps

- Forced migration! Appsetups having the old statsgrid bundle are migrated to use the new one.
- UserIndicator migration: data and region references are migrated to be compatible with the new statsgrid functionality.
- PX-Web adapter for statistical maps has been improved.
- Sotkanet adapter now fetches data in json format instead of csv.
- Error handling and projection support improved for regionsets in statistical maps.
- Indicator data caching improvements

### Layer capabilities

Capabilities for WMS and WMTS services can now be updated automatically (scheduled update). The parsing has been improved and includes changes to the service API (https://github.com/oskariorg/oskari-server/pull/86)

### Database modifications

- oskari_layergroup table renamed to oskari_dataprovider
- oskari_maplayer.groupid column renamed oskari_maplayer.dataprovider_id
- portti_inspiretheme table renamed to oskari_maplayer_group
- oskari_maplayer_themes table renamed to oskari_maplayer_group_link
- oskari_maplayer_group_link.themeid column renamed oskari_maplayer_group_link.groupid

### transport

Added new config options for oskari(/transport)-ext.properties for controlling when to stop sending requests to service that doesn't answer fast enough (service might be slowed down by getting too many requests and we don't want to pile up requests that will fail):

    # milliseconds as observation window for counting failures before stop sending more WFS-requests to a problematic service (defaults 100 seconds)
    oskari.transport.rollingwindow = 100000

    # amount of WFS-requests that need to fail/layer in rolling window to do a cooldown (circuit break)
    oskari.transport.failrequests = 5

    # milliseconds to wait after circuit break until start sending new WFS-requests to problematic WFS-service (defaults 20 seconds)
    oskari.transport.sleepwindow = 20000

Also increased the defaut timeout from 15 seconds to 25 - configurable with:

    oskari.transport.job.timeoutms = 25000

### Others

- GetFeatureInfo responses handling improved and configuration options added to allow more tags in the response (https://github.com/oskariorg/oskari-server/pull/133)
- Environmental configs moved out of mapfull bundle's config to a new "env" key under GetAppSetup response.
- Database table name changes for inspire themes/layer groups/dataproviders
- WCS-client is now available as service-wcs (https://github.com/oskariorg/oskari-server/pull/89)
- Metadata search by area projection support improved.
- IOHelper improvements for example on URL-parameter handling.
- Fixes for handling analysis related data.
- Analysis and Userlayers now use MyBatis instead of IBatis. Any Ibatis configuration can be removed from oskari-server-extensions and database settings are now completely configured in oskari-ext.properties
- Added an action route for getting default views of the system in preparation of larger projection support functionality.
- WFS-service timeout defaults changed/configuration options added (https://github.com/oskariorg/oskari-server/pull/92)
- New action routes for MyPlaces functionality in preparation of OpenLayers 4 based myplaces.
- Initial version of download basket functionality has been added to oskari-server (https://github.com/oskariorg/oskari-server/pull/74)
- Logging improvements.

## 1.44.1

### XmlHelper

Now configured to reject external entities in XML.

### CSW/layer coverage data handling

The extent of a layer is described with an envelope in WGS84 coordinates. Currently the envelope is reprojected to the
 currently used coordinate system by reprojecting the bottom left corner and the upper right corner coordinates and
  forming another envelope from those. This often creates creates unwanted results especially when dealing with huge
   extents (for example (-180,45),(180,90)) or when using for example North Pole LAEA projections.

This functionality has been improved by generating a polygon from the envelope with (possibly) more than five
 coordinates (each corner + the first corner again for closing the ring) via linear interpolation.
  The generated polygon is then transformed to the target coordinate system.

## 1.44.0

### Library changes and Java requirement change

*Oskari-server now requires Java 8.*

Updated libs:

- Postgres JDBC from 9.3-1102-jdbc41 to 42.1.4
- Jedis from 2.7.2 to 2.9.0
- MyBatis from 3.4.1 to 3.4.5
- jsoup from 1.7.2 to 1.10.3
- Jackson from 2.5.4 to 2.9.0
- Axiom from 1.2.15 to 1.2.20
- Dropwizard metrics from 3.1.0 to 3.2.4

### State cookie handling

Added error handling to state handling.
State cookie parsing no longer assumes that referenced bundles are part of the appsetup.

### GFI response sanitation

Style-tags in GFI html response were interfering with global Oskari CSS.
Response html is now sanitized with Jsoup if presentation type is TEXT which removes potentially dangerous
 script injections as well.
Only tags in in Jsoup ["relaxed" whitelist](https://jsoup.org/apidocs/org/jsoup/safety/Whitelist.html#relaxed--) are allowed.

### Terms of use for map publish functionality

Separate terms of use for map publishing functionality can now be configured in oskari-ext.properties:

    oskari.map.terms.url=https://my.site/terms
    oskari.map.publish.terms.url=https://my.site/terms-for-publishing

The code will look for publish terms first and default to the generic terms config if not found.
Both properties can be localized by adding .fi/.en etc language code at the end of the key.
The value will be populated to publisher/publisher2 bundle configs.

### Userlayer import

New imported userlayers now maintain order for feature properties (user_layer-table's fields-column stored as JSON array instead of object).

The database access library has been updated from Ibatis to Mybatis.
UserLayerDbService has been changed to be suitable for new Mybatis implementation.
Old iBATIS implementation isn't compatible with new UserLayerDbService.
Now layer, style and data inserts are handled in one transaction.

Added error codes (e.g. invalid_file) to response instead of textual messages to support localization in the frontend.

Added feature count to layerJSON response. Also adds a warning object with skipped_features to layerJSON response
 if feature(s) were skipped (no geometry object or geometry is null) during import.

### Improvements to CSW response parsing

Improved data quality information parsing for metadata.

New configuration option for filtering out field values from MetadataSearchOptions query:

    search.channel.METADATA_CATALOGUE_CHANNEL.field.<field name>.blacklist=<comma separated list of values>


### Initial search channel autocomplete functionality

Added initial autocomplete support for search channels. Any search channel that can support autocompletion can
 implement a new SearchAutocomplete interface to participate on the autocomplete results.

See service-search-nls/src/main/java/fi/nls/oskari/search/channel/ELFGeoLocatorSearchChannel.java for an example.

### Transparent fill & stroke on polygons

Polygon style now supports no fill and no stroke. The condition is expressed as allowed null color string values for
 "fill_color" and "border_color" in UserLayerStyle/AnalysisStyle/MyPlaceCategory/WFSLayerStore.

### Initial version of print functionality rewrite

New Maven module service-print which provides built-in png/pdf generation for replacing the current
 servlet-printout once it has been proved production ready.

### Additional layer configuration

WMS-layers GFI functionality can now be enabled/disabled overwriting layer capabilities by adding a configuration in database
 oskari_maplayer.attributes:

    {
        "isQueryable" : false
    }

### Layer updating

Modifying layers registered to Oskari always updated "params" and "attributes" fields even if the client didn't send them.
Now they are modified only if new values are sent in the request and like other parameters can be omitted without problem.


### Test resources/helper

Added a database connection helper in shared-test-resources/TestHelper.
Provide database credentials/url with env property:

    # linux
    export oskari_db_test_props=/opt/my.test.properties
    # windows
    set oskari_db_test_props=C:/somefolder/my.test.properties

Use assumeTrue to check for db connection WHEN test relies on database connection as it might not always
 be available depending on the build environment:

    @Test
    public void testingDB() {
        assumeTrue(TestHelper.dbAvailable());
        DataSource ds = TestHelper.getDBforUnitTest();
        // TODO: use ds for tests
    }

The TestHelper reads the properties to PropertyUtil so remember to clean up after a test using database:

    @AfterClass
    public static void teardown() {
        PropertyUtil.clearProperties();
    }

### ELF GeoLocator search channel

Removed countries listing resource JSON. Instead uses a CountryFilter operation to fetch the countries from
 the service.

### Spationeo integration

Removed serval API integration and now only including the monitor API.

### service-users

The database access library has been updated from iBATIS to MyBatis. DatabaseUserService now uses MybatisRoleService and MybatisUserService.

### Database services MyBatis migration

Services have been migrated from Ibatis to MyBatis library:

- KeywordService
- KeywordRelationService
- AnalysisService
- AnalyisisStyleService
- AnalysisAndStyleService removed because it's not used
- UserService
- RoleService

Ibatis is still used by some services, but any new functionality should be implemented with MyBatis.

## 1.43.0

### servlet-printout

Printout no longer assumes Redis is on localhost. Configurable in print-properties with:

    redis.hostname=localhost
    redis.port=6379

### KTJKIISearchChannel

Removed spammy log messages when results were not found.

## 1.42.1

### User registration functionality rewrite

There was some missing validations and funky looking error handling/messaging on the user registration feature.
It has been rewritten:

 - registration starts by just entering email address
 - invalid/expired tokens are now handled by showing a page where user can continue and not the "next step" with an error message.
 - tokens are now refreshed when the user requests another one so users can't get stuck with an expired token and no means of resetting it.
 - mails now use HTML-templates that are customizable for the Oskari instance
 - passwords now have configurable strength check
 - new users are written to db after they have completed the registration (previously when the initial email was sent for confirmation)
 - emails and usernames are now checked in case-insensitive fashion
 - user content (myplaces, saved views, embedded maps, userlayers, analysis, indicators) is now removed from the database with the user.

To customize password requirements configure oskari-ext.properties:

    # min length for user password
    user.passwd.length=8
    # Require lower and UPPER chars
    user.passwd.case=true
    # Number of days that registration/passwd recover links are valid
    oskari.email.link.expirytime=2

To customize email-templates configure oskari-ext.properties (add files in classpath for example under jetty/resources/templates):

    # defaults
    # on registration init
    oskari.email.registration.tpl=/templates/registration_email.html
    # on registration init if there's already a user account with the email
    oskari.email.exists.tpl=/templates/registration_email_exists.html
    # on "forgot my password"
    oskari.email.passwordrecovery.tpl=/templates/user_passwordreset_email.html
    # on "forgot my password" when there's no user account associated with the email
    oskari.email.passwordrecovery.noaccount.tpl=/templates/user_passwordreset_email_new_user.html

    # you can specify localized versions by adding the language code at the end of the property key
    oskari.email.registration.tpl.fi=/templates/registration_email_finnish_version.html


The default templates are stored in control-users/src/main/resources/fi/nls/oskari/control/users/service
The templates receive variables for:

 - URL to continue the process (link_to_continue)
 - number of days before the token expires (days_to_expire)

### Thematic maps

Fixed an issue where GetRegions action route returns the geometry reference point incorrect projection.

### service-csw

Fixed an issue where data quality fields were not parsed correctly from CSW response.

### Layer urls handling for https-services

Layer urls are modified for the frontend if the Oskari instance is running in a secure URL (https://). Most services only provide
 http urls and won't work properly if the map is loaded using https. For any layer where url doesn't start
  with https:// or / the url is modified to use a proxied url with GetLayerTile action route.
  Previously the protocol was replaced with https:// and to preserve this functionality you can add a property
   for oskari-ext.properties:

    maplayer.wmsurl.secure=https://

### Shapefile import

Shapefile import now tries to find cpg file for identifying the character encoding to be used. This fixes an issue where scandic letters are shown wrong with imported Shapefiles.

The original Shapefile standard defines to use ISO-8859-1 for dpf file encoding. So by default Shapefile is parsed using ISO-8859-1. Optional cpg file can be used to specify the code page for identifying the character set to be used. Also the header of dbf has a reference to a code page (encoding) but unfortunately GeoTools can't handle it reliably. So if you want to use different encoding, you should include cpg file which describes used encoding. e.g. to use UTF-8 encoding create a myshapename.cpg with a texteditor and insert 5 characters (and nothing more): UTF-8.

## 1.42

### Default published JSP-file

The map element now includes the class "published" as some features detect "embedded mode" using it. It was already
present in the published JSP in webapp-map but missing from the default. This fixes an issue where some frontend
 features were started in "geoportal mode" on published maps with oskari-server-extensions (namely statsgrid2016 and maplegend).

### SystemLogger

The simple System.out/err logger can now be configured with environment variable "oskari.syslog.level" with a value of
 debug, info, warn or error. Defaults to debug as before.

### control-base

Removed fi.nls.oskari.util.PrintOutHelper as it's not used anywhere.
Use JSONHelper.isEqual(JSONArray jsonArray1, JSONArray jsonArray2) for comparing arrays instead.

### Search

SearchResultItem.setVillage() and getVillage() have been deprecated and replaced with setRegion() and getRegion().
 JSON-presentation of result items now include a region key in addition to the village key with the same value.
 The village key will be removed in the future.

WFSSearchChannels defaults config is migrated automatically renaming "village" to "region".

SearchOptions action route can now be configured to ignore some of the channels available in the system. This is done
by configuring a comma-separated list of channel ids in oskari-ext.properties:

    actionhandler.SearchOptions.blacklist=METADATA_CATALOGUE_CHANNEL

### AppSetup action route (publisher)

Additional bundles can be whitelisted for publishing using a new property in oskari-ext.properties:

    actionhandler.AppSetup.bundles.simple=maprotator,maplegend

Defaults to maprotator and maplegend as new bundles that can be published. The value of the property is a comma-separated list of
bundle ids. If the payload from the browser has a configuration to a bundle that is whitelisted the bundle is added
to the published map view using the default startup from portti_bundle database table. The configuration and state for
the bundle are merged with the values from the browser before saving to the database.

### Myplaces as WMS-layers (in embedded maps)

My places layers used in embedded maps are shown as WMS-layers to the frontend, but have some custom behavior on the server.
OpenLayers 3 defaults to WMS version 1.3.0 which might cause problems with coordinate order on some instances.
My places layers that are used in embedded maps now use WMS 1.1.0 as a workaround for this.

Map clicks/GetFeatureInfo requests for my places layers should now properly work in embedded maps in
 projections other than EPSG:3067.

### WFS-layer removal fix

The link between a custom SLD-style and a WFS-layer is now removed by database constraint when a layer is removed.
This fixes an issue where the link prevented a WFS-layer with custom style being removed properly.

### Thematic maps

The GetRegions action route now returns the geometry as GeoJSON and reference point for the region in addition to id and name.
The action route now requires srs-parameter to be sent and any statslayer rows in the database should include the srs_name value.

Datasources configuration can now have an info-object including a url key for more information about the datasource.
The frontend will provide a link with the datasource name in attribution information when provided.

### UserLayerProcessor for property_json

The UserLayerProcessor parses features' property_json JSONObject to new actual properties. Now GFI popup and Feature Data table show user data correctly.

selected_feature_params and feature_params_locales are set empty from portti_wfs_layer table to get all non-geometry feature properties.

Properties: uuid, user_layer_id, feature_id, created, updated and attention_text comes from user_layer_data table and are excluded from feature properties.

### Datasource handling

The datasource configuration didn't work properly before when datasource creation was done by Oskari:
all the database modules used the default datasource. For most use cases this is acceptable, but the problem emerges
 when using different database connections for "core" oskari, myplaces, analysis and userlayers.

You can now specify additional connections per flyway module. These are the defaults:

    db.url=jdbc:postgresql://localhost:5432/oskaridb
    db.username=oskari
    db.password=oskari
    db.additional.modules=myplaces,analysis,userlayer,myapp

If you would want to store myplaces to different database you can add the properties:

    db.myplaces.url=jdbc:postgresql://localhost:5432/db_for_usercontent
    db.myplaces.username=oskari
    db.myplaces.password=oskari
    db.myplaces.jndi.name=jdbc/myplacesPool

If the user/pass is the same, you can leave them out and it will default to db.username/db.password property values.
Note! Ibatis-mappings for analysis and userlayers still have hardcoded values as JNDI-name so you might need to override files under
"servlet-map/src/main/resources/META-INF": SqlMapConfig_Analysis.xml and SqlMapConfig_UserLayer.xml.

### service-csw

Date parsing has been improved. Any non-parseable dates are now used as is from the XML. This fixes an issue where CSW data
with dates having for example only year or year and month failed parsing and the user was presented with an empty result.

## 1.41

### CSW Metadata improvements

URL-parameter "metadata" with a value of metadata uuid can now be used to open the metadata info flyout on startup.
Requires metadataflyout bundle to be present in the appsetup and CSW-service configured in Oskari.

Added a way to add an "always on" filter to CSW-searches. This can be done by adding properties to oskari-ext.properties:

Include some randomly named field to this property like "alwaysOnFilter":

    search.channel.METADATA_CATALOGUE_CHANNEL.fields=...,alwaysOnFilter

You can do a like filter by just naming the filter property and providing it a default value for the:

    search.channel.METADATA_CATALOGUE_CHANNEL.field.alwaysOnFilter.filter=somePropName
    search.channel.METADATA_CATALOGUE_CHANNEL.field.alwaysOnFilter.value=someValue

This will result in the query having a like filter:

	<ogc:PropertyIsLike escapeChar="/" matchCase="true" singleChar="?" wildCard="*">
		<ogc:PropertyName>somePropName</ogc:PropertyName>
		<ogc:Literal><![CDATA[someValue]]></ogc:Literal>
	</ogc:PropertyIsLike>

If you want to do an exact match you can include filterOp=COMP_EQUAL as additional property:

    search.channel.METADATA_CATALOGUE_CHANNEL.field.alwaysOnFilter.filterOp=COMP_EQUAL

This will result in the query having an exact filter:

	<ogc:PropertyIsEqualTo matchCase="false">
		<ogc:PropertyName>somePropName</ogc:PropertyName>
		<ogc:Literal><![CDATA[someValue]]></ogc:Literal>
	</ogc:PropertyIsEqualTo>

### Code refactoring

fi.nls.oskari.control.view.modifier.param,ParamHandler has been moved from control-base to
fi.nls.oskari.view.modifier.ParamHandler in service-control Maven module.
Please update any references to point to the new package.

### Layer urls with secure domains

Layers that already use secure url or have no protocol/domain as part of the url are no longer prefixed when used in https-enabled Oskari-instance.

### User registration

The default pages have been visually improved and the default role for registered user is no longer hardcoded as "User".
 The default role can be configured with oskari-ext.properties (defaults to "User"):

    oskari.user.role.loggedIn=User

Any parameters from registration form prefixed with "user_" like "user_phone" will be saved to attributes JSON in
 database table oskari_users. This allows more customization for fields to use on registration.

### OpenTripPlanner

OpenTripPlanner defaults changed: max walk distance has been updated from 1000 to 1000000.

Routing action route now provides the otpURL key in response for users having the admin role. The value is the url
that is used to call OpenTripPlanner so make the feature easier to debug.

### MetaDataFieldHandler

Modifying handler so at field values now can define space replaced char.
If space is wanted to replace some character then following properties can be defined in properties file:
search.channel.METADATA_CATALOGUE_CHANNEL.field.<name>.space.char = ?

This is done because of GeoNetwork cannot query GetRecord for special cases. For example: space are not allowed when searching OrganisationName for LocalisedCharacterString.

### search-service

Removed SearchUtil.maxCount and SearchWorker.maxCount. The same value is now returned by
 SearchService.getMaxResultsCount() and can be configured with oskari-ext.properties:

    search.max.results=100

Classes extending SearchChannel have a new function getMaxResults() which looks for a property:

    search.channel.[CHANNEL_ID].maxFeatures=100

and defaults to 'search.max.results' property. This can be used to configure channel-specific limits. They also have
 a new function getMaxResults(int max) that you can use to pass the requested count from search criteria. This will
 return the requested count if it's smaller than the set limit for the channel. Each SearchChannel should resolve
 maximum results to return by calling getMaxResults(searchCriteria.getMaxResults()).

### servlet-transport

WFS 2.0.0 service responses (feature-engine parsing) can now be logged for debugging with transport-ext.properties:

    transport.response.debug=true

### coordinate transform improvements

Coordinate transforms changed in Oskari to work the same way even when the Geotools flag for enforced coordinate order
 is used (system property org.geotools.referencing.forceXY=true). This is always true when for example Geoserver is
  running on the same appserver as Oskari).

Thanks @kessu:

- Geometry transform improvements and AxisOrder management for all projections as lon,lat in geometries.
- New property configuration in oskari-ext.properties for file import to set default source Crs. Default is used when
 source crs is not found in import file (SHP and MIF):

    userlayer.default.source.epsg=EPSG:3067

- Improvements in DescribeFeatureType parser / wfs 2.0
- Improvements in map click buffer tolerance calculation when map crs is geographical (degree units)
- Optional configuration available for wfs layers (oskari_maplayer attributes column):

        {
            "reverseXY": {
                "EPSG:4326":true
            },
            "longSrsName":{
                "EPSG:4326":true
            }
        }

These are helpful if the WFS-service uses different coordinate order than what is assumed or expects the long version of
 SRS name:
    - "reverseXY": lat,lon order in wfs service
    - "longSrsName": long srsName syntax in GetFeature

### control/service-statistics

Refactored layer-mapping for statistics layers. Removed source_property and layer_property and added config as JSON with
the value of source_property as value in  { "regionType" : [value] } for layers that are mapped to SotkaNET and KAPA
 datasource plugins. Other datasources have an empty config as they didn't use the columns. Config can be used as
 datasource specific layer configuration that the corresponding plugin can use to provide customized handling for layer.

Removed action routes that were not used by old or new statsgrid implementation: GetSotkaRegion,
 GetIndicatorSelectorMetadata, GetIndicatorsMetadata

Renamed classes to make it easier to separate between the two and corresponding functions:
StatisticalIndicatorSelectors -> StatisticalIndicatorDataModel
StatisticalIndicatorSelector -> StatisticalIndicatorDataDimension

Refactored the indicator listing functionality for datasource adapters. There's a new method update() that should begin
processing the datasource for indicators and call onIndicatorProcessed() method for any indicators that are suitable for
using in Oskari. This will be called from a background thread. For very user specific content and fast datasource you can
also override getIndicatorSet() method to return the user indicators directly. This way the update will not be called
 as it's triggered by the default implementation of getIndicatorSet().

Added a scheduled task to update statistical datasources data to cache. It runs by default at 4 AM each night, but can be configured in oskari-ext.properties:

    oskari.scheduler.job.StatisticsDatasources.cronLine=0 0 4 * * ?

To disable running it you can set the value to empty

    oskari.scheduler.job.StatisticsDatasources.cronLine=

Statistical datasource configuration in the database can now include hints for sorting indicator dimension values:

    {
      "hints" : {
        "dimensions" : [ {
          "id" : "year",
          "sort" : "DESC"
        }, {
          "id" : "gender",
          "default" : "total"
        }]
      }
    }

Where id value will match the id of a datadimension item in indicator datamodel. Other keys affect the order of
allowed values for that dimension. Sort (if present) will be done first with either DESC or ASC value. If default is present
 the matching allowed value will be moved as the first value in allowed values.

### service-base

Added list operations for JedisManager: pushToList() and popList().
Added convenience method for checking if a key holds value (returns length of the value. Assumes string value): getValueStringLength().

### Removed deprecated code

Removed GetInspireThemes action route as it has been deprecated for a while and it's not used by anything in the front-end code.
InspireThemes with GET request works as a replacement, but offers also admin functions with other request types.

Removed GetSupportedLocales action route as the information is part of the GetAppSetup response.

### Updated libraries

The managed dependency of java3d.vecmath version 1.3.1 has been updated to new version 1.5.2.
 The groupid for the library has changed between versions and is now javax.vecmath.
 You should update any dependencies using it from:

    <dependency>
        <groupId>javax.vecmath</groupId>
        <artifactId>vecmath</artifactId>
    </dependency>

To the new one:

	<dependency>
        <groupId>java3d</groupId>
        <artifactId>vecmath</artifactId>
    </dependency>

## 1.40

### Configuration improvements

Added "oskari.transport.port" property handling so transport configuration can be configured (as oskari.transport.domain and oskari.transport.url)
in oskari-ext.properties if default values are not used.

Removed default search channel settings from oskari.properties (previously had OpenStreetMap as default).
Now all available search channels that return true from SearchableChannel.isDefault() are used.
 The properties below can be used for keeping the previous behavior:

    # comma-separated list of id for search channel that will be used (defaulted to) if none are explicitly configured
    search.channels.default=OPENSTREETMAP_CHANNEL
    # comma-separated list of search channel ids used by GetSearchResult
    actionhandler.GetSearchResult.channels=OPENSTREETMAP_CHANNEL

Note! if actionhandler.GetSearchResult.channels is used any additional default channels are not included in the search (like wfs-channels).
To get wfs-channels working you need to blacklist individual channels you don't want to include instead of whitelisting:

    # blacklist single channel with id "CHANNEL_ID"
    search.channel.CHANNEL_ID.isDefault=false

### Utils

IOHelper: Added a new convenience method setupBasicAuth(connection, user, pass) which sets up basic auth for the given connection.
JSONHelper: Added a new convenience method createJSONArray(json, bln) to easily create empty arrays from null/problematic JSON param.
content-resources/ViewHelper: Added convenience methods for easily adding a bundle to default views.
Flyway migrations can use them like this to add a bundle to default and user type views if the view doesn't have the bundle already:

        public class Vxx_yy__add_bundle_to_views implements JdbcMigration {
            private static final String BUNDLE_ID = "[replace with bundle id]";

            public void migrate(Connection connection) throws Exception {
                final ArrayList<Long> views = ViewHelper.getUserAndDefaultViewIds(connection);
                for(Long viewId : views){
                    if (ViewHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                        continue;
                    }
                    ViewHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
                }
            }
        }

### MyBatis

Updated MyBatis version from 3.2.7 to 3.4.1.
New module service-mybatis for common helpers/typehandlers to be used with MyBatis.

### control-base

GetSearchResult action route now supports channels-parameter for client-specified search channels.
The value is a comma-separated list of channel ids.

### search-service

Added three new interface methods for SearchableChannel and default implementations for them on the SearchChannel base class:

    // should the channel be used for searching when none has been selected (defaults true)
    boolean isDefaultChannel();

    // does the user have permission to use this channel (defaults true)
    boolean hasPermission(User user);

    /**
     * JSON presentation of channel localization like
     * {
     *     "en" : {
     *         "name" : "Channel name",
     *         "desc" : "This channel is used for..."
     *     }
     * }
     *
     * Defaults to name as channel ID for the default language
     * @return
     */
    JSONObject getUILabels();

Added a new hook for search service to enable SearchChannel factories: ChannelProvider. On startup the search service
will find any @Oskari annotated classes extending ChannelProvider and add any search channels they returns with getChannels() method.
 Any changes to factory-provided channels are propagated to the available channels in SearchService on runtime using an
 included SearchChannelChangeListener interface implementation.

Search service now adds all default search channels as targets for searches when none is specified.

### search-service-nls

Added TM35LehtijakoSearchChannel class to allow both reverse geocoding and normal searches for map sheets in Finland. Also added support for "scale" parameter in GetReverseGeocodingResultHandler for reverse geocoding search channels.

### WFS search services

Added a new module service-search-wfs based on https://github.com/dimenteq/tampere-oskari-server-extension/tree/436550ba3dd7c4f4645ec243487f81d7e1285e08.
Changed the code to use MyBatis instead of Ibatis for more programmatic setup of database usage.
This enables WFS-services registered as maplayers to be used as sources for search channels. The frontend bundle to enable
 configuration is found at Oskari/bundles/tampere/admin-wfs-search-channel. Documentation for using and extending the
 functionality will be added to oskari.org in the near future.

### service-statistics/control-statistics

New server-side functionality for statistical datasources integration. The code has been redesigned to read statistical data
from multiple datasources with a plugin architecture to interpret statistics APIs to common internal format usable by the
 new frontend implementation. Documentation for using and extending the functionality will be added to oskari.org in
 the near future.

## 1.39

### Major migration

 A gigantic migration is being done for publish template and published maps on the system.
 Please see [Migration Guide](MigrationGuide.md) for details.

### User registration

Added initial support for user registration. The functionality can be enabled by adding properties:

    allow.registration=true
    oskari.email.sender=<sender email>
    oskari.email.host=smtp.domain.com

When building as an oskari-server-extension you need to also add the dependency:

    <groupId>fi.nls.oskari.service</groupId>
    <artifactId>oskari-control-users</artifactId>

### Metadata search channel

Code moved from service-search-nls to service-cws for metadata search functionality since it's not NLS spesific.

### service-search-nls

Fixed ELFGeoLocatorSearchChannel common fields override to just handle scale in spesific way.
Other common fields are now properly inherited from base functionality.

### servlet-transport

Browser version information is no longer supported since frontend doesn't send it anymore. This results in some IE 6-8
specific code being removed.

## 1.38.2

### service-search

Added optional language variable to search result items.
Added configurable rank based on result item type for search channels common functionalities:

    # default rank for all items from channel
    search.channel.OPENSTREETMAP_CHANNEL.rank=100
    # type-based rank for place and amenity types
    search.channel.OPENSTREETMAP_CHANNEL.rank.place=120
    search.channel.OPENSTREETMAP_CHANNEL.rank.amenity=130

Updated What3Words searchchannel now returns the language information for results.

## 1.38.1

### service-search-opendata

Updated What3Words searchchannel to API version 2.

## 1.38

### csw record parsing

-added coverage geometry
-added crs-info
-optional metadata rating added (service.metadata.rating - property)
-optional license info added(search.channel.METADATA_CATALOGUE_CHANNEL.licenseUrlPrefix - property)

### publisher bundle handler

Now uses oskari.map.terms.url -property for injecting terms url to publisher bundle like with LogoPlugin.

### AppSetup action route (map publisher)

Statsgrid is now allowed in published map. It will replace publishedgrid in the future, but currently both are allowed.

### Publish action route

The Publish action route has been deprecated for a while and is now removed. AppSetup is the current implementation for
map publisher server side functionality.

### servlet-transport

Now recognizes special style named "oskari_none": tile images are not rendered when style is selected.

## 1.37

### publish template

The publish template for new Oskari installations with the default setup is now Openlayers 3 based.
We are planning to update existing installs in the next release or so with the kind of "recommended, but optional"
 migration that we are doing for publisher in this release. See MigrationGuide.md for details.

### service-users

When using the DatabaseUserService for user management the user password is now encrypted with bcrypt instead of MD5.
Both are supported for logging in, but new passwords are saved using bcrypt.

### GetAppSetup

AppSetup now includes markers for the env properties.

### service-map

SLD Style setup and management is added for wfs layers (versions 1.1.0 and 2.0.0) in admin layer selector.

### transport && control-base

**WFS 2.0.0  / Feature engine**

Feature highlighting is now supported for wfs 2.0.0 layers.

{"cascading":true} setup is optionally available in oskari_maplayer attributes-column for wfs 2.0.0 layers.

 (In this case bbox filter is used in MapClick / GetFeature request. Use this septup, if service doesn't support intersect filter.)

### control-example/GetArticlesByTag action route

Previously removed retried files by removing _ in names other than the last tag

- termsofuse__mappublication__en.html
- termsofuse_mappublication.html

Fixed to work as intended

- termsofuse__mappublication__en.html
- termsofuse__mappublication.html

### DB changes

Added source column to portti_backendstatus table in preparation for multiple service status sources.

## 1.36.2

### Database migration

Restrict oskari_maplayer and oskari_capabilities_cache tables version column to not null and default to empty string.
Cleanup any cached capabilities with null version (there might be many, this is the main reason for the hotfix).

### control-example/GetArticlesByTag action route

Now tries to locate "articles"/content by reducing tags if the exact match doesn't get a hit.

For example with default settings a call to /action?action_route=GetArticlesByTag&tags=termsofuse%2C+mappublication%2C+en
tries to locate page under articlesByTag folder with filenames in the following order:

- termsofuse__mappublication__en.html
- termsofuse__mappublication__en.json
- termsofuse__mappublication.html
- termsofuse__mappublication.json
- termsofuse.html
- termsofuse.json

The first one matching is returned and if none of these are found a message telling to add a file under articlesByTag is displayed.

### control-base / GetWSCapabilities action route

Cached capabilities might be faulty in the database. The GetWSCapabilities action route now tries to fetch capabilities
from the service in such case.

### service-csw

Added errorhandling for missing dates on CSW response.

## 1.36.1

### Migration for published maps

Adds toolbar bundle with no default buttons to all views with type PUBLISHED that don't have it already.
 Configures MarkerPlugin to NOT add it's button to toolbar as it's not supported and should be part of
 publish template.
Toolbar is required for mobile mode functionality in maps and any old published maps don't have it if the user hasn't
selected the history/measurement tools for the map.

### HTML for published maps

Added viewport metadata-tag for published maps.

    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />

## 1.36

### Generic

Geotools version has been updated to 14.2.
Custom JSP-files need to be updated.
See MigrationGuide.md for details.

### GetAppSetup

AppSetup now includes generic information about the environment like languages and available locales.

### service-map

A bugfix to legend image parsing when there were multiple styles with the same name.

Multiple parser configs are now allowed for same feature type (layer name)  (wfs 2).
Parser configs are defined in oskari_wfs_parser_config table.

ResolveDepth attribute setup is added for wfs layers in admin layer selector.

#### WMS layers
WMS service capabilities parsing is improved and prepared to support service versions.
It is now possible to add WMS layers both 1.1.1 and 1.3.0 versions under same wms service in admin layer selector.

Capabilities cache was layertype and service url based, It is now now layertype, service url and version based.

### service-feedback [new]

New service for ``Feedback``  action route

### service-feedback-open311 [new]

New Open311 feedback implementation for feedback service

### Database

Added indexes for oskari_resource and oskari_permission tables.

New ``title`` column inserted into oskari_wfs_parser_config table.

### service-search-opendata

New url parameter **&what3words** for positioning the map in startup
e.g. http://www.paikkatietoikkuna.fi/web/en/map-window?ver=1.17&zoomLevel=6&what3words=examine.flying.daytime&mapLayers=base_35+100+default&showMarker=true

### control-base

#### FeedbackHandler [new, This is POC for time being and will be develop future on]

New handler for feedback requests. Look at oskari.org documentation for more details.

    &action_route=Feedback&method=postFeedback
    &action_route=Feedback&method=serviceList
    &action_route=Feedback&method=serviceDefinition&serviceId=172
    &action_route=Feedback&method=postFeedback

2 new properties must be defined in oskari-ext.properties

    #Api key for posting feedback (Open311 api_key parameter value, only required for posting user's  feeback data)
    #test api base url http://dev.hel.fi/open311-test/v1/
    feedback.open311.key=f1301b1ded935eabc5faa6a2ce975f6
    feedback.open311.url=http://dev.hel.fi/open311-test/v1/

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

**WFS 2.0.0**

WFS layer's current coordinate reference system (crs) is now Transport session's crs (was layer config crs).
This reduces coordinate transformations and process better result.

**WFS / manual refresh**

One new property ''success_nop'' added to notify-job-completed response.
There is no need to the client to act for this layer, when success_nop is true (e.g. refresh feature data)

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
- Support for new link parameter "degrees" was added that lets the map start at a rotated angle (requires maprotator on frontend to work)
