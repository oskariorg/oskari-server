
UPDATE portti_view_bundle_seq SET config = '{
  "queryUrl" : "[REPLACED BY HANDLER]",
  "featureNS" : "http://www.oskari.org",
  "layerDefaults" : {
    "wmsName" : "oskari:my_places_categories"
  },
  "wmsUrl" : "/karttatiili/myplaces?myCat="
}' WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name='myplaces2')

UPDATE portti_bundle SET config = '{
  "queryUrl" : "[REPLACED BY HANDLER]",
  "featureNS" : "http://www.oskari.org",
  "layerDefaults" : {
    "wmsName" : "oskari:my_places_categories"
  },
  "wmsUrl" : "/karttatiili/myplaces?myCat="
}' WHERE name = 'myplaces2'