# Oskari Permissions

The maven module under service-permissions provides a generic permission handling database. Two db tables (oskari_resource and oskari_permission) are used to control access rights to layers and bundles. 

## Example: grant view permission for maplayer

We want to grant access for guest users to see a maplayer:

* assume guests have role with id 10110 (configurable in UserService implementation)

* assume layer wmsurl is http://www.oskari.org

* assume layer wmsname is oskari

The maplayer needs to be registered as a resource for permissions db (since the permission implementation is generic).

### oskari_resource

| resource_type	| Resource_mapping		        |
| --------------|:-----------------------------:|
| maplayer      | http://www.oskari.org+oskari  |


* Resource_type is a string reference to another table for example in this case oskari_maplayer.

* Resource_mapping is a table specific reference to the resource in question. For maplayers its url+name since this combination references to an actual unique service. The value could be the layers id in oskari_maplayer, but then one could insert a layer for some url and then after receiving permissions change the layer to a completely different service (in this case the person granting permissions might be another than the one adding/modifying the actual layer).

After the layer has been added as a resouce we need to map the actual permissions

### oskari_permission

| oskari_resource_id | permission    | external_type | external_id |
| -------------------|:-------------:|:-------------:|:-----------:|
| 1	                 | EDIT_LAYER    | ROLE          | 10110       |


* oskari_resource_id is reference to a row in oskari_resource table

* permission is a constant value of the type of permission in question

* external_type is a reference link for the external_id. Usually permissions are mapped for user roles so you the value is then 'ROLE' which means external_id is a role id. Another possible external_type is 'USER' which means external_id is a user id, but this isn't widely used or might not be supported in all cases. Recommented use for now is to only use ROLE mappings.

### Permission types

* VIEW_LAYER: Permission to view a layer

* PUBLISH: Permission to publish a layer/use when creating an embedded map

* VIEW_PUBLISHED: Permission to view a layer in a published/embedded map

* EDIT_LAYER: Permission to modify a layer

## Example: grant permission to add layers (for non-admin user role)

Adding layers is currently a generic permission (not mapped to data producer or similar). We need to add a resource for "generic-functionality".

### oskari_resource

| resource_type	| Resource_mapping		        |
| --------------|:-----------------------------:|
| Bundle        | generic-functionality         |

### oskari_permission

| oskari_resource_id | permission    | external_type | external_id |
| -------------------|:-------------:|:-------------:|:-----------:|
| 2	                 | ADD_MAPLAYER  | ROLE          | 10110       |


## Admin layer rights

There is an admin-layerrights bundle with which admins can set layers' permissions. Check [adding bundles](AddingBundlesBasedOnRole.md) for how to add the bundle.
When the bundle is added a role can be chosen and rights for that role for each layer can be set through the Layer Right view.