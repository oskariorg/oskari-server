package org.oskari.control.myfeatures;

import org.oskari.control.myfeatures.dto.MyFeaturesLayerInfo;

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
