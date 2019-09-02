package fi.mml.portti.domain.permissions;

public class UniqueResourceName {
	private String name;
	private String namespace;
	private String type;
	private String id;
	
	@Override
	public boolean equals(Object o) {
        if( !(o instanceof UniqueResourceName)) {
            return false;
        }
        return this.toString().equals(o.toString());
	}

	public int compareTo (UniqueResourceName urn) {
		if (!getType().equals(urn.getType())) {
			return getType().compareTo(urn.getType());
		}
		if (!getId().equals(urn.getId())) {
			return getId().compareTo(urn.getId());
		}
		if (!getName().equals(urn.getName())) {
			return getName().compareTo(urn.getName());
		}
		return getNamespace().compareTo(urn.getNamespace());
	}

    @Override
    public int hashCode() {
        int result = id != null ?
				id.hashCode() :
				31 * name.hashCode() + namespace.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public String toString() {
		String str = id != null ?
				"id=" + id :
				"name=" + name + ", namespace=" + namespace;
		return str + ", type=" + type;
	}

	public String getName() {
		if (name == null) {
			return "";
		}
		
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		if (namespace == null) {
			return "";
		}
		
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getType() {
		if (type == null) {
			return "";
		}
		
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		if (id == null) {
			return "";
		}

		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResourceMapping () {
		if (id != null) {
			return id;
		}
		return getNamespace() + "+" + getName();
	}
}
