package fi.nls.oskari.domain.map.myfeatures;

public class MyFeaturesFieldInfo {

    private String name;
    private MyFeaturesFieldType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyFeaturesFieldType getType() {
        return type;
    }

    public void setType(MyFeaturesFieldType type) {
        this.type = type;
    }

    public static MyFeaturesFieldInfo of(String name, MyFeaturesFieldType type) {
        MyFeaturesFieldInfo fieldInfo = new MyFeaturesFieldInfo();
        fieldInfo.setName(name);
        fieldInfo.setType(type);
        return fieldInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyFeaturesFieldInfo other = (MyFeaturesFieldInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
