package fi.nls.oskari.scheduler;

import fi.nls.oskari.util.PropertyUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.JobKey.*;
import static org.quartz.TriggerKey.*;
import static org.quartz.DateBuilder.*;
import static org.quartz.impl.matchers.KeyMatcher.*;
import static org.quartz.impl.matchers.GroupMatcher.*;
import static org.quartz.impl.matchers.AndMatcher.*;
import static org.quartz.impl.matchers.OrMatcher.*;
import static org.quartz.impl.matchers.EverythingMatcher.*;

/**
 * A service for scheduling timed or regular method calls, to be executed in their own threads.
 *
 * The Quartz configuration properties can be set in the oskari.properties file.
 * http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/
 */
public class SchedulerService {

    private static final String JOBS_KEY = "oskari.scheduler.jobs";

    private Scheduler scheduler;

    public void initializeScheduler() throws SchedulerException {
        final Properties schedulerProperties = PropertyUtil.getProperties();
        final StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize(schedulerProperties);
        scheduler = sf.getScheduler();
        scheduler.start();
        this.setupJobs();
    }

    public void setupJobs() {
        for (final String job : PropertyUtil.getCommaSeparatedList(JOBS_KEY)) {
            final String key = String.format("oskari.scheduler.job.%s", job);
            final String cronLine = PropertyUtil.get(key + ".cronLine");
            final String className = PropertyUtil.get(key + ".className");
            final String methodName = PropertyUtil.get(key + ".methodName");
            this.scheduleMethodCall(cronLine, className, methodName);
        }
    }

    public void scheduleMethodCall(final String cronLine, final String className, final String methodName) {
        final JobDetail job = newJob()
                .ofType(ArbitraryStaticMethodCallJob.class)
                .build();
        final Trigger trigger = newTrigger()
                .withSchedule(cronSchedule(cronLine))
                .usingJobData("className", className)
                .usingJobData("methodName", methodName)
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
        }
    }

}
