package fi.nls.oskari.domain.map;

public class MyPlaceCategory extends UserDataLayer {

    private boolean isDefault;

    @Override
    public final String getType() {
        return OskariLayer.TYPE_MYPLACES;
    }

    @Deprecated
    public String getCategory_name() {
        return getName();
    }

    @Deprecated
    public void setCategory_name(String category_name) {
        setName(category_name);
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

}
