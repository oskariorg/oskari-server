package fi.nls.oskari.work;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.transport.TransportJobException;
import fi.nls.oskari.wfs.WFSExceptionHelper;

import java.util.Map;

/**
 * Common validation routine that should be performed before running OWSMapLayerJobs
 */
public class JobValidator {
    private final Logger log = LogFactory.getLogger(JobValidator.class);

    private OWSMapLayerJob job;

    public JobValidator(OWSMapLayerJob job) {
        this.job = job;
    }

    public OWSMapLayerJob getJob() {
        return job;
    }

    public void onJobCanceled() {
        log.info("Job canceled when running validations for layer", job.getLayerId());
    }

    public boolean validateJob() {
        if(!job.hasValidParams()) {
            onInvalidParams();
            return false;
        }
        if(!job.goNext()) {
            onJobCanceled();
            return false;
        }

        boolean layerPermission = JobHelper.hasPermission(getJob().layerId, job.getSessionId(), job.getRoute());
        if(!layerPermission) {
            onInvalidPermissions();
            return false;
        }

        if(!job.goNext()) {
            onJobCanceled();
            return false;
        }

        if(job.layer == null) {
            onLayerMissing();
            return false;
        }

        if(!job.validateMapScales()) {
            onInvalidScale();
            return false;
        }
        return true;
    }

    public void onLayerMissing() {
        log.warn("Layer (" + job.getLayerId() + ") configurations couldn't be fetched");
        throw new TransportJobException("Layer configurations couldn't be fetched",
                WFSExceptionHelper.ERROR_CONFIGURATION_FAILED);
    }

    public void onInvalidParams() {
        log.warn("Not enough information to continue the task (" + job.type + ")");
    }

    public void onInvalidScale() {
        log.info("Map scale was not valid for layer", job.getLayerId());
        Map<String, Object> output = job.createCommonWarningResponse("Map scale was not valid for layer",
                WFSExceptionHelper.ERROR_LAYER_SCALE_OUT_OF_RANGE);
        output.put(job.OUTPUT_ZOOMSCALE, job.session.getMapScales().get((int) job.session.getLocation().getZoom()));
        output.put(job.OUTPUT_MINSCALE, job.layer.getMinScale());
        output.put(job.OUTPUT_MAXSCALE, job.layer.getMaxScale());
        job.sendCommonErrorResponse(output, true);

    }

    public void onInvalidPermissions() {
        log.warn("Session (" + job.getSessionId() + ") has no permissions for getting the layer (" + job.getLayerId() + ")");
        Map<String, Object> output = job.createCommonWarningResponse("Session has no permissions for getting the layer",
                WFSExceptionHelper.ERROR_NO_PERMISSIONS);
        job.sendCommonErrorResponse(output, true);
    }

}
