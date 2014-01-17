oskari-printout-backend
=======================

# Project

oskari-printout-backend

# Summary

A JAX-RS service to be used with oskari frontend and oskari-backend. 

This project produces a JAX-RS service that combines printouts from map tiles as PNG images, 
layered PDF documents based  on maplink parameters or JSON document specification.

API is based on oskari de-facto maplinks as well as 
 expanded oskari layer JSON definitions with additions to support printout settings.

This printout service provides clients with upscaling and downscaling of resulting images
to support thumbnails and previews in user-interfaces.
 
    
This is not a general purpose printout implementation. 

# Dependencies

Spatial operations are based on GeoTools version 10.2.
- https://github.com/geotools/geotools

Raster Map output is generated with GeoTools 10.2.
- https://github.com/geotools/geotools
 
GeoJSON processing is based on GeoTools GeoJSON module.

JAX-RS Web Api is based on Jersey 1.17
 - https://jersey.java.net/documentation/latest/user-guide.html
 - https://jersey.java.net/documentation/1.7/user-guide.html

Tile Grid calculations are borrowed from both OpenLayers and GeoWebCache.
- http://geowebcache.org/
- https://github.com/GeoWebCache/geowebcache

PDF output is based on Apache PdfBox
- http://pdfbox.apache.org/

PPTX output is based on Apache POI
 - http://poi.apache.org/
 
PNG upscaling and downscaling is based on Morten Nobel Lanczos3 algorithm implementations.
 - http://en.wikipedia.org/wiki/Lanczos_algorithm
 - http://en.wikipedia.org/wiki/Lanczos_resampling
 - http://blog.nobel-joergensen.com/2008/12/20/downscaling-images-in-java/

Image tile requests use Apache Async HttpClient.

Redis (via Jedis) is used as blob cache for in-server cluster of  portti-map-imaging instances
 - https://github.com/xetorthio/jedis/

# Requirements

A Working Eclipse IDE environment 
- Eclipse (Juno) 
- Eclipse Maven integration (m2e)

 
# Test Requirementes
- Access to Jetty at localhost:8888 (or via SSH tunnels) (embedded mock jetty setup may be available at a later stage)
- Access to WMS services referenced in test JSON spec files

# Run time requirements
- Access to WMS services referenced JSON configurations
- optional access to REDIS at 127.0.0.1:6379 (as of 1.0.4 optional)


# Build

Eclipse > Run as > Maven clean

Eclipse > Run as > Maven Build > war:war 
or
Eclipse > Run as > Maven install

# Test

Eclipse > Run as > Maven Test

# Notes

This software reads map tiles from given WMS sources and outputs combined RASTER map image.
Output formats are currently PNG, layered PDF with embedded PNG images, simple PPTX with PNG image.
PDF output is layered set of PNG images embedded in PDF document (this includes rasterized GeoJSON vector objects).

# Notes on projections, grid sets and resolutions 
EPSG:3067 is the default supported projection with NLSFI resolution stack
This may now be changed with configuration. Sample setup for JHS180 settings is included
and may be activated with properties.

Change requires setting EPSG code in properties file. 
Appropriate resolution settings and grid sets must be declared in geowebcache_template.xml
(GeoWebCache is used to calculate tile BBOxes) 

# Restrictions

This software is compatible with oskari map layer definition implementations and as such shall not
be considered a generic solution for map printout producing.

This is view snapshot as raster - not a real map output. 
Map scales equal to those shown on screen and are not very useful printed.    

Some oskari specific processing of special layer types in addition to default WMS handling is embedded in requesting map tiles.

Running (tests also) requires Jetty Servlet container and Redis in memory db backend as well.
Some HTTP server serving layers JSON for a list of provided map is required when using maplink GET urls.
For POST based mapping layers JSON is embedded within JSON map spec. 

PDF vector output MAY be implemented at a later stage.

Assumes OpenLayers either 2.12 or 2.13 DPI settings (default is 2.12 compatibility for now).


# Security notes

Maplink (URL args) based output uses only WMS servers declared in layers JSON which is read from URL defined in config. 
JSON tile URLs are whitelisted based on RegExp in properties file

