package fi.nls.oskari.work.arcgis;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.work.JobType;
import fi.nls.oskari.work.MapLayerJobProvider;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.ResultProcessor;
import fi.nls.oskari.work.hystrix.HystrixMapLayerJob;

/**
 * Wrapping for ArcGisMapLayerJob so it can be found with Oskari annotation
 */
@Oskari("arcgis-rest")
public class ArcgisMapLayerJobProvider extends MapLayerJobProvider {

    public HystrixMapLayerJob createJob(ResultProcessor service, JobType type, SessionStore store, String layerId,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        /*
        return new ArcGisMapLayerJob(service, type, store, layerId,
                reqSendFeatures, reqSendImage, reqSendHighlight);
                */
        return null;
    }
}
