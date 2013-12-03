package fi.mml.portti.domain.permissions;

public class UniqueResourceName {
	private String name;
	private String namespace;
	private String type;
	
	@Override
	public boolean equals(Object o) {
		UniqueResourceName u2 = (UniqueResourceName) o;
		return this.getName().equals(u2.getName()) 
			&& this.getNamespace().equals(u2.getNamespace()) 
			&& this.getType().equals(u2.getType());
	}
	
	public String toString() {
		return "name=" + name + ", namespace=" + namespace + ", type=" + type;
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
}
