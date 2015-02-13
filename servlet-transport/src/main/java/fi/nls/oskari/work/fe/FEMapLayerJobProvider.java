package fi.nls.oskari.work.fe;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.work.MapLayerJobProvider;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.ResultProcessor;
import fi.nls.oskari.work.WFSMapLayerJob;

/**
 * Wrapping for WFSMapLayerJob so it can be found with Oskari annotation
 */
@Oskari("oskari-feature-engine")
public class FEMapLayerJobProvider extends MapLayerJobProvider {

    public OWSMapLayerJob createJob(ResultProcessor service, OWSMapLayerJob.Type type, SessionStore store, String layerId,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        return new FEMapLayerJob(service, type, store, layerId,
                reqSendFeatures, reqSendImage, reqSendHighlight);
    }
}
