# WFSServiceTester.java

## Settings

Test properties can be modified in /oskari-server/service-wfs/src/main/resources/service-wfs.properties.

    # Logger implementation - SystemLogger logs into System.out/err, replace with logging implementation of your choice
    oskari.logger=fi.nls.oskari.log.SystemLogger

    # Proxy settings - these could be setup also via VM options on command prompt or Idea Run / edit configuration setup
    http.proxyHost=wwwp.nls.fi
    http.proxyPort=800
    http.nonProxyHosts="*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi

    # for wfs layer insert script writer (no sql script generation, when property in undefined)
    # template is under resource path
    wfs.sql.insert.layer.template=template_WFS_layer_insert.sql (sql template file for sql script)
    wfs.sql.write.path=C://Omat/sql/    (write path for generated sql-files, java.io.tmpdir is used, if not defined)



## VM options for command line or for Idea run

    * wfs.service.url  WFS service url e.g. `-Dwfs.service.url=http://geo.stat.fi:8080/geoserver/wfs`
    * wfs.service.user WFS service credentials
    * wfs.service.pw
    * wfs.service.epsg  Spatial reference system code  e.g. "-Dwfs.service.epsg=EPSG:3067" (if not defined, default service epsg is in use)

    * oskari.layer.group Layer group name e.g. Tilastokeskus  (only used, when wfs.sql.insert.layer.template is defined)
    * oskari.inspire.theme  Inspire theme  e.g. Tilastoyksiköt (only used, when wfs.sql.insert.layer.template is defined)
    * oskari.wps_params    e.g. {"no_data":-1} (only used, when wfs.sql.insert.layer.template is defined)
    * oskari.layer.opacity    e.g. 30 (only used, when wfs.sql.insert.layer.template is defined)

## Run

### Maven

     cd /oskari-server/service-wfs/
     mvn clean install exec:java -Dwfs.service.url="http://geo.stat.fi:8080/geoserver/wfs" -Dhttp.proxyHost=wwwp.nls.fi -Dhttp.proxyPort=800
     (if not working, do 1st cd ../oskari-server   -> mvn clean install)

### IDEA

    1. Select WFSServiceTester.java to Idea editor window
    2. Run  >
    3. Setup Idea Run / edit configurations / Application / WFSServiceTester / VM options -> e.g. -Dwfs.service.url=http://www.ign.es/wfs-inspire/ngbe -Dhttp.proxyHost=wwwp.nls.fi -Dhttp.proxyPort=800
    4. Run or Debug

## Tests

    1. Program tests WFS versions 1.0.0, 1.1.0 and 2.0.0
    2. Test HTTP GET GetCapabilities for each version
    3. Test GeoTools GetCapabilities and DescribeFeature (WFSDataStore) for each version
    if Test 3. is OK, then there are GetFeature tests for each featuretype
       4. Test http GET Getfeature 
       if Test 4. is OK, then there are Transport WFS parser test and Geotools BBOX GetFeature test
       5. Transport WFS parser test
       6. Geotools BBOX GetFeature test

## Sql scripts generator

    * Program writes sql script files for each featuretype during tests when wfs.sql.insert.layer.template is not null (only version 1.1.0)    
    * output path is defined by wfs.sql.write.path property
    * use VM options e.g. -Dwfs.service.url=http://geo.stat.fi:8080/geoserver/wfs -Doskari.layer.group=Tilastokeskus -Doskari.inspire.theme=Tilastoyksiköt for oskari layer group and inspiretheme

## Results

