package fi.nls.oskari.domain.map.myfeatures;

public class MyFeaturesFieldInfo {

    private String name;
    private String type;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public static MyFeaturesFieldInfo of(String name, String type) {
        MyFeaturesFieldInfo fieldInfo = new MyFeaturesFieldInfo();
        fieldInfo.setName(name);
        fieldInfo.setType(type);
        return fieldInfo;
    }

}
