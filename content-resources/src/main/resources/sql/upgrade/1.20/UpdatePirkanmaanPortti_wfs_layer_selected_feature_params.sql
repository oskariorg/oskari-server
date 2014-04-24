UPDATE portti_wfs_layer
   SET 
       selected_feature_params='{
    "default":["NIMI","KOHDETYYPPI","KOHTEEN_ID","INVENTOINTINO","HANKETIETO","KYLA","KAUPUNGINOSA","OSOITE","RAKENNUSLKM","RAKENNUSHARVO","HISTARVO","YMPARISTOARVO","INVENTOINTIPVM","DIGITOINTIPVM"]
}'
       
 WHERE feature_element='"MU_RAK_YMP_KOHDE';

 UPDATE portti_wfs_layer
   SET 
       selected_feature_params='{
    "default":["NIMI","ALUELUOKKA","ALUEEN_ID"]
}'
       
 WHERE feature_element='"MU_ALUEET';

 UPDATE portti_wfs_layer
   SET 
       selected_feature_params='{
    "default":["KOHTEEN_ID"]
}'
       
 WHERE feature_element='"MU_MUINAISJ';