### Example run with  

    -Dwfs.service.url="http://geo.stat.fi:8080/geoserver/wfs"  -Dwfs.service.epsg=EPSG:3857


    ++++++++++++++++++++++++++++++++++++++++++++++++ WFS Test START - Wed Jul 09 14:47:21 EEST 2014
    Service url: http://geo.stat.fi:8080/geoserver/wfs 
    WFS test - version: 1.0.0:::: START <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  
    WFS version test  (GetCapabilities http request) <<<< HTTP GETCAPABILITIES ------- 
    WFS Http GetCapabilities request OK - version 1.0.0 
    ----------------------------------------------------------------------------- >>>> 
    WFS version test  (GetCapabilities GeoTools ) <<<< GEOTOOLS GETCAPABILITIES AND DESCRIBEFEATURE ---------- 
    WFS GT GetCapabilities request OK - version 1.0.0 
    ----------------------------------------------------------------------------- >>>> 
    WFS DescribeFeature test  (Http request ) <<<< HTTP DESCRIBEFEATURE ---------- 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:avi1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:avi4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:avi1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:avi1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:avi4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:avi4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:ely1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:ely4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:ely1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:ely1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:ely4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:ely4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:kunta1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:kunta4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:kunta1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:kunta1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:kunta4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:kunta4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:maakunta1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:maakunta4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:maakunta1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:maakunta1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:maakunta4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:maakunta4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: oppilaitokset:oppilaitokset - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:suuralue1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:suuralue4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:suuralue1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:suuralue1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:suuralue4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tilastointialueet:suuralue4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tieliikenne:tieliikenne_2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tieliikenne:tieliikenne_2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: tieliikenne:tieliikenne_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: ttlaitokset:toimipaikat - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:avi_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:ely_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:kunta_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:maakunta_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:seutukunta_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:suuralue_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:avi_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:ely_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:kunta_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:maakunta_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:seutukunta_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:suuralue_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:avi_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:ely_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:kunta_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:maakunta_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:seutukunta_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoalue:suuralue_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2005_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2005_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2010_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2010_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2011_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2011_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2012_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2012_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2013_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2013_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2005_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2010_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2011_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2012_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: vaestoruutu:vaki2013_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.0.0 --  Feature: laearuutu:laea_1km_2011 - default EPSG:3035 
    ----------------------------------------------------------------------------- >>>> 
    WFS GetFeature test  (Http request, Transport, Geotools ) <<<< GETFEATURE ---------- 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:avi1000k_2013 fid="avi1000k_2013.fid--6598fd40_1471aa4a802_-4a51"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:avi1000k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:avi4500k_2013 fid="avi4500k_2013.fid--6598fd40_1471aa4a802_-4a44"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:avi4500k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:avi1000k fid="avi1000k.fid--6598fd40_1471aa4a802_-4a3d"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointialueet:vuos 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:avi1000k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:avi1000k_2014 fid="avi1000k_2014.fid--6598fd40_1471aa4a802_-4a36"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:avi1000k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:avi4500k fid="avi4500k.fid--6598fd40_1471aa4a802_-4a2f"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointialueet:vuos 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:avi4500k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:avi4500k_2014 fid="avi4500k_2014.fid--6598fd40_1471aa4a802_-4a28"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:avi4500k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:ely1000k_2013 fid="ely1000k_2013.fid--6598fd40_1471aa4a802_-4a21"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:ely1000k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:ely4500k_2013 fid="ely4500k_2013.fid--6598fd40_1471aa4a802_-4a17"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:ely4500k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:ely1000k fid="ely1000k.fid--6598fd40_1471aa4a802_-4a0d"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointialueet:vuos 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:ely1000k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:ely1000k_2014 fid="ely1000k_2014.fid--6598fd40_1471aa4a802_-4a03"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:ely1000k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:ely4500k fid="ely4500k.fid--6598fd40_1471aa4a802_-49f9"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointialueet:vuos 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:ely4500k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:ely4500k_2014 fid="ely4500k_2014.fid--6598fd40_1471aa4a802_-49ef"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:ely4500k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:kunta1000k_2013 fid="kunta1000k_2013.fid--6598fd40_1471aa4a802_-49e5"><tilastointialueet:gid>1</tilastointialueet:gid><tilastoi 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:kunta1000k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:kunta4500k_2013 fid="kunta4500k_2013.fid--6598fd40_1471aa4a802_-49db"><tilastointialueet:gid>1</tilastointialueet:gid><tilastoi 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:kunta4500k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:kunta1000k_2014 fid="kunta1000k_2014.fid--6598fd40_1471aa4a802_-49d1"><tilastointialueet:gid>1</tilastointialueet:gid><tilastoi 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:kunta1000k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:kunta1000k fid="kunta1000k.fid--6598fd40_1471aa4a802_-49c7"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointialueet: 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:kunta1000k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:kunta4500k fid="kunta4500k.fid--6598fd40_1471aa4a802_-49bd"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointialueet: 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:kunta4500k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:kunta4500k_2014 fid="kunta4500k_2014.fid--6598fd40_1471aa4a802_-49b3"><tilastointialueet:gid>1</tilastointialueet:gid><tilastoi 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:kunta4500k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:maakunta1000k_2013 fid="maakunta1000k_2013.fid--6598fd40_1471aa4a802_-49a9"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:maakunta1000k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:maakunta4500k_2013 fid="maakunta4500k_2013.fid--6598fd40_1471aa4a802_-499f"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:maakunta4500k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:maakunta1000k fid="maakunta1000k.fid--6598fd40_1471aa4a802_-4995"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:maakunta1000k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:maakunta1000k_2014 fid="maakunta1000k_2014.fid--6598fd40_1471aa4a802_-498b"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:maakunta1000k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:maakunta4500k fid="maakunta4500k.fid--6598fd40_1471aa4a802_-4981"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:maakunta4500k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:maakunta4500k_2014 fid="maakunta4500k_2014.fid--6598fd40_1471aa4a802_-4977"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:maakunta4500k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><oppilaitokset:oppilaitokset fid="oppilaitokset.1"><oppilaitokset:til_vuosi>2013</oppilaitokset:til_vuosi><oppilaitokset:onimi>Aapiskujan koulu</o 
    Transport WFSParser  OK - version 1.0.0 --  Feature: oppilaitokset:oppilaitokset EPSG:3857 
     Features corner: 1st: 211008.51 2nd: 6673735.34 
    GeoTools BBOX GetFeature parser OK- version 1.0.0 --  Feature: oppilaitokset:oppilaitokset EPSG:3857 
     Features found: 147 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:seutukunta1000k_2013 fid="seutukunta1000k_2013.fid--6598fd40_1471aa4a802_-496d"><tilastointialueet:gid>1</tilastointialueet:gid 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta1000k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:seutukunta4500k_2013 fid="seutukunta4500k_2013.fid--6598fd40_1471aa4a802_-4963"><tilastointialueet:gid>1</tilastointialueet:gid 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta4500k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:seutukunta1000k_2014 fid="seutukunta1000k_2014.fid--6598fd40_1471aa4a802_-4959"><tilastointialueet:gid>1</tilastointialueet:gid 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta1000k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:seutukunta1000k fid="seutukunta1000k.fid--6598fd40_1471aa4a802_-494f"><tilastointialueet:gid>1</tilastointialueet:gid><tilastoi 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta1000k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:seutukunta4500k fid="seutukunta4500k.fid--6598fd40_1471aa4a802_-4945"><tilastointialueet:gid>1</tilastointialueet:gid><tilastoi 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta4500k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:seutukunta4500k_2014 fid="seutukunta4500k_2014.fid--6598fd40_1471aa4a802_-493b"><tilastointialueet:gid>1</tilastointialueet:gid 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:seutukunta4500k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:suuralue1000k_2013 fid="suuralue1000k_2013.fid--6598fd40_1471aa4a802_-4931"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:suuralue1000k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:suuralue4500k_2013 fid="suuralue4500k_2013.fid--6598fd40_1471aa4a802_-492c"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:suuralue4500k_2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:suuralue1000k fid="suuralue1000k.fid--6598fd40_1471aa4a802_-4927"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:suuralue1000k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:suuralue1000k_2014 fid="suuralue1000k_2014.fid--6598fd40_1471aa4a802_-4922"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:suuralue1000k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:suuralue4500k_2014 fid="suuralue4500k_2014.fid--6598fd40_1471aa4a802_-491d"><tilastointialueet:gid>1</tilastointialueet:gid><ti 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:suuralue4500k_2014 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tilastointialueet:suuralue4500k fid="suuralue4500k.fid--6598fd40_1471aa4a802_-4918"><tilastointialueet:gid>1</tilastointialueet:gid><tilastointia 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tilastointialueet:suuralue4500k EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tieliikenne:tieliikenne_2011 fid="tieliikenne_2011.1"><tieliikenne:vvonn>2011</tieliikenne:vvonn><tieliikenne:kkonn>1</tieliikenne:kkonn><tieliik 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tieliikenne:tieliikenne_2011 EPSG:3857 
     Features corner: 1st: 382220.5484 2nd: 6672014.7878 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tieliikenne:tieliikenne_2012 fid="tieliikenne_2012.1"><tieliikenne:vvonn>2012</tieliikenne:vvonn><tieliikenne:kkonn>8</tieliikenne:kkonn><tieliik 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tieliikenne:tieliikenne_2012 EPSG:3857 
     Features corner: 1st: 89186.1504 2nd: 6683214.7876 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><tieliikenne:tieliikenne_2013 fid="tieliikenne_2013.1"><tieliikenne:vvonn>2013</tieliikenne:vvonn><tieliikenne:kkonn>12</tieliikenne:kkonn><tielii 
    Transport WFSParser  OK - version 1.0.0 --  Feature: tieliikenne:tieliikenne_2013 EPSG:3857 
     Features corner: 1st: 206238.4092 2nd: 6673295.0205 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><ttlaitokset:toimipaikat fid="toimipaikat.1"><ttlaitokset:til_vuosi>2012</ttlaitokset:til_vuosi><ttlaitokset:euref_x>291399.0</ttlaitokset:euref_x 
    Transport WFSParser  OK - version 1.0.0 --  Feature: ttlaitokset:toimipaikat EPSG:3857 
     Features corner: 1st: 289432.0 2nd: 7010693.0 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:avi_vaki2011 fid="avi_vaki2011.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:avi>1</vaestoalue:avi><vaestoalue:nimi>Etelä-Su 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:avi_vaki2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:ely_vaki2011 fid="ely_vaki2011.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:ely>01</vaestoalue:ely><vaestoalue:nimi>Uudenma 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:ely_vaki2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:kunta_vaki2011 fid="kunta_vaki2011.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:kunta>005</vaestoalue:kunta><vaestoalue:nim 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:kunta_vaki2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:maakunta_vaki2011 fid="maakunta_vaki2011.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:maakunta>01</vaestoalue:maakunta><vae 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:maakunta_vaki2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:seutukunta_vaki2011 fid="seutukunta_vaki2011.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:seutukunta>011</vaestoalue:seutuk 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:seutukunta_vaki2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:suuralue_vaki2011 fid="suuralue_vaki2011.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:suuralue>1</vaestoalue:suuralue><vaes 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:suuralue_vaki2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:avi_vaki2012 fid="avi_vaki2012.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:avi>1</vaestoalue:avi><vaestoalue:nimi>Etelä-Su 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:avi_vaki2012 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:ely_vaki2012 fid="ely_vaki2012.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:ely>01</vaestoalue:ely><vaestoalue:nimi>Uudenma 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:ely_vaki2012 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:kunta_vaki2012 fid="kunta_vaki2012.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:kunta>005</vaestoalue:kunta><vaestoalue:nim 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:kunta_vaki2012 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:maakunta_vaki2012 fid="maakunta_vaki2012.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:maakunta>01</vaestoalue:maakunta><vae 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:maakunta_vaki2012 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:seutukunta_vaki2012 fid="seutukunta_vaki2012.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:seutukunta>011</vaestoalue:seutuk 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:seutukunta_vaki2012 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:suuralue_vaki2012 fid="suuralue_vaki2012.1"><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:suuralue>1</vaestoalue:suuralue><vaes 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:suuralue_vaki2012 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:avi_vaki2013 fid="avi_vaki2013.1"><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:avi>1</vaestoalue:avi><vaestoalue:nimi>Etelä-Su 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:avi_vaki2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:ely_vaki2013 fid="ely_vaki2013.1"><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:ely>01</vaestoalue:ely><vaestoalue:nimi>Uudenma 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:ely_vaki2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:kunta_vaki2013 fid="kunta_vaki2013.1"><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:kunta>005</vaestoalue:kunta><vaestoalue:nim 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:kunta_vaki2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:maakunta_vaki2013 fid="maakunta_vaki2013.1"><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:maakunta>01</vaestoalue:maakunta><vae 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:maakunta_vaki2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:seutukunta_vaki2013 fid="seutukunta_vaki2013.1"><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:seutukunta>011</vaestoalue:seutuk 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:seutukunta_vaki2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoalue:suuralue_vaki2013 fid="suuralue_vaki2013.1"><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:suuralue>1</vaestoalue:suuralue><vaes 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoalue:suuralue_vaki2013 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2005_1km fid="vaki2005_1km.86337"><vaestoruutu:kunta>071</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7112E0418</vaestoruutu:grd_id 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2005_1km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2005_1km_kp fid="vaki2005_1km_kp.fid--6598fd40_1471aa4a802_-4913"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_id>1 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2005_1km_kp EPSG:3857 
     Features corner: 1st: 245500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2010_1km fid="vaki2010_1km.96065"><vaestoruutu:kunta>851</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7325E0380</vaestoruutu:grd_id 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2010_1km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2010_1km_kp fid="vaki2010_1km_kp.fid--6598fd40_1471aa4a802_-48a5"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_id>1 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2010_1km_kp EPSG:3857 
     Features corner: 1st: 245500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2011_1km fid="vaki2011_1km.98497"><vaestoruutu:kunta>854</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7403E0384</vaestoruutu:grd_id 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2011_1km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2011_1km_kp fid="vaki2011_1km_kp.fid--6598fd40_1471aa4a802_-4837"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_id>1 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2011_1km_kp EPSG:3857 
     Features corner: 1st: 245500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2012_1km fid="vaki2012_1km.97281"><vaestoruutu:kunta>698</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7370E0457</vaestoruutu:grd_id 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2012_1km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2012_1km_kp fid="vaki2012_1km_kp.fid--6598fd40_1471aa4a802_-47c9"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_id>1 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2012_1km_kp EPSG:3857 
     Features corner: 1st: 245500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2013_1km fid="vaki2013_1km.70529"><vaestoruutu:kunta>743</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN6992E0272</vaestoruutu:grd_id 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2013_1km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2013_1km_kp fid="vaki2013_1km_kp.fid--6598fd40_1471aa4a802_-475f"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_id>1 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2013_1km_kp EPSG:3857 
     Features corner: 1st: 245500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser FAILED or IS EMPTY - version 1.0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2005_5km fid="vaki2005_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id><va 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2005_5km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2010_5km fid="vaki2010_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id><va 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2010_5km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2011_5km fid="vaki2011_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id><va 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2011_5km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2012_5km fid="vaki2012_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id><va 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2012_5km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><vaestoruutu:vaki2013_5km fid="vaki2013_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id><va 
    Transport WFSParser  OK - version 1.0.0 --  Feature: vaestoruutu:vaki2013_5km EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    WFS http GET GetFeature request OK - version 1.0.0 - response... featureMember><laearuutu:laea_1km_2011 fid="laea_1km_2011.90881"><laearuutu:grd_id>1kmN4401E5286</laearuutu:grd_id><laearuutu:year>2011</laearuutu:year><laearuu 
    Transport WFSParser  OK - version 1.0.0 --  Feature: laearuutu:laea_1km_2011 EPSG:3857 
     Features corner: 1st: 0.0 2nd: 0.0 
    ----------------------------------------------------------------------------- >>>> 
    WFS test - version: 1.0.0:::: END >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  
    WFS test - version: 1.1.0:::: START <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  
    WFS version test  (GetCapabilities http request) <<<< HTTP GETCAPABILITIES ------- 
    WFS Http GetCapabilities request OK - version 1.1.0 
    ----------------------------------------------------------------------------- >>>> 
    WFS version test  (GetCapabilities GeoTools ) <<<< GEOTOOLS GETCAPABILITIES AND DESCRIBEFEATURE ---------- 
    WFS GT GetCapabilities request OK - version 1.1.0 
    ----------------------------------------------------------------------------- >>>> 
    WFS DescribeFeature test  (Http request ) <<<< HTTP DESCRIBEFEATURE ---------- 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: laearuutu:laea_1km_2011 - default EPSG:3035 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: oppilaitokset:oppilaitokset - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tieliikenne:tieliikenne_2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tieliikenne:tieliikenne_2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tieliikenne:tieliikenne_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:avi1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:avi1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:avi1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:avi4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:avi4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:avi4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:ely1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:ely1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:ely1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:ely4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:ely4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:ely4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:kunta1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:kunta1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:kunta1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:kunta4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:kunta4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:kunta4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:maakunta1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:maakunta1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:maakunta1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:maakunta4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:maakunta4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:maakunta4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:suuralue1000k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:suuralue1000k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:suuralue1000k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:suuralue4500k - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:suuralue4500k_2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: tilastointialueet:suuralue4500k_2014 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: ttlaitokset:toimipaikat - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:avi_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:avi_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:avi_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:ely_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:ely_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:ely_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:kunta_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:kunta_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:kunta_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2011 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2012 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2013 - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2005_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2005_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2005_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2010_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2010_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2010_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2011_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2011_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2011_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2012_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2012_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2012_5km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2013_1km - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2013_1km_kp - default EPSG:3067 
    WFS DescribeFeatureType request OK - version 1.1.0 --  Feature: vaestoruutu:vaki2013_5km - default EPSG:3067 
    ----------------------------------------------------------------------------- >>>> 
    WFS GetFeature test  (Http request, Transport, Geotools ) <<<< GETFEATURE ---------- 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><laearuutu:laea_1km_2011 gml:id="laea_1km_2011.90881"><laearuutu:grd_id>1kmN4401E5286</laearuutu:grd_id><laearuutu:year>2011</laearuutu:year><laea 
    Transport WFSParser  OK - version 1.1.0 --  Feature: laearuutu:laea_1km_2011 EPSG:3857 
     Features corner: 1st: 4401000.0 2nd: 5286000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: laearuutu:laea_1km_2011 EPSG:3857 
     Features found: 50378 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><oppilaitokset:oppilaitokset gml:id="oppilaitokset.1"><oppilaitokset:til_vuosi>2013</oppilaitokset:til_vuosi><oppilaitokset:onimi>Aapiskujan koulu 
    Transport WFSParser  OK - version 1.1.0 --  Feature: oppilaitokset:oppilaitokset EPSG:3857 
     Features corner: 1st: 340457.01 2nd: 7007581.93 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: oppilaitokset:oppilaitokset EPSG:3857 
     Features found: 1457 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tieliikenne:tieliikenne_2011 gml:id="tieliikenne_2011.1"><tieliikenne:vvonn>2011</tieliikenne:vvonn><tieliikenne:kkonn>1</tieliikenne:kkonn><tiel 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tieliikenne:tieliikenne_2011 EPSG:3857 
     Features corner: 1st: 387958.5534 2nd: 6676726.7617 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tieliikenne:tieliikenne_2011 EPSG:3857 
     Features found: 51 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tieliikenne:tieliikenne_2012 gml:id="tieliikenne_2012.1"><tieliikenne:vvonn>2012</tieliikenne:vvonn><tieliikenne:kkonn>8</tieliikenne:kkonn><tiel 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tieliikenne:tieliikenne_2012 EPSG:3857 
     Features corner: 1st: 89186.1504 2nd: 6698967.0132 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tieliikenne:tieliikenne_2012 EPSG:3857 
     Features found: 1212 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tieliikenne:tieliikenne_2013 gml:id="tieliikenne_2013.1"><tieliikenne:vvonn>2013</tieliikenne:vvonn><tieliikenne:kkonn>12</tieliikenne:kkonn><tie 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tieliikenne:tieliikenne_2013 EPSG:3857 
     Features corner: 1st: 470984.2475 2nd: 6730883.807 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tieliikenne:tieliikenne_2013 EPSG:3857 
     Features found: 99 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:avi1000k gml:id="avi1000k.fid--6598fd40_1471aa4a802_-46f5"><gml:name>Southern Finland AVI</gml:name><tilastointialueet:gid>1</t 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:avi1000k EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:avi1000k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:avi1000k_2013 gml:id="avi1000k_2013.fid--6598fd40_1471aa4a802_-46ed"><gml:name>Southern Finland AVI</gml:name><tilastointialuee 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:avi1000k_2013 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:avi1000k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:avi1000k_2014 gml:id="avi1000k_2014.fid--6598fd40_1471aa4a802_-46e5"><gml:name>Southern Finland AVI</gml:name><tilastointialuee 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:avi1000k_2014 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:avi1000k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:avi4500k gml:id="avi4500k.fid--6598fd40_1471aa4a802_-46d9"><gml:name>Southern Finland AVI</gml:name><tilastointialueet:gid>1</t 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:avi4500k EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:avi4500k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:avi4500k_2013 gml:id="avi4500k_2013.fid--6598fd40_1471aa4a802_-46d1"><gml:name>Southern Finland AVI</gml:name><tilastointialuee 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:avi4500k_2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:avi4500k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:avi4500k_2014 gml:id="avi4500k_2014.fid--6598fd40_1471aa4a802_-46c9"><gml:name>Southern Finland AVI</gml:name><tilastointialuee 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:avi4500k_2014 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:avi4500k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:ely1000k gml:id="ely1000k.fid--6598fd40_1471aa4a802_-46c1"><gml:name>Uusimaa ELY Centre</gml:name><tilastointialueet:gid>1</til 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:ely1000k EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:ely1000k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:ely1000k_2013 gml:id="ely1000k_2013.fid--6598fd40_1471aa4a802_-46b6"><gml:name>Uusimaa ELY Centre</gml:name><tilastointialueet: 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:ely1000k_2013 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:ely1000k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:ely1000k_2014 gml:id="ely1000k_2014.fid--6598fd40_1471aa4a802_-46ab"><gml:name>Uusimaa ELY Centre</gml:name><tilastointialueet: 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:ely1000k_2014 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:ely1000k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:ely4500k gml:id="ely4500k.fid--6598fd40_1471aa4a802_-469c"><gml:name>Uusimaa ELY Centre</gml:name><tilastointialueet:gid>1</til 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:ely4500k EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:ely4500k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:ely4500k_2013 gml:id="ely4500k_2013.fid--6598fd40_1471aa4a802_-4691"><gml:name>Uusimaa ELY Centre</gml:name><tilastointialueet: 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:ely4500k_2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:ely4500k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:ely4500k_2014 gml:id="ely4500k_2014.fid--6598fd40_1471aa4a802_-4686"><gml:name>Uusimaa ELY Centre</gml:name><tilastointialueet: 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:ely4500k_2014 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:ely4500k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:kunta1000k gml:id="kunta1000k.fid--6598fd40_1471aa4a802_-467b"><gml:name>Alajärvi</gml:name><tilastointialueet:gid>1</tilastoin 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:kunta1000k EPSG:3857 
     Features corner: 1st: 320404.4517000001 2nd: 6959056.437600002 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:kunta1000k EPSG:3857 
     Features found: 130 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:kunta1000k_2013 gml:id="kunta1000k_2013.fid--6598fd40_1471aa4a802_-45ef"><gml:name>Alajärvi</gml:name><tilastointialueet:gid>1< 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:kunta1000k_2013 EPSG:3857 
     Features corner: 1st: 320404.4517000001 2nd: 6959056.437600002 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:kunta1000k_2013 EPSG:3857 
     Features found: 130 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:kunta1000k_2014 gml:id="kunta1000k_2014.fid--6598fd40_1471aa4a802_-4563"><gml:name>Alajärvi</gml:name><tilastointialueet:gid>1< 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:kunta1000k_2014 EPSG:3857 
     Features corner: 1st: 320404.4517000001 2nd: 6959056.437600002 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:kunta1000k_2014 EPSG:3857 
     Features found: 130 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:kunta4500k gml:id="kunta4500k.fid--6598fd40_1471aa4a802_-44d7"><gml:name>Alajärvi</gml:name><tilastointialueet:gid>1</tilastoin 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:kunta4500k EPSG:3857 
     Features corner: 1st: 321987.0717000002 2nd: 6959704.550799999 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:kunta4500k EPSG:3857 
     Features found: 129 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:kunta4500k_2013 gml:id="kunta4500k_2013.fid--6598fd40_1471aa4a802_-444c"><gml:name>Alajärvi</gml:name><tilastointialueet:gid>1< 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:kunta4500k_2013 EPSG:3857 
     Features corner: 1st: 321987.0717000002 2nd: 6959704.550799999 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:kunta4500k_2013 EPSG:3857 
     Features found: 129 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:kunta4500k_2014 gml:id="kunta4500k_2014.fid--6598fd40_1471aa4a802_-43c1"><gml:name>Alajärvi</gml:name><tilastointialueet:gid>1< 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:kunta4500k_2014 EPSG:3857 
     Features corner: 1st: 321987.0717000002 2nd: 6959704.550799999 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:kunta4500k_2014 EPSG:3857 
     Features found: 129 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:maakunta1000k gml:id="maakunta1000k.fid--6598fd40_1471aa4a802_-4336"><gml:name>Uusimaa</gml:name><tilastointialueet:gid>1</tila 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:maakunta1000k EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:maakunta1000k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:maakunta1000k_2013 gml:id="maakunta1000k_2013.fid--6598fd40_1471aa4a802_-432b"><gml:name>Uusimaa</gml:name><tilastointialueet:g 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:maakunta1000k_2013 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:maakunta1000k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:maakunta1000k_2014 gml:id="maakunta1000k_2014.fid--6598fd40_1471aa4a802_-4320"><gml:name>Uusimaa</gml:name><tilastointialueet:g 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:maakunta1000k_2014 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:maakunta1000k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:maakunta4500k gml:id="maakunta4500k.fid--6598fd40_1471aa4a802_-4315"><gml:name>Uusimaa</gml:name><tilastointialueet:gid>1</tila 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:maakunta4500k EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:maakunta4500k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:maakunta4500k_2013 gml:id="maakunta4500k_2013.fid--6598fd40_1471aa4a802_-430a"><gml:name>Uusimaa</gml:name><tilastointialueet:g 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:maakunta4500k_2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:maakunta4500k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:maakunta4500k_2014 gml:id="maakunta4500k_2014.fid--6598fd40_1471aa4a802_-42ff"><gml:name>Uusimaa</gml:name><tilastointialueet:g 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:maakunta4500k_2014 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:maakunta4500k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:seutukunta1000k gml:id="seutukunta1000k.fid--6598fd40_1471aa4a802_-42f4"><gml:name>Helsinki</gml:name><tilastointialueet:gid>1< 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6644750.0425 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:seutukunta1000k_2013 gml:id="seutukunta1000k_2013.fid--6598fd40_1471aa4a802_-42e8"><gml:name>Helsinki</gml:name><tilastointialu 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k_2013 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6644750.0425 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k_2013 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:seutukunta1000k_2014 gml:id="seutukunta1000k_2014.fid--6598fd40_1471aa4a802_-42dc"><gml:name>Helsinki</gml:name><tilastointialu 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k_2014 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6644750.0425 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:seutukunta1000k_2014 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:seutukunta4500k gml:id="seutukunta4500k.fid--6598fd40_1471aa4a802_-42d0"><gml:name>Helsinki</gml:name><tilastointialueet:gid>1< 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6650996.341700001 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:seutukunta4500k_2013 gml:id="seutukunta4500k_2013.fid--6598fd40_1471aa4a802_-42c4"><gml:name>Helsinki</gml:name><tilastointialu 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k_2013 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6650996.341700001 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k_2013 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:seutukunta4500k_2014 gml:id="seutukunta4500k_2014.fid--6598fd40_1471aa4a802_-42b8"><gml:name>Helsinki</gml:name><tilastointialu 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k_2014 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6650996.341700001 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:seutukunta4500k_2014 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:suuralue1000k gml:id="suuralue1000k.fid--6598fd40_1471aa4a802_-42ac"><gml:name>Helsinki-Uusimaa</gml:name><tilastointialueet:gi 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:suuralue1000k EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:suuralue1000k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:suuralue1000k_2013 gml:id="suuralue1000k_2013.fid--6598fd40_1471aa4a802_-42a6"><gml:name>Helsinki-Uusimaa</gml:name><tilastoint 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:suuralue1000k_2013 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:suuralue1000k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:suuralue1000k_2014 gml:id="suuralue1000k_2014.fid--6598fd40_1471aa4a802_-42a0"><gml:name>Helsinki-Uusimaa</gml:name><tilastoint 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:suuralue1000k_2014 EPSG:3857 
     Features corner: 1st: 265642.5415000003 2nd: 6632288.0901999995 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:suuralue1000k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:suuralue4500k gml:id="suuralue4500k.fid--6598fd40_1471aa4a802_-429a"><gml:name>Helsinki-Uusimaa</gml:name><tilastointialueet:gi 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:suuralue4500k EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:suuralue4500k EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:suuralue4500k_2013 gml:id="suuralue4500k_2013.fid--6598fd40_1471aa4a802_-4294"><gml:name>Helsinki-Uusimaa</gml:name><tilastoint 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:suuralue4500k_2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:suuralue4500k_2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><tilastointialueet:suuralue4500k_2014 gml:id="suuralue4500k_2014.fid--6598fd40_1471aa4a802_-428e"><gml:name>Helsinki-Uusimaa</gml:name><tilastoint 
    Transport WFSParser  OK - version 1.1.0 --  Feature: tilastointialueet:suuralue4500k_2014 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: tilastointialueet:suuralue4500k_2014 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><ttlaitokset:toimipaikat gml:id="toimipaikat.1"><ttlaitokset:til_vuosi>2012</ttlaitokset:til_vuosi><ttlaitokset:euref_x>291399.0</ttlaitokset:eure 
    Transport WFSParser  OK - version 1.1.0 --  Feature: ttlaitokset:toimipaikat EPSG:3857 
     Features corner: 1st: 291398.9062999999 2nd: 7010693.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: ttlaitokset:toimipaikat EPSG:3857 
     Features found: 5723 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:avi_vaki2011 gml:id="avi_vaki2011.1"><gml:name>Southern Finland AVI</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:avi 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:avi_vaki2011 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:avi_vaki2011 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:avi_vaki2012 gml:id="avi_vaki2012.1"><gml:name>Southern Finland AVI</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:avi 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:avi_vaki2012 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:avi_vaki2012 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:avi_vaki2013 gml:id="avi_vaki2013.1"><gml:name>Southern Finland AVI</gml:name><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:avi 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:avi_vaki2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:avi_vaki2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:ely_vaki2011 gml:id="ely_vaki2011.1"><gml:name>Uusimaa ELY Centre</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:ely>0 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:ely_vaki2011 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:ely_vaki2011 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:ely_vaki2012 gml:id="ely_vaki2012.1"><gml:name>Uusimaa ELY Centre</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:ely>0 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:ely_vaki2012 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:ely_vaki2012 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:ely_vaki2013 gml:id="ely_vaki2013.1"><gml:name>Uusimaa ELY Centre</gml:name><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:ely>0 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:ely_vaki2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:ely_vaki2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:kunta_vaki2011 gml:id="kunta_vaki2011.1"><gml:name>Alajärvi</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:kunta>005</ 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:kunta_vaki2011 EPSG:3857 
     Features corner: 1st: 321987.0717000002 2nd: 6959704.550799999 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:kunta_vaki2011 EPSG:3857 
     Features found: 129 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:kunta_vaki2012 gml:id="kunta_vaki2012.1"><gml:name>Alajärvi</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:kunta>005</ 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:kunta_vaki2012 EPSG:3857 
     Features corner: 1st: 321987.0717000002 2nd: 6959704.550799999 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:kunta_vaki2012 EPSG:3857 
     Features found: 129 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:kunta_vaki2013 gml:id="kunta_vaki2013.1"><gml:name>Alajärvi</gml:name><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:kunta>005</ 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:kunta_vaki2013 EPSG:3857 
     Features corner: 1st: 321987.0717000002 2nd: 6959704.550799999 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:kunta_vaki2013 EPSG:3857 
     Features found: 129 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:maakunta_vaki2011 gml:id="maakunta_vaki2011.1"><gml:name>Uusimaa</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:maakun 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2011 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2011 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:maakunta_vaki2012 gml:id="maakunta_vaki2012.1"><gml:name>Uusimaa</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:maakun 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2012 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2012 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:maakunta_vaki2013 gml:id="maakunta_vaki2013.1"><gml:name>Uusimaa</gml:name><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:maakun 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:maakunta_vaki2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:seutukunta_vaki2011 gml:id="seutukunta_vaki2011.1"><gml:name>Helsinki</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:s 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2011 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6650996.341700001 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2011 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:seutukunta_vaki2012 gml:id="seutukunta_vaki2012.1"><gml:name>Helsinki</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoalue:s 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2012 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6650996.341700001 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2012 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:seutukunta_vaki2013 gml:id="seutukunta_vaki2013.1"><gml:name>Helsinki</gml:name><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoalue:s 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2013 EPSG:3857 
     Features corner: 1st: 311139.0367999999 2nd: 6650996.341700001 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:seutukunta_vaki2013 EPSG:3857 
     Features found: 2 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:suuralue_vaki2011 gml:id="suuralue_vaki2011.1"><gml:name>Helsinki-Uusimaa</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoal 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2011 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2011 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:suuralue_vaki2012 gml:id="suuralue_vaki2012.1"><gml:name>Helsinki-Uusimaa</gml:name><vaestoalue:vuosi>2013</vaestoalue:vuosi><vaestoal 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2012 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2012 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoalue:suuralue_vaki2013 gml:id="suuralue_vaki2013.1"><gml:name>Helsinki-Uusimaa</gml:name><vaestoalue:vuosi>2014</vaestoalue:vuosi><vaestoal 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2013 EPSG:3857 
     Features corner: 1st: 268733.57189999986 2nd: 6637031.5271000005 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoalue:suuralue_vaki2013 EPSG:3857 
     Features found: 1 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2005_1km gml:id="vaki2005_1km.86337"><vaestoruutu:kunta>071</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7112E0418</vaestoruutu:grd 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2005_1km EPSG:3857 
     Features corner: 1st: 418000.0 2nd: 7112000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2005_1km EPSG:3857 
     Features found: 40771 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2005_1km_kp gml:id="vaki2005_1km_kp.fid--6598fd40_1471aa4a802_-4288"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_i 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2005_1km_kp EPSG:3857 
     Features corner: 1st: 280500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2005_1km_kp EPSG:3857 
     Features found: 21 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2005_5km gml:id="vaki2005_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id> 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2005_5km EPSG:3857 
     Features corner: 1st: 85000.0 2nd: 6690000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2005_5km EPSG:3857 
     Features found: 490 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2010_1km gml:id="vaki2010_1km.96065"><vaestoruutu:kunta>851</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7325E0380</vaestoruutu:grd 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2010_1km EPSG:3857 
     Features corner: 1st: 380000.0 2nd: 7325000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2010_1km EPSG:3857 
     Features found: 57482 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2010_1km_kp gml:id="vaki2010_1km_kp.fid--6598fd40_1471aa4a802_-4264"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_i 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2010_1km_kp EPSG:3857 
     Features corner: 1st: 280500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2010_1km_kp EPSG:3857 
     Features found: 21 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2010_5km gml:id="vaki2010_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id> 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2010_5km EPSG:3857 
     Features corner: 1st: 85000.0 2nd: 6690000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2010_5km EPSG:3857 
     Features found: 491 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2011_1km gml:id="vaki2011_1km.98497"><vaestoruutu:kunta>854</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7403E0384</vaestoruutu:grd 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2011_1km EPSG:3857 
     Features corner: 1st: 384000.0 2nd: 7403000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2011_1km EPSG:3857 
     Features found: 58699 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2011_1km_kp gml:id="vaki2011_1km_kp.fid--6598fd40_1471aa4a802_-4245"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_i 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2011_1km_kp EPSG:3857 
     Features corner: 1st: 280500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2011_1km_kp EPSG:3857 
     Features found: 21 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2011_5km gml:id="vaki2011_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id> 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2011_5km EPSG:3857 
     Features corner: 1st: 85000.0 2nd: 6690000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2011_5km EPSG:3857 
     Features found: 488 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2012_1km gml:id="vaki2012_1km.97281"><vaestoruutu:kunta>698</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN7370E0457</vaestoruutu:grd 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2012_1km EPSG:3857 
     Features corner: 1st: 457000.0 2nd: 7370000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2012_1km EPSG:3857 
     Features found: 39238 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2012_1km_kp gml:id="vaki2012_1km_kp.fid--6598fd40_1471aa4a802_-4226"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_i 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2012_1km_kp EPSG:3857 
     Features corner: 1st: 280500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2012_1km_kp EPSG:3857 
     Features found: 20 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2012_5km gml:id="vaki2012_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id> 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2012_5km EPSG:3857 
     Features corner: 1st: 85000.0 2nd: 6690000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2012_5km EPSG:3857 
     Features found: 486 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2013_1km gml:id="vaki2013_1km.70529"><vaestoruutu:kunta>743</vaestoruutu:kunta><vaestoruutu:grd_id>1kmN6992E0272</vaestoruutu:grd 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2013_1km EPSG:3857 
     Features corner: 1st: 272000.0 2nd: 6992000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2013_1km EPSG:3857 
     Features found: 57991 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2013_1km_kp gml:id="vaki2013_1km_kp.fid--6598fd40_1471aa4a802_-4208"><vaestoruutu:kunta>078</vaestoruutu:kunta><vaestoruutu:grd_i 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2013_1km_kp EPSG:3857 
     Features corner: 1st: 280500.0 2nd: 6640500.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2013_1km_kp EPSG:3857 
     Features found: 20 
    WFS http GET GetFeature request OK - version 1.1.0 - response... featureMember><vaestoruutu:vaki2013_5km gml:id="vaki2013_5km.1"><vaestoruutu:kunta>043</vaestoruutu:kunta><vaestoruutu:grd_id>5kmN1338E0017</vaestoruutu:grd_id> 
    Transport WFSParser  OK - version 1.1.0 --  Feature: vaestoruutu:vaki2013_5km EPSG:3857 
     Features corner: 1st: 85000.0 2nd: 6690000.0 
    GeoTools BBOX GetFeature parser OK- version 1.1.0 --  Feature: vaestoruutu:vaki2013_5km EPSG:3857 
     Features found: 489 
    ----------------------------------------------------------------------------- >>>> 
    WFS test - version: 1.1.0:::: END >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  
    WFS test - version: 2.0.0:::: START <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  
    WFS version test  (GetCapabilities http request) <<<< HTTP GETCAPABILITIES ------- 
    WFS Http GetCapabilities request OK - version 2.0.0 
    ----------------------------------------------------------------------------- >>>> 
    WFS version test  (GetCapabilities GeoTools ) <<<< GEOTOOLS GETCAPABILITIES AND DESCRIBEFEATURE ---------- 
    WFS GT GetCapabilities request FAILED - version: 2.0.0 
    Exception report: Expected {http://www.opengis.net/wfs}WFS_Capabilities but was http://www.opengis.net/wfs/2.0#WFS_Capabilities 
    ----------------------------------------------------------------------------- >>>> 
    WFS test - version: 2.0.0:::: END >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  
    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++ WFS Test END - Wed Jul 09 14:47:21 EEST 2014