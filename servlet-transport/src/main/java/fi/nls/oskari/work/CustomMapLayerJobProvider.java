package fi.nls.oskari.work;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.worker.Job;

/**
 * Wrapping for WFSCustomParserMapLayerJob so it can be found with Oskari annotation
 */
@Oskari("oskari-custom-parser")
public class CustomMapLayerJobProvider extends MapLayerJobProvider {

    public Job createJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {

        return new WFSCustomParserMapLayerJob(service, type, store, layer,
                reqSendFeatures, reqSendImage, reqSendHighlight);
    }
}
