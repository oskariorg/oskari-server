# Migration guide

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
