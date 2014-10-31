
-- oskari_resource table update for resource mappings --- niiskuke01 and niiskutu01 lportal db
-- new resource mapping is  type+url+name

-- oskari_resource update for wms layers resource mappings

update oskari_resource as a
set resource_mapping = b.type || '+' || b.url || '+' || b.name from oskari_maplayer as b 
where b.type='wmslayer' and a.resource_type='maplayer' and substring(resource_mapping from position('+' in resource_mapping)+1)=b.name 
 and substring(resource_mapping from 1 for position('+' in resource_mapping)-1)=b.url ;

-- oskari_resource update for wmts layers resource mappings

update oskari_resource as a
set resource_mapping = b.type || '+' || b.url || '+' || b.name from oskari_maplayer as b 
where b.type='wmtslayer' and a.resource_type='maplayer' and substring(resource_mapping from position('+' in resource_mapping)+1)=b.name 
 and substring(resource_mapping from 1 for position('+' in resource_mapping)-1)=b.url ;

-- oskari_resource update for collection layers resource mappings

update oskari_resource as a
set resource_mapping = b.type || '+' || b.url || '+' || b.name from oskari_maplayer as b 
where b.type='collection' and a.resource_type='maplayer' and substring(resource_mapping from position('+' in resource_mapping)+1)=b.name 
 and substring(resource_mapping from 1 for position('+' in resource_mapping)-1)=b.url ;

-- oskari_resource update for statslayer layers resource mappings

update oskari_resource as a
set resource_mapping = b.type || '+' || b.url || '+' || b.name from oskari_maplayer as b 
where b.type='statslayer' and a.resource_type='maplayer' and substring(resource_mapping from position('+' in resource_mapping)+1)=b.name 
 and substring(resource_mapping from 1 for position('+' in resource_mapping)-1)=b.url ;

-- oskari_resource update for wfs layer resource mappings  niiskuke01 and niiskutu01 lportal db
-- old resource mapping is "wfs"+name
-- new resource mapping is  type+url+name
-- check 1st that all wfslayer's has url with value "wfs"
-- SELECT id, name, url   FROM oskari_maplayer where type='wfslayer' order by name

update oskari_resource as a
set resource_mapping = b.type || '+' || b.url || '+' || b.name from oskari_maplayer as b 
where b.type='wfslayer' and a.resource_type='maplayer' and substring(resource_mapping from position('+' in resource_mapping)+1)=b.name 
 and substring(resource_mapping from 1 for 4)='wfs+' ;