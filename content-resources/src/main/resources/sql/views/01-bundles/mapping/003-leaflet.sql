INSERT INTO portti_bundle (name, startup)
  VALUES ('leaflet','{}');


UPDATE portti_bundle set startup = '{
    "title" : "Leaflet",
    "bundlename" : "leaflet",
    "bundleinstancename" : "leaflet",
    "metadata" : {
        "Import-Bundle" : {
            "leaflet" : {
                "bundlePath" : "/Oskari/packages/leaflet/bundle/"
            }
        }
     }
}' WHERE name = 'leaflet';
