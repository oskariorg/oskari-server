# Content editor

Tampere Oskari bundle

Extends Oskari functionality to support managing layers features and his geometries

## Prerequisites

These uses jetty-8.1.16-oskari version

### Front-end

This bundle needs new Oskari version (jetty-8.1.16-oskari or above)

### Back-end


##### Database

Oskari database is needed but nothing more need to be change

## Installation

Create base folder i.e. Tampere
```
mkdir Tampere
```

Change directory to Tampere

```
cd Tampere
```

Download Oskari (http://oskari.org/build/server/jetty-8.1.16-oskari.zip) and extract archive to current (new created) directory


Goto jetty folder

```
cd jetty-8.1.16-oskari
```

remove Oskari content and clone Oskari client

```
git clone https://github.com/sitooy/tampere-oskari.git oskari
```

Clone Oskari server to base directory (here Tampere)

```
git clone https://github.com/sitooy/tampere-oskari-server.git oskari-server
```

Enter to Oskari server directory

```
cd oskari-server
```

build

```
mvn clean install
mvn clean install -f transport-pom.xml
```

and copy war files (site and transport)
```
cp webapp-map\target\oskari-map.war ..\jetty-8.1.16-oskari\webapps\oskari-map.war
```

```
cp webapp-transport\target\transport.war ..\jetty-8.1.16-oskari\webapps\transport.war
```

### Configuration

To configure new permission type add to oskari-ext.properties
```
permission.types = editLayer
permission.editLayer.id=EDIT_LAYER
permission.editLayer.name.fi=Muokkaa
permission.editLayer.name.en=Edit layer
```

To add new content-editor bundle to Oskari configuration run [SQL file](https://github.com/sitooy/tampere-oskari-server/blob/master/content-resources/src/main/resources/sql/views/01-bundles/tampere/001-content-editor.sql) to add bundle configuration to database. 

Add bundle dynamically to correct roles in oskari-ext.properties. For example:
```
actionhandler.GetAppSetup.dynamic.bundles = admin-layerselector, admin-layerrights, admin-users, admin, content-editor
actionhandler.GetAppSetup.dynamic.bundle.content-editor.roles = Admin
```

Enter to jetty directory and run
```
java -jar start.jar
```
