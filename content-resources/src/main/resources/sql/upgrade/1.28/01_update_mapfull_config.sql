-- update proper config for views
UPDATE portti_view_bundle_seq SET
config='{
    "globalMapAjaxUrl": "[REPLACED BY HANDLER]",
    "imageLocation": "/Oskari/resources",
    "mapOptions" : {"srsName":"EPSG:3067","maxExtent":{"bottom":6291456,"left":-548576,"right":1548576,"top":8388608},"resolutions":[2048,1024,512,256,128,64,32,16,8,4,2,1,0.5,0.25]},
    "plugins" : [
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.MarkersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.ControlsPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.GetInfoPlugin",
         "config" : {
            "ignoredLayerTypes" : ["WFS","MYPLACES", "USERLAYER"],
            "infoBox": false
         }
       },
       { "id" : "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin",
         "config" : {
           "contextPath" : "/transport-0.0.1",
           "hostname" : "dev.paikkatietoikkuna.fi",
           "port" : 80,
           "lazy" : true,
           "disconnectTime" : 30000,
           "backoffIncrement": 1000,
           "maxBackoff": 60000,
           "maxNetworkDelay": 10000
         }
       },
       { "id" : "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" } ,
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" },
       { "id" : "Oskari.mapframework.bundle.mapmyplaces.plugin.MyPlacesLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapanalysis.plugin.AnalysisLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.RealtimePlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.VectorLayerPlugin" },
       {
            "id" : "Oskari.mapframework.bundle.mapmodule.plugin.BackgroundLayerSelectionPlugin",
            "config" : {
                "showAsDropdown" : false,
                "baseLayers" : ["base_2", "24", "base_35"]
            }
       },
       {
        "id": "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin",
        "config": {
            "mapUrlPrefix": {
                "en": "http://www.paikkatietoikkuna.fi/web/en/map-window?",
                "fi": "http://www.paikkatietoikkuna.fi/web/fi/kartta?",
                "sv": "http://www.paikkatietoikkuna.fi/web/sv/kartfonstret?"
            },
            "termsUrl": {
                "en": "http://www.paikkatietoikkuna.fi/web/en/terms-and-conditions",
                "fi": "http://www.paikkatietoikkuna.fi/web/fi/kayttoehdot",
                "sv": "http://www.paikkatietoikkuna.fi/web/sv/anvandningsvillkor"
            }
        } },
        {"id": "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin" },
        {"id": "Oskari.arcgis.bundle.maparcgis.plugin.ArcGisLayerPlugin" }
      ],
      "layers": [
      ]
}
'
WHERE view_id IN (SELECT id FROM portti_view WHERE application='full-map_guest' OR ( application='full-map' AND type='DEFAULT'))
AND bundle_id=(SELECT id FROM portti_bundle WHERE name='mapfull');

-- update proper config for bundle
UPDATE portti_bundle SET config = '{
       "globalMapAjaxUrl": "/web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=1&p_p_state=exclusive&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&_Portti2Map_WAR_portti2mapportlet_fi.mml.baseportlet.CMD=ajax.jsp&",
       "imageLocation": "/Oskari/resources",
       "plugins" : [
          { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" },
          { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" },
          { "id" : "Oskari.mapframework.mapmodule.MarkersPlugin" },
          { "id" : "Oskari.mapframework.mapmodule.ControlsPlugin" },
          { "id" : "Oskari.mapframework.mapmodule.GetInfoPlugin" },
          { "id" : "Oskari.mapframework.bundle.mapwfs.plugin.wfslayer.WfsLayerPlugin" },
          { "id" : "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" } ,
          { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin" },
          { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" },
          { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" },
          { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin" },
          { "id" : "Oskari.mapframework.mapmodule.VectorLayerPlugin" }
         ],
         "layers": [
            { "id": "base_35" }
         ],
         "user": {
              "firstName": "",
               "lastName": "",
               "loginName": "default@maanmittauslaitos.fi",
               "nickName": "10110"
         }
   }
' WHERE name = 'mapfull';