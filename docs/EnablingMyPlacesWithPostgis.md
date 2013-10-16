# Enabling My Places With Postgis

You need to have Postgresql installed and set up.

1) Install PostGIS http://postgis.net/ (testet with version 2.1 (with postgresql 9.3))

Create postgis extension for the database. 
(eg. in pgAdmin select new extension and select postgis.)
Now you are able to create tables with geometry fields.

2) Install Geoserver http://geoserver.org/ (tested with 9.4)

3) Check settings in oskari.properties 
	Following settins uncomment the following settings and set them point to your geoserver location
	oskari.proxy.myplacestile.url=http://localhost:8080/geoserver/wms?CQL_FILTER=
	oskari.proxy.myplacestile.handler=fi.nls.oskari.proxy.MyPlacesProxyHandler 
	oskari.proxy.myplacestile.user=geoserver
	oskari.proxy.myplacestile.pass=geoserver

	myplaces.ows.url=http://localhost:8080/geoserver/ows/ows?
	myplaces.wms.url=http://localhost:8080/geoserver/wms?buffer=128&tiled=yes&tilesorigin=0,0&CQL_FILTER=
	myplaces.user=admin
	myplaces.password=geoserver


4) Configure geoserver: 
 * Open geoserver admin tool (eg. localhost:8080/geoserver) Default admin = admin,  pw = geoserver.
 * Create new workspace ows. Set it as default and enable WFS and WMS services. Set namespace URI: (eg. http://oskari.org/myplaces)
 * Add your database as a new postgis store
 	* Select ows as workspace and name the data source: "my_places_categories" 
 	* Check it as enablet
	* Set the connection parameters, for example: 
		* host: localhost, port: 5432
		* database: oskaridb, schema: public
		* user: *username*, passwd: *password* 
 * Add layers my_places, categories, and my_places_gategories
 	* my_places:
		* Set Declared SRS (eg. EPSG:3067)
	* categories:
		* Set Declared SRS (eg. EPSG:3067)sama taso.
	* my_places_categories:
		* Set Declared SRS (eg. EPSG:3067)
 * Create and select style		
 		* You can create your own style choosing Styles and modifying existing style or uploading a new style file. You can find an example file at example-server-conf/MyPlacesSampleStyle.sld


5) Build: 
Build with
 mvn -f servlet-map-pom.xml clean install -Doskari.dropdb=true -Doskari.setup=postgres-myplaces

After opening  http://localhost:2373/?viewId=4 in your browser my_places and categories tables should be added to your database.
