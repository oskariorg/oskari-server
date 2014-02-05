# Release Notes

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

### Service-OGC and control-wfs

Have been deprecated. The required parts have been moved to service-map and the currently recommended backend component for WFS-functionality is the transport-servlet.

The deprecated modules can still be found inside oskari-server/deprecated folder.

### Transport

Added override properties handling. Tries to search for file 'transport-ext.properties' in classpath and if found, overrides default values loaded from config.properties if 

### GetAppSetup/ParamHandlers

It's now possible to add preprocessors for ParamHandlers used in GetAppSetup. Check [service-control/README.md](service-control/README.md) for more
info about preprocessing parameters.

## PublishHandler

Has been some what refactored for clearer implementation. Views and Bundles can now be copied with clone() method.
Still gathers the view to be saved as JSON instead of modifying the view object, this will propably be streamlined as well in the future.

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
