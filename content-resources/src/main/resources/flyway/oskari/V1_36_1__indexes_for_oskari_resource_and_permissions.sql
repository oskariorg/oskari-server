
CREATE INDEX oskari_resource_idx
ON oskari_resource
USING btree
(resource_type, resource_mapping);

CREATE INDEX oskari_permission_resId_idx
ON oskari_permission
USING btree
(oskari_resource_id);