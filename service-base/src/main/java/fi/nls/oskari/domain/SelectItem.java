package fi.nls.oskari.domain;

public class SelectItem implements Comparable<SelectItem> {

	private String name = "";
	private String value = "";
	
	public int compareTo(SelectItem si2) {
        if(si2 == null) {
            return 0;
        }
		return this.getName().compareTo(si2.getName());
	}
	
	public SelectItem(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
    public String getName(boolean actual) {
        if(actual) {
            return name;
        }
        return getName();
    }
	public String getName() {
        if(name == null || name.isEmpty()) {
            return getValue();
        }
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	

}
