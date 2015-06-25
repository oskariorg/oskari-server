package fi.nls.oskari.work.hystrix;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import fi.nls.oskari.work.JobValidator;
import fi.nls.oskari.work.OWSMapLayerJob;

/**
 * Created by SMAKINEN on 8.4.2015.
 */
public class HystrixJobValidator extends JobValidator {

    private boolean isCanceled = false;

    public HystrixJobValidator(OWSMapLayerJob job) {
        super(job);
    }

    @Override
    public void onInvalidParams() {
        super.onInvalidParams();
        throw new HystrixBadRequestException("Not enough information to continue the task (" +  getJob().getType() + ")");

    }
    @Override
    public void onInvalidPermissions() {
        super.onInvalidPermissions();
        throw new HystrixBadRequestException("Session (" +   getJob().getSessionId() + ") has no permissions for getting the layer (" + getJob().getLayerId() + ")");
    }

    @Override
    public void onInvalidScale() {
        super.onInvalidScale();
        throw new HystrixBadRequestException("Map scale was not valid for layer (" + getJob().getLayerId() + ")");
    }

    public void onJobCanceled() {
        super.onJobCanceled();
        isCanceled = true;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
