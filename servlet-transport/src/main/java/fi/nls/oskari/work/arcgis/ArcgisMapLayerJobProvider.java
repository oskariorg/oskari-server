package fi.nls.oskari.work.arcgis;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.work.MapLayerJobProvider;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.ResultProcessor;

/**
 * Wrapping for ArcGisMapLayerJob so it can be found with Oskari annotation
 */
@Oskari("arcgis-rest")
public class ArcgisMapLayerJobProvider extends MapLayerJobProvider {

    public OWSMapLayerJob createJob(ResultProcessor service, OWSMapLayerJob.Type type, SessionStore store, String layerId,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        return new ArcGisMapLayerJob(service, type, store, layerId,
                reqSendFeatures, reqSendImage, reqSendHighlight);
    }
}
