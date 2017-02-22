package fi.nls.oskari.scheduler;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.worker.ScheduledJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;
import java.util.Properties;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * A service for scheduling timed or regular method calls, to be executed in their own threads.
 *
 * The Quartz configuration properties can be set in the scheduler-test.properties file.
 * http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/
 */
public class SchedulerService {

    private static final Logger log = LogFactory.getLogger(SchedulerService.class);

    private static final String JOBS_KEY = "oskari.scheduler.jobs";

    private Scheduler scheduler;

    public SchedulerService initializeScheduler() throws SchedulerException {
        log.info("Initializing scheduler");
        final Properties schedulerProperties = PropertyUtil.getProperties();
        final StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize(schedulerProperties);
        scheduler = sf.getScheduler();
        scheduler.start();
        this.setupJobs();
        return this;
    }

    public void shutdownScheduler() throws SchedulerException {
        if (null != scheduler) {
            scheduler.shutdown(false);
        }
    }

    public SchedulerService setupJobs() {
        log.info("Starting up scheduler jobs");

        final Map<String, ScheduledJob> annotatedJobs = OskariComponentManager.getComponentsOfType(ScheduledJob.class);
        log.info("Annotated scheduler jobs:", annotatedJobs.size());
        for (final Map.Entry<String, ScheduledJob> entry : annotatedJobs.entrySet()) {
            final String cronLine = entry.getValue().getCronLine();
            if (null == cronLine || cronLine.isEmpty()) {
                log.warn("Available scheduled job", entry.getKey(), "needs the cronLine configuration parameter");
            } else {
                this.scheduleJob(entry.getValue(), cronLine);
            }
        }

        final String[] propertiesJobCodes = PropertyUtil.getCommaSeparatedList(JOBS_KEY);
        log.info("Properties based scheduler jobs:", propertiesJobCodes.length);
        for (final String jobCode : propertiesJobCodes) {
            final String key = String.format("oskari.scheduler.job.%s", jobCode);
            final String cronLine = PropertyUtil.getOptional(key + ".cronLine");
            final String className = PropertyUtil.getOptional(key + ".className");
            final String methodName = PropertyUtil.getOptional(key + ".methodName");
            if (null == cronLine || null == className || null == methodName) {
                log.error("the job", jobCode, "needs the cronLine, className and methodName configuration parameters");
            } else {
                this.scheduleMethodCall(jobCode, cronLine, className, methodName);
            }
        }
        return this;
    }
    public SchedulerService scheduleJob(final ScheduledJob scheduledJob, final String cronLine)
    {
        final String jobCode = scheduledJob.getName();

        final JobDetail job = newJob()
                .withIdentity(jobCode)
                .ofType(OskariScheduledJob.class)
                .usingJobData(OskariScheduledJob.CLASS_NAME, scheduledJob.getClass().getName())
                .build();

        final Trigger trigger = newTrigger()
                .withSchedule(cronSchedule(cronLine))
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled job", jobCode);
        } catch (final SchedulerException e) {
            log.error(e, "Failed to schedule job", jobCode);
        }
        return this;
    }

    public SchedulerService scheduleMethodCall(final String jobCode, final String cronLine,
                                   final String className, final String methodName)
    {
        final JobDetail job = newJob()
                .withIdentity(jobCode)
                .ofType(ArbitraryStaticMethodCallJob.class)
                .build();
        final Trigger trigger = newTrigger()
                .withSchedule(cronSchedule(cronLine))
                .usingJobData("className", className)
                .usingJobData("methodName", methodName)
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
            log.info("scheduled job", jobCode);
        } catch (final SchedulerException e) {
            log.error(e, "failed to schedule job", jobCode);
        }
        return this;
    }

}