Security Issue: Posted JSON Spec based printing uses WMS servers declared in JSON. POST JSON service shall not be directly accessible to public.  

# License

This is free software with EUPL license see LICENSE.md 
  
# Installation 

Fix default.properties file in src/main/resources/fi/nls/oskari/printout/ws/jaxrs/resource/default.properties 
OR configure JVM command line (see following section)
    
    Run as > Maven Build > war:war
	or
    Run as > Maven install


    copy WAR to some servlet container

# Properties file 

Instances of this service read the some configuration settings from the default (see above) or given properties file. 
The optional location of properties file is declared via JVM command line definition
  -Dfi.nls.oskari.imaging.config=/etc/default/oskari-printout-backend.properties
  
Values in <> brackets shall be replaced with installation specific URLs.

```
layersURL=<URL-that-declares any public map layers- see following section layers.json>
layer.urlTemplate.wmslayer=%2$s%3$s
layer.urltemplate.myplaces=<URL-to-MyPlaces-rendere-action-ROUTE>?myCat=%1$s&ID=myplaces_%1$s
layer.urltemplate.myplaces.layers=oskari:my_places_categories
layer.credentials.myplaces=Basic <base64encoded username:password>
layer.urltemplate.wfslayer=<URL-to-WFS-renderer-action-route>?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=GET_PNG_MAP&flow_pm_wfsLayerId=%1$s
layer.urltemplate.wfslayer.legacy=true|false (default false)
layer.urltemplate.statslayer=<URL-to-statslayer-renderer-actionroute>
layer.cache.exclude=<comma separated list of oskari map layer ids that shall not be pushed to redis blob cache 
layer.tiles.url.whitelist=^http:\\/\\/(www|demo|static|cdn)\\.paikkatietoikkuna\\.fi\\/.+$|^http:\\/\\/(a|b|c|d)\\.karttatiili\\.fi\\/.+$|^http:\\/\\/karttatiili\\.fi\\/.+$|^data:image\\/png;base64,.+$|^data:image\\/jpeg\\;base64,.+|^/web\\/.+$
 ```


# Properties file sample for local dev environment

 ```
layersURL=http://127.0.0.1:8080/web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=GetMapLayers&lang=fi
layer.urlTemplate.wmslayer=%2$s%3$s
#layer.urltemplate.myplaces=http://127.0.0.1:8080/karttatiili/myplaces?myCat=%1$s&ID=myplaces_%1$s
layer.urltemplate.myplaces=http://127.0.0.1:8888/geoserver/ows
layer.urltemplate.myplaces.layers=oskari:my_places_categories
layer.credentials.myplaces=Basic <base64encoded username:password>
layer.urltemplate.wfslayer=http://127.0.0.1:8080/karttatiili/myplaces?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=GET_PNG_MAP&flow_pm_wfsLayerId=%1$s
layer.urltemplate.statslayer=http://127.0.0.1:8080
layer.cache.exclude=90,91,250
layer.tiles.url.whitelist=^http:\\/\\/(www|dev|demo|static|cdn)\\.paikkatietoikkuna\\.fi\\/.+$|^http:\\/\\/(a|b|c|d)\\.karttatiili\\.fi\\/.+$|^http:\\/\\/karttatiili\\.fi\\/.+$|^data:image\\/png;base64,.+$|^data:image\\/jpeg\\;base64,.+|^/web\\/.+$
layer.timeout.seconds=32
geojson.debug=true
redis.blobcache=false

 ```

# layers.json 

A Json list of map layers that are served when using maplink based printing with GET protocol

see oskari documentation for declaration of layers json structure and fields.

# MapLink arguments 

(to be documented)



# Post JSON spec 

(to be documented)


# ICC 
Project includes ICC profile

/oskari-printout-backend/src/main/resources/org/color/sRGB_IEC61966-2-1_black_scaled.icc

If this is a problem please contact 
   paikkatietoikkuna /(-at-)/ maanmittauslaitos.fi


# PDF/a

Support for PDF/A is in pdfbox trunk which is not available in maven repositories atm.
WebContent/WEB-INF/lib contains 2.0.0-SNAPSHOT jars for pdfbox.
