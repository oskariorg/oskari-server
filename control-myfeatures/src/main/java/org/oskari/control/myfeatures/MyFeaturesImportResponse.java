package org.oskari.control.myfeatures;

import org.oskari.control.myfeatures.dto.MyFeaturesLayerFullInfo;

public class MyFeaturesImportResponse {

    private MyFeaturesLayerFullInfo layer;
    private MyFeaturesImportWarning warning;

    public MyFeaturesLayerFullInfo getLayer() {
        return layer;
    }

    public void setLayer(MyFeaturesLayerFullInfo layer) {
        this.layer = layer;
    }

    public MyFeaturesImportWarning getWarning() {
        return warning;
    }

    public void setWarning(MyFeaturesImportWarning warning) {
        this.warning = warning;
    }

}
