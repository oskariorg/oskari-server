UPDATE portti_wfs_layer SET
selected_feature_params = '{
    "default": ["property_json"]
}'
,feature_params_locales = '{
    "fi": ["Ominaisuustiedot"]
}'
WHERE layer_name = 'oskari:vuser_layer_data';