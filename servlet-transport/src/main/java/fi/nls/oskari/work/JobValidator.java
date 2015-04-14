package fi.nls.oskari.work;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Common validation routine that should be performed before running OWSMapLayerJobs
 */
public class JobValidator {
    private static final Logger log = LogFactory.getLogger(JobValidator.class);

    private OWSMapLayerJob job;

    public JobValidator(OWSMapLayerJob job) {
        this.job = job;
    }

    public OWSMapLayerJob getJob() {
        return job;
    }

    public void onJobCanceled() {

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
            return false;
        }
        return true;
    }

    public void onLayerMissing() {
        log.warn("Layer (" + job.getLayerId() + ") configurations couldn't be fetched");
        job.sendCommonErrorResponse(ResultProcessor.ERROR_CONFIGURATION_FAILED, true);
        throw new RuntimeException("Layer (" +  job.getLayerId() + ") configurations couldn't be fetched");
    }
    public void onInvalidParams() {
        log.warn("Not enough information to continue the task (" +  job.type + ")");
    }
    public void onInvalidScale() {
        log.info("Map scale was not valid for layer", job.getLayerId());
    }
    public void onInvalidPermissions() {
        log.warn("Session (" + job.getSessionId() + ") has no permissions for getting the layer (" + job.getLayerId() + ")");
        job.sendCommonErrorResponse(ResultProcessor.ERROR_NO_PERMISSIONS, true);
    }

}
