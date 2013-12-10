-- update proper config for view
UPDATE portti_view_bundle_seq set config = '{
    "globalMapAjaxUrl": "[REPLACED BY HANDLER]",
    "imageLocation": "/Oskari/resources",
    "plugins" : [
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.MarkersPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.ControlsPlugin" },
       { "id" : "Oskari.mapframework.mapmodule.GetInfoPlugin",
         "config" : { 
            "ignoredLayerTypes" : ["WFS"], 
            "infoBox": false 
         }
       },
       { "id" : "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin", 
         "config" : { 
           "contextPath" : "/transport-0.0.1", 
           "hostname" : "demo.paikkatietoikkuna.fi", 
           "port" : "80",
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
       { "id" : "Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin" },
       { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin" },
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
        } }
      ],
      "layers": [
      ]
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name = 'mapfull') 
    AND view_id=[VIEW_ID];