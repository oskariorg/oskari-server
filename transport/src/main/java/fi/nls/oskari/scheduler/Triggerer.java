package fi.nls.oskari.scheduler;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Handles Quartz jobs and triggers and inits them with scheduler
 */
public class Triggerer {
    private final static Logger log = LogFactory.getLogger(Triggerer.class);

    private Scheduler scheduler;

    public Triggerer() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        } catch(Exception e) {
            log.error(e, "Scheduler couldn't be created");
        }
    }

    public void destroy() {
        try {
            scheduler.shutdown(true);
        } catch(Exception e) {
            log.error(e, "Scheduler shutdown failed");
        }
    }

    /**
     * Inits schema cache validator
     */
    public void initSchemaCacheValidator() {
        // create job
        JobDetail job = JobBuilder.newJob(SchemaCacheValidatorJob.class)
                .withIdentity("schemaCacheValidator", "group1").build();

        // create trigger
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("schemaCacheValidator", "group1")
                //.withSchedule(CronScheduleBuilder.cronSchedule("00 00 * * * ?"))
                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .build();

        // give to the scheduler
        try {
            scheduler.scheduleJob(job, trigger);
        } catch(Exception e) {
            log.error(e, "Could not start the job with trigger, scheduler failed");
        }
    }
}
