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

}
