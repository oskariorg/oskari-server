package org.oskari.control.myfeatures;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

public class MyFeaturesImportResponse {

    private MyFeaturesLayer layer;
    private MyFeaturesImportWarning warning;

    public MyFeaturesLayer getLayer() {
        return layer;
    }

    public void setLayer(MyFeaturesLayer layer) {
        this.layer = layer;
    }

    public MyFeaturesImportWarning getWarning() {
        return warning;
    }

    public void setWarning(MyFeaturesImportWarning warning) {
        this.warning = warning;
    }

}
