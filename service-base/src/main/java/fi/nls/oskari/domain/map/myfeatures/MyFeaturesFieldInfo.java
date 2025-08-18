package fi.nls.oskari.domain.map.myfeatures;

public class MyFeaturesFieldInfo {

    private String name;
    private Class<?> type;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Class<?> getType() {
        return type;
    }
    public void setType(Class<?> type) {
        this.type = type;
    }

    public static MyFeaturesFieldInfo of(String name, Class<?> type) {
        MyFeaturesFieldInfo fieldInfo = new MyFeaturesFieldInfo();
        fieldInfo.setName(name);
        fieldInfo.setType(type);
        return fieldInfo;
    }

}
