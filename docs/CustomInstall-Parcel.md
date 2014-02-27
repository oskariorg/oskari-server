# Standalone Jetty installation - Base for parcel application

This document helps to set up the environment. It does NOT instruct how to tune for performance or how things would be optimally configured for a production server.

Assumes pre-installed:

* JDK 1.6+ (tested with 1.6.18)
* Cygwin32 or 64, if windows environment (Windows 7 tested)
* Maven 3+ (tested with 3.0.5)
* PostgresSQL 9.1+ (tested with 9.3) with a oskaridb created for Oskari

# Create oskaridb data base with pgAdmin or with psql

     CREATE DATABASE oskaridb
     WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

## Create postgis extension for the database. 

   (eg. in pgAdmin select oskaridb and in SQL window: *CREATE EXTENSION postgis;* ) 
   - install postgis, if doesn't work (windows 7) (http://download.osgeo.org/postgis/windows/pg93/)
   [detail docs](http://postgis.net/docs/postgis_installation.html#create_new_db_extensions)


Now you are able to create tables with geometry fields.

# Setting up Jetty Hightide 8.1.14

1) Download http://dist.codehaus.org/jetty/jetty-hightide-8.1.14/

2) unpack to selected location (referred as {jetty.home})

3) Configure database connection pool by adding the following snippet in {jetty.home}/etc/jetty.xml:

    <New id="OskariPool" class="org.eclipse.jetty.plus.jndi.Resource">
       <Arg></Arg>
       <Arg>jdbc/OskariPool</Arg>
       <Arg>
          <New class="org.postgresql.ds.PGSimpleDataSource">
             <Set name="User">postgres</Set>
             <Set name="Password">[your pw]</Set>
             <Set name="DatabaseName">oskaridb</Set>
             <Set name="ServerName">localhost</Set>
             <Set name="PortNumber">5432</Set>
          </New>
       </Arg>
    </New>

4) Edit the previous snippet to include actual database properties specific for your environment

5) Download PostgreSQL JDBC driver jar-file and copy it to {jetty.home}/lib/jdbc

    http://jdbc.postgresql.org/download/postgresql-9.3-1100.jdbc4.jar

6) Set default jetty.port for jetty run in etc/jetty.xml file

     <Call name="addConnector">
      <Arg>
          <New class="org.eclipse.jetty.server.nio.SelectChannelConnector">
            <Set name="host"><Property name="jetty.host" /></Set>
            <Set name="port"><Property name="jetty.port" default="8888"/></Set>



---------------------------------------------

# Setting up static frontend code to be server by Jetty 

1) Go to jettys static content webapp

    cd {jetty.home}\webapps\root

2) Get the frontend code from github:

    git clone https://github.com/nls-oskari/oskari.git

3) Rename the created oskari folder to Oskari

    mv oskari Oskari

---------------------------------------------

# Setting up and packaging the oskari backend 

1) Get the backend code from github:

    git clone https://github.com/nls-oskari/oskari-server.git



2) Run all Maven commands listed in *oskari-server/external-libs/mvn-install.txt*

    This adds maven dependencies not found in common repositories to your local maven repository


3) Ignore jetty-env.xml in /oskari-server/servlet-map path, if exists
    e.g. mv jetty-env.xml jetty-env-ignore.xml

4) Fix pom files for parcel extensions   

* Add to oskari/server/servlet-map-pom.xml

  <module>control-example</module>
 
* Add to   oskari_server/servlet-map/pom.xml
  <dependency>
            <groupId>fi.nls.oskari.service</groupId>
            <artifactId>oskari-control-example</artifactId>
            <version>1.0-SNAPSHOT</version>
  </dependency>

5) Compile and package the servlet by running 

    cd oskari-server
    mvn clean package -f servlet-map-pom.xml

6) Copy the war package from under oskari-server/servlet-map/target/ to {jetty.home}/webapps

