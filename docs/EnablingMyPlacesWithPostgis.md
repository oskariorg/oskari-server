# Enabling My Places With Postgis

Assumes pre-installed:

* JDK 1.6+ (tested with 1.6.18)
* Maven 3+ (tested with 3.0.5)
* PostgresSQL 9.1+ (tested with 9.3) with a oskaridb created for Oskari
* Jetty (http://dist.codehaus.org/jetty/jetty-hightide-8.1.14/)
* See BasicInstall-OskariServletPostgres.md


1) Create postgis extension for the database. 

(eg. in pgAdmin select oskaridb and in SQL window: *CREATE EXTENSION postgis;* ) 
- install postgis, if doesn't work (windows 7) (http://download.osgeo.org/postgis/windows/pg93/)
[detail docs](http://postgis.net/docs/postgis_installation.html#create_new_db_extensions)


Now you are able to create tables with geometry fields.

2) Rebuild oskaridb:

Build db structure and data (basic tables, my places and wfs)

* cd  oskari-server/content-resources
* mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-myplaces-wfs2
	* To get myplaces timestamps working correctly you need to add triggers to myplaces table. SQLs for these are listed in *oskari-server/content-resources/src/main/resouces/sql/PostgreSQL/trigger-myplaces.sql*. You need to run these manually in pgAdmin SQL-window  since at the moment the sql-parser can't handle them correctly.

3) Install GeoServer http://geoserver.org/ (tested with 2.4.3)

* Use unique port for GS  e.g. 8082 (not equal to Oskari Jetty run)
*   If GS is already available, look guidelines in point 9) 


4) Replace Geoserver data directory, add OskariMarkFactory extension and start GeoServer (GS)

* Replace {geoserver}\data_dir with oskari\oskari-server\content-resources\config\geoserver\data
* Add GS symbolizer extension OskariMarkFactory-1.0.jar to {geoserver}\webapps\WEB-INF\lib and geoserver-ext\OskariMarkFactory\src\main\resources\dot-markers.ttf to {geoserver}\data_dir\styles (**see \oskari-server\geoserver-ext\OskariMarkFactory\readme.txt**)
* Start GeoServer {geoserver}\bin\startup
* Check data configuration with GS admin  (**http://localhost:8082/geoserver/web** --> layer preview layers my_places and my_places_categories ! these are empty in the initial  state)
* If there are problems in layer preview, check workspace and store-setups with GS Admin (see point 9) ).
* Restart GS

5) Check settings in oskari-ext.properties (jetty-hightide-8.1.14.v20131031\resources )

Uncomment or add the following settings and set them point to your geoserver url

	oskari.proxy.myplacestile.url=http://localhost:8082/geoserver/wms?CQL_FILTER=
	oskari.proxy.myplacestile.handler=fi.nls.oskari.proxy.MyPlacesProxyHandler 
	oskari.proxy.myplacestile.user=admin
	oskari.proxy.myplacestile.pass=geoserver

	myplaces.ows.url=http://localhost:8082/geoserver/oskari/ows?
	myplaces.wms.url=http://localhost:8082/geoserver/oskari/wms?buffer=128&tiled=yes&tilesorigin=0,0&CQL_FILTER=
	myplaces.user=admin
	myplaces.password=geoserver
	# Base WFS layer id for myplaces (portti_maplayer and portti_wfs_layer tables)
	# Find correct id layer later on when my_places wfs base layer is inserted
    myplaces.baselayer.id=14
    # My places namespace
    myplaces.xmlns=http://www.oskari.org
    # My places namespace prefix
    myplaces.xmlns.prefix=oskari

6) Install WFS transport service, if not yet installed

* Look at [SettingUpTransportWFS.md](SettingUpTransportWFS.md)


7) Test Oskari

*  Start **eg. http://localhost:8888/oskari-map?viewId=4** in your browser
* A Logged-in-user should be able to use myplaces (add own points, lines and polygons)

8) If there are troubles to add myplaces, You should check correct  feature namespace (default NS should be *http://www.oskari.org* with prefix *oskari*) and ajax url according to your environment.

Example configuration can be found in 
**content-resources/src/main/resources/json/views/postgres-myplaces-view.json**

wmsUrl should be normal ajax url (default is /? or /oskari-map/?) + action_route=MyPlacesTile&myCat=

* Edit postgres-myplaces-view.json and re-execute items in point  2)

or

* use pgAdmin SQL eg.
** UPDATE portti_view_bundle_seq SET config='{"queryUrl":"[REPLACED BY HANDLER]","wmsUrl":"/oskari-map/?action_route=MyPlacesTile&myCat=","featureNS":"http://www.oskari.org"}' 
 where bundleinstance='myplaces2'* **
  
You may also need to change service-map/src/main/resources/fi/nls/oskari/map/myplaces/service/GetFeatureInfoMyPlaces.xsl to fix your geoserver url and namespace

9) Guidelines for GS configuration : 

* Open geoserver admin tool (eg. localhost:8082/geoserver) Default admin = admin,  pw = geoserver.
* Create new workspace *oskari* . Set it as default and enable WFS and WMS services. Set namespace URI: (eg. http://www.oskari.org)
* Add your database as a new postgis store
	* Store->Add new Store->PostGIS
	* Select *oskari* as workspace and name the data source: "my_places_categories" 
	* Check it as Enabled
	* Set the connection parameters, for example: 
		* host: localhost, port: 5432
		* database: oskaridb, schema: public
		* user: *username*, passwd: *password* (sample default postgres/postgres)
* Create and select style		
	* You can create your own style choosing Styles and modifying existing style or uploading a new style file.
		You can find an example file at example-server-conf/MyPlacesSampleStyle.sld
		Note! the sample contains the necessary descriptions, but is by no means 
* Add layers categories, my_places, and my_places_gategories
	* Layers->Add a new resource->Add layer from "ows:my_places_categories"
	* Publish categories (no geometry field )
		* Set Declared SRS (eg. EPSG:3067)
		* Set Native bbox (50 000, 6400000, 800000, 8000000)
		* Set Lat/Lon Bounding Box (eg. press compute from native bounds)
		* Publishing->Set Default Style (e.g. "MyPlacesSampleStyle") (not used)
		* Save
	* Publish my_places :
		* Set Declared SRS (eg. EPSG:3067)
		* Set Native bbox (50 000, 6400000, 800000, 8000000)
		* Set Lat/Lon Bounding Box (eg. press compute from native bounds)
		* Publishing->Set Default Style (e.g. "MyPlacesSampleStyle") (not used)
		* Save
	* Publish my_places_categories:
		* Set Declared SRS (eg. EPSG:3067)
		* Set Lat/Lon Bounding Box (eg. press compute from native bounds)
		* Publishing->Set Default Style (e.g. "MyPlacesSampleStyle") (used for rendering in Oskari)
		* Save
* Set Security items for data / Add new rule
  e.g.

          *.*.r                     *
	      oskari.categories.w       *
	      oskari.my_places.w        *
	      GeoServer Admin

* Set oskari:my_places_categories Layer / tile Caching setup

    Tile cache configuration

        (x)Create a cached layer for this layer
         - use default values
         - set
           Gutter size in pixels
           100