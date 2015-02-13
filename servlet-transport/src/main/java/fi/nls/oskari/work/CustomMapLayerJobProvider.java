package fi.nls.oskari.work;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.service.OskariComponent;

/**
 * Wrapping for WFSCustomParserMapLayerJob so it can be found with Oskari annotation
 */
@Oskari("oskari-custom-parser")
public class CustomMapLayerJobProvider extends MapLayerJobProvider {

    public OWSMapLayerJob createJob(ResultProcessor service, OWSMapLayerJob.Type type, SessionStore store, String layerId,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        return new WFSCustomParserMapLayerJob(service, type, store, layerId,
                reqSendFeatures, reqSendImage, reqSendHighlight);
    }
}
