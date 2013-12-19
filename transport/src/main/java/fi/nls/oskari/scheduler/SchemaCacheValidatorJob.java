package fi.nls.oskari.scheduler;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SchemaCacheValidatorJob implements Job {
    private final static Logger log = LogFactory.getLogger(SchemaCacheValidatorJob.class);

    public static final String SCHEMA_CHANNEL = "schemaInfo";

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        JedisManager.publish(SCHEMA_CHANNEL, "test message");
        log.warn("message sent");
    }
}
