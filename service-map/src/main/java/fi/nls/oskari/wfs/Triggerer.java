package fi.nls.oskari.wfs;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Handles Quartz jobs and triggers and inits them with cache
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
    public void initWFSLayerConfigurationUpdater() {
        // create job
        JobDetail job = JobBuilder.newJob(WFSLayerConfigurationUpdater.class)
                .withIdentity("schemaCacheValidator", "wfs").build();

        // create trigger
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("schemaCacheValidator", "wfs")
                        //.withSchedule(CronScheduleBuilder.cronSchedule("00 00 * * * ?")) // every night
                .withSchedule(CronScheduleBuilder.cronSchedule("0 */1 * * * ?")) // every hour
                .build();

        // give to the cache
        try {
            scheduler.scheduleJob(job, trigger);
        } catch(Exception e) {
            log.error(e, "Could not start the job with trigger, cache failed");
        }
    }
}
