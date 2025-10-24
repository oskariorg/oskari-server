package org.oskari.control.myfeatures;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayerInfo;

public class MyFeaturesImportResponse {

    private MyFeaturesLayerInfo layer;
    private MyFeaturesImportWarning warning;

    public MyFeaturesLayerInfo getLayer() {
        return layer;
    }

    public void setLayer(MyFeaturesLayerInfo layer) {
        this.layer = layer;
    }

    public MyFeaturesImportWarning getWarning() {
        return warning;
    }

    public void setWarning(MyFeaturesImportWarning warning) {
        this.warning = warning;
    }

}
