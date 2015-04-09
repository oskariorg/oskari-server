package fi.nls.oskari.work.arcgis;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.*;
import fi.nls.oskari.worker.Job;

/**
 * Wrapping for ArcGisMapLayerJob so it can be found with Oskari annotation
 */
@Oskari("arcgis-rest")
public class ArcgisMapLayerJobProvider extends MapLayerJobProvider {

    public Job createJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {

        return new ArcGisMapLayerJob(service, type, store, layer,
                reqSendFeatures, reqSendImage, reqSendHighlight);
    }
}
