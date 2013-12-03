# Oskari Permissions


Two db tables are used to control access rights to layers and bundles. 
A new resource is added into oskari_resource table. Resource type can be maplayer or layerclass for example.
Access rights to resources are set in oskari_permission table. The table contains permissions and references to resources in oskari_resource table.

 
##Oskari permissions (oskari_permission)

| Permission	| Resource type 	| Resource reference 	 | Description					|
| --------------|:-----------------:|:----------------------:|---------------------:		|
| EDIT_LAYER	| Layer				| wms url + wms name	 | Permission to modify a layers|
| VIEW_LAYER	| Layer				| wms url + wms name	 | Permission to view a layer  	|
| ADD_MAPLAYER	| Bundle			| "generic-functionality"| Permission to add layers 	|


## Admin layer rights

There is an admin-layerrights bundle with which admins can set layers' permissions. Check [adding bundles](AddingBundlesBasedOnRole.md) for how to add the bundle.
When the bundle is added,  a role can be chosen and rights for that role for each layer can be set through the Layer Right view.