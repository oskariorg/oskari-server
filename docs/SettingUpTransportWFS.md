# Setting up transport wfs service (requires postgresql)

1) Install Redis: http://redis.io/ (tested with version 2.6.14 for linux and 2.4.5 for Windows (available in https://github.com/dmajkic/redis))

2) Redis configuration can be found in `oskari-server/servlet-map/src/main/resources/oskari.properties` but using default values nothing needs to be changed:

    # redis
    redis.hostname=localhost
    redis.port=6379
    redis.pool.size=100

3) Startup redis (redis-server). You may use redis-cli.exe for cleaning cache (flushall command)

4) Basic oskaridb wfs configuration 

** skip this, if my places configuration is already done ( cd  oskari-server/content-resources -> mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-myplaces-wfs2) **

* Start the server with `mvn clean install -f servlet-map-pom.xml -Doskari.dropdb=true -Doskari.setup=postgres-mapwfs2  -Pjetty-profile`

OR

   Just prepare database 
   * cd  oskari-server/content-resources
   * `mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-mapwfs2`

5) 

5.1 Go to `oskari-server/transport`
5.2 You may need to change config.properties under /transport (serviceURL=http://localhost:? , analysis.baselayer.id=?, myplaces.baselayer.id=?) OR add a 'transport-ext.properties' file with the properties you need on your own environment to override the defaults.
5.2 Compile the transport service with `mvn clean install` or `mvn clean install -DskipTests`
5.3 Start the jetty service with `mvn jetty:run`. or use other jetty installation (cd {jettyhome}/bin -> java -jar start.jar )

With default settings you now have:
* redis running on port 6379
* oskari-server running on port 2373 or any port
* transport wfs service running on port 2374 or any port
 

6) Open your browser with http://localhost:2373/?viewId=4 or http://localhost:2373/oskari-map?viewId=4  to see a new view loaded from the database.
    You should now see a new map layer of type WFS listed on the maplayers flyout. Add it to map, zoom close to Helsinki for example and you should see points appearing to the map from the wfs service

7) If WFS layer doen't work, you might need to configure view setup for wfslayer plugin  (mapwfs2-view.json or postgres-myplaces2-view.json)
   and reprepare database * `mvn clean install exec:java -Doskari.dropdb=true -Doskari.setup=postgres-mapwfs2`

   { "id" : "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin",
                        "config" : {
                            "contextPath" : "/transport", <--- /transport-0.0.1
                            "hostname" : "localhost",
                            "port" : "2374",              <--- 
                            "lazy" : true,
                            "disconnectTime" : 30000,
                            "backoffIncrement": 1000,
                            "maxBackoff": 60000,
                            "maxNetworkDelay": 10000
                        }
                    },

8) sample conf for transport (setup as transport-ext.properties in server classpath to override defaults)

    #serviceURL=http://localhost:2373

    #serviceURLSessionParam=jsessionid
    #serviceURLLiferayPath=?action_route=

    #workerCount=10
    #redisHostname=localhost
    #redisPort=6379

    # Supported locales, comma separated and default first
    #oskari.locales=fi_FI,sv_SE,en_EN

    # base WFS layer id s
    #analysis.baselayer.id=387
    #myplaces.baselayer.id=14

    #wfs.extension.analysis=fi.nls.oskari.wfs.extension.AnalysisFilter
    #wfs.extension.myplaces=fi.nls.oskari.wfs.extension.MyPlacesFilter

    serviceURL=http://localhost:8888

    serviceURLParam=/oskari-map
    serviceURLSessionParam=jsessionid
    serviceURLLiferayPath=?action_route=

    workerCount=10
    redisHostname=localhost
    redisPort=6379

    # Supported locales, comma separated and default first
    oskari.locales=fi_FI,sv_SE,en_EN

    # base WFS layer id s
    analysis.baselayer.id=387
    myplaces.baselayer.id=14

    wfs.extension.analysis=fi.nls.oskari.wfs.extension.AnalysisFilter
    wfs.extension.myplaces=fi.nls.oskari.wfs.extension.MyPlacesFilter


