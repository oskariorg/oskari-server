package fi.mml.portti.domain.permissions;

import java.util.ArrayList;
import java.util.List;

public class Permissions implements Comparable<Permissions> {
	public final static String EXTERNAL_TYPE_USER = "USER";
	public final static String EXTERNAL_TYPE_ROLE = "ROLE";
	
	public final static String RESOURCE_TYPE_MAP_LAYER = "maplayer";
	public final static String RESOURCE_TYPE_LAYER_GROUP = "layerclass";
	public final static String RESOURCE_TYPE_DOWNLOADABLE_MATERIAL = "DOWNLOADABLE_MATERIAL";
	public final static String RESOURCE_TYPE_APPLICATION = "RESOURCE_TYPE_APPLICATION";
	
	public final static String PERMISSION_TYPE_PUBLISH = "PUBLISH";
	public final static String PERMISSION_TYPE_VIEW_LAYER = "VIEW_LAYER";
    public final static String PERMISSION_TYPE_EDIT_LAYER = "EDIT_LAYER";
    public final static String PERMISSION_TYPE_EDIT_LAYER_CONTENT = "EDIT_LAYER_CONTENT";
	public final static String PERMISSION_TYPE_VIEW_PUBLISHED = "VIEW_PUBLISHED";
	public final static String PERMISSION_TYPE_DOWNLOAD = "DOWNLOAD";
	public final static String PERMISSION_TYPE_EXECUTE = "EXECUTE";
	
	public final static String APPLICATION_NET_SERVICE_CENTER = "NetServiceCenter";

	public final static String BUNDLE_MY_PLACES = "BundleMyPlaces";
	public final static String BUNDLE_SAVE_MAP_STATE = "BundleSaveMapState";

	
	
	private int id;
	private UniqueResourceName uniqueResourceName = new UniqueResourceName();
	private String externalId;
	private String externalIdType;
	List<String> grantedPermissions;
	
	public String toString() {
		return "id=" + id + ", uniqueResourceName=" + uniqueResourceName + ", externalId=" + externalId 
		+ ", externalIdType=" + externalIdType + ", grantedPermissions=" + grantedPermissions;
	}
	
	public int compareTo(Permissions p) {
		if (this.getExternalIdType().equals(p.getExternalIdType())) {
			if (this.getExternalId().equals(p.getExternalId())) {
				if (this.getUniqueResourceName() == null) {
					return -1;
				} else if (p.getUniqueResourceName() == null) {
					return 1;
				} else {
					if (this.getUniqueResourceName().getType().equals(p.getUniqueResourceName().getType())) {
						if (this.getUniqueResourceName().getNamespace().equals(p.getUniqueResourceName().getNamespace())) {
							return this.getUniqueResourceName().getName()
								.compareTo(p.getUniqueResourceName().getName());
						}
						
						return this.getUniqueResourceName().getNamespace()
							.compareTo(p.getUniqueResourceName().getNamespace());
					}
					
					return this.getUniqueResourceName().getType().compareTo(p.getUniqueResourceName().getType());
				}
			}
			
			return this.getExternalId().compareTo(p.getExternalId());
		}
		
		return this.getExternalIdType().compareTo(p.getExternalIdType());
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UniqueResourceName getUniqueResourceName() {
		return uniqueResourceName;
	}

	public void setUniqueResourceName(UniqueResourceName uniqueResourceName) {
		this.uniqueResourceName = uniqueResourceName;
	}
	
	public String getExternalId() {
		if (externalId == null) {
			return "";
		}

		return externalId;
	}
	
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public String getExternalIdType() {
		if (externalIdType == null) {
			return "";
		}
		return externalIdType;
	}
	
	public void setExternalIdType(String externalIdType) {
		this.externalIdType = externalIdType;
	}

	public List<String> getGrantedPermissions() {
		if (grantedPermissions == null) {
			grantedPermissions = new ArrayList<String>();
		}
		
		return grantedPermissions;
	}

	public void setGrantedPermissions(List<String> grantedPermissions) {
		this.grantedPermissions = grantedPermissions;
	}
}