7) Setup override properties for Oskari. Add an oskari-ext.properties in {jetty.home}/resources/oskari-ext.properties (oskari.trustAllCerts/oskari.trustAllHosts bypasses certificate errors on ssl requests):

    # set to true to get database populated with initial demo content
    oskari.init.db=false

    # true all ssl certs/hosts for debugging! configure certs on the server for production
    oskari.trustAllCerts=true
    # true all ssl certs/hosts for debugging! configure certs on the server for production
    oskari.trustAllHosts=true

    # url path to call for ajax requests/action routes
    oskari.ajax.url.prefix=/oskari-map/?

    #PreParcel query and wfst url
    preparcel.oskari.url=http://localhost:8084/geoserver/oskari/wfs?
    preparcel.user=admin
    preparcel.password=geoserver


    #Url for print service (maplink) - same server as for GeoServer
    service.print.maplink.json.url=http://127.0.0.1:8084/oskari-printout-backend-1.0.3-SNAPSHOT/imaging/service/thumbnail/maplinkjson
    service.print.maplink.geojson.url=http://127.0.0.1:8084/oskari-printout-backend-1.0.3-SNAPSHOT/imaging/service/thumbnail/maplinkgeojson

    #File path for temp pdf/png files
    service.print.saveFilePath = {jetty.home}/webapps/root/print/


    ##############################
    # proxy services
    ##############################
    # adding property/parcel proxy
    oskari.proxyservices = property, print

    oskari.proxy.print.url=http://127.0.0.1:8084/oskari-printout-backend-1.0.3-SNAPSHOT/imaging/service/thumbnail/extent.jsonp

    oskari.proxy.property.url=https://ws.nls.fi/ktjkii/wfs/wfs
    oskari.proxy.property.header.Host=ws.nls.fi
    oskari.proxy.property.header.Authorization=
    oskari.proxy.property.header.Content-Type=text/xml

    # Supported locales, comma separated and default first
    oskari.locales=fi_FI

---------------------------------------------


# Generate/populate database

1) Go to oskari-server/content-resources

2) Configure your database settings in content-resources/src/main/resources/db.properties:

    url=jdbc:postgresql://localhost:5432/oskaridb
    user=postgres
    pass=[your pw]

3) Ignore jetty-env.xml in /oskari-server/servlet-map path, if exists
    e.g. mv jetty-env.xml jetty-env-ignore.xml

4a) Simple one-step setup

* Create the initial db with default view (this isn't perfect but gets you started) 

    mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-parcel


OR 

4b) Step-by-step setup

    a ) Create and empty database by running (dropdb=true forces SQL execute even if there is existing tables):

        mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=create-empty-db

    b) Register bundles to the database by running:

        mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-register-bundles

    c) Add configurations for layers that you want to use in your application. View and parcel bundle refers to layer with certain IDs. These need to match with the inserted layers. To insert layers and permissions to view those layers see:

        content-resources/src/main/resources/sql/exampleLayersAndRoles.sql
        docs/ModifyingInitialDemoData.md
        docs/OskariPermissions.md

    d) Edit references to layer ids and queryUrl in

        content-resources/src/main/resources/json/views/parcel-as-default-view.json

    e) Add a default view (the view configuration referes to content-resources/src/main/resources/json/views/{addview param})

        mvn clean install exec:java -Doskari.addview=parcel-as-default-view.json



NOTES!! 

The provided parcel-as-default-view.json defines parcel action route as:

    "queryUrl" : "/oskari-map/?action_route=GetProxyRequest&serviceId=property",

This needs to be configured to an url responding to parcel loading

    "stickyLayerIds" : [99,90]

The layer ids also need to be configured according to the system/configuration (oskari_maplayer table)


-------------------------------------------

# Startup application

1) Startup Jetty  (cmd.exe in Windows)

    cd {jetty.home}
    java -jar start.jar
   
   (or start with proxy setup, if any  eg. java -jar start.jar -Dhttp.proxyHost=wwwp.xxx.xx -Dhttp.proxyPort=800 -Dhttp.nonProxyHosts="*.yyy.xx|*.foo.fi" --exec)

2) Start application
   *http://localhost:8888/oskari-map* in your browser

   You can login with username "user" and password "user" as a normal user or "admin"/"oskari" as an admin user (no real difference yet)

-------------------------------------------

# Add parcel editing support

1) Install GeoServer http://geoserver.org/ (tested with 2.4.3)

* Use unique port for GS  e.g. 8084 (not equal to Oskari Jetty run)
*   If GS is already available, look guidelines in point 9) 


2) Replace Geoserver data directory, add OskariMarkFactory extension and start GeoServer (GS)

* Replace {geoserver}\data_dir with oskari\oskari-server\content-resources\config\geoserver\data
* Start GeoServer {geoserver}\bin\startup
* Check data configuration with GS admin  (**http://localhost:8084/geoserver/web** --> layer preview layers my_places and my_places_categories ! these are empty in the initial  state)
* Restart GS 

3) Set GS layer configurations for preparcel and preparcel_data tables and security setup / data 

* Use GS Admin (see below section **Details for GeoServer configuration**).

# Add print service support

1) Build  oskari_server/servlet-printout ( mvn clean install) and copy oskari-printout-backend-1.0.x-SNAPSHOT.war from target path to {jetty.home}/webapps path 

2) Install redis, if not yet in use (yum install redis)
   * option: create conf file for redis 
             /etc/redis.cfg
             # Redis configuration file example
             # ...
 
             dir /data/redis/
 
            # ...
            maxmemory 1gb
 
            vm-swap-file /data/redis/redis.swap

3) Install fonts 
    sudo yum install liberation-mono
    sudo yum install liberation-serif
    sudo yum install liberation-sans

4) Copy default.properties somewhere under {jetty.home}/resources/oskari-printout-backend.properties and modify/check url values 

5) Add to GeoServer startup.bat -Dfi.nls.oskari.imaging.config={jetty.home}/resources/oskari-printout-backend.properties 

Sample startup.bat 
call "C:\Program Files (x86)\Java\jdk1.6.0_18\bin\java.exe" -DGEOSERVER_DATA_DIR="C:\Omat\GeoServer_2_4_0\data_dir" -Xmx512m -XX:MaxPermSize=128m -DSTOP.PORT=8079 -DSTOP.KEY=geoserver -Djetty.port=8084 -Dfi.nls.oskari.imaging.config={jetty.home}/resources/oskari-printout-backend.properties-Dhttp.proxyHost=wwwp.xxx.xx -Dhttp.proxyPort=800 -Dhttp.nonProxyHosts="*.yyy.xx|*.foo.fi"  -Djetty.logs="C:\Omat\GeoServer_2_4_0\logs" -jar "C:\Omat\GeoServer_2_4_0\start.jar"


4) Restart GeoServer


## Sample printout properties file

layersURL=http://127.0.0.1:8888/oskari-map?action_route=GetMapLayers&lang=fi
layer.urlTemplate.wmslayer=%2$s%3$s
layer.urltemplate.myplaces=http://127.0.0.1:8084/geoserver/wms?myCat=%1$s&ID=myplaces_%1$s
layer.urltemplate.myplaces.layers=oskari:my_places_categories
layer.credentials.myplaces=
layer.urltemplate.wfslayer=http://127.0.0.1:8888/oskari-map?action_route=GET_PNG_MAP&flow_pm_wfsLayerId=%1$s
layer.urltemplate.statslayer=http://localhost/dataset/statslayer/service/actionroute
layer.cache.exclude=90,91,250
layer.tiles.url.whitelist=^http:\\/\\/(www|demo|static|cdn)\\.paikkatietoikkuna\\.fi\\/.+$|^http:\\/\\/(a|b|c|d)\\.karttatiili\\.fi\\/.+$|^http:\\/\\/karttatiili\\.fi\\/.+$|^data:image\\/png;base64,.+$|^data:image\\/jpeg\\;base64,.+|^/web\\/.+$
layer.timeout.seconds=32
geojson.debug=true


# Details for GS configuration : 

* Open geoserver admin tool (eg. localhost:8084/geoserver) Default admin = admin,  pw = geoserver.
* Create new workspace *oskari*, if not . Set it as default and enable WFS and WMS services. Set namespace URI: (eg. http://www.oskari.org)
* Add your database as a new postgis store
    * Store->Add new Store->PostGIS
    * Select *oskari* as workspace and name the data source: "my_places_categories" 
    * Check it as Enabled
    * Set the connection parameters, for example: 
        * host: localhost, port: 5432
        * database: oskaridb, schema: public
        * user: *username*, passwd: *password* (this sample default postgres/postgres)
* Add layers preparcel, preparcel_data
    * Layers->Add a new resource->Add layer from "ows:my_places_categories"
    * Publish preparcel (no geometry field )
        * Set Declared SRS (eg. EPSG:3067)
        * Set Native bbox (50 000, 6400000, 800000, 8000000)
        * Set Lat/Lon Bounding Box (eg. press compute from native bounds)
        * Publishing->Set Default Style (e.g. "Polygon") (no matter what)
        * Save
    * Publish preparcel_data :
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
          oskari.prepracel.w       *
          oskari.preparcel_data.w        *
          