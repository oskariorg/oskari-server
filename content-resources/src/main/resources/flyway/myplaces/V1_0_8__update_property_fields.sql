update portti_wfs_layer
set
selected_feature_params =
'{
  "default": ["name", "place_desc","link", "image_url", "attention_text"],
  "fi": ["name", "place_desc", "link", "image_url", "attention_text"],
  "sv": ["name", "place_desc", "link", "image_url", "attention_text"],
  "en": ["name", "place_desc", "link", "image_url", "attention_text"]
}',
feature_params_locales =
'{
  "fi": ["Nimi", "Kuvaus", "Linkki", "Kuvalinkki", "Teksti kartalla"],
  "sv": ["Namn", "Beskrivelse", "Webbaddress", "URL-address", "Bild-URL", "Text på kartan"],
  "en": ["Name", "Description", "URL", "Image URL", "Text on map"]
}'
where layer_name = 'oskari:my_places';
