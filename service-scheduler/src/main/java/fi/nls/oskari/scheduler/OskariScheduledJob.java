package fi.nls.oskari.scheduler;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.worker.ScheduledJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
* A configurable Quartz job wrapper for executing Oskari ScheduledJobs.
 *
 * Requires JobData entry for "className" which should point to a class extending fi.nls.oskari.worker.ScheduledJob.
*/
public class OskariScheduledJob implements Job {

    private static final Logger log = LogFactory.getLogger(OskariScheduledJob.class);

    protected static final String CLASS_NAME = "className";

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getMergedJobDataMap();

        try {
            final Class<ScheduledJob> clazz = (Class<ScheduledJob>)Class.forName(data.getString(CLASS_NAME));
            ScheduledJob job = clazz.newInstance();
            job.execute(data.getWrappedMap());
        } catch (final IllegalAccessException e) {
            log.error(e, "illegal access in method call");
        } catch (final ClassNotFoundException e) {
            log.error(e, "class not found", data.getString(CLASS_NAME));
        } catch (InstantiationException e) {
            log.error(e, "Instantiation failed");
        }
    }
}
