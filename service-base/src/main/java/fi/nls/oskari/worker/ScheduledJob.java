package fi.nls.oskari.worker;

import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Map;

/**
 * Marker base class for annotation based scheduled jobs.
 * Classes extending this are not automatically run, but need a
 * scheduler to use them. One is provided in service-scheduler Oskari-module.
 *
 * To make a new scheduled job extend this class and annotate the class with
 * @Oskari("MyJobId"). The service-scheduler will detect any such class and
 * try to schedule it by getting a cron-line from properties with key:
 * 'oskari.scheduler.job.MyJobId.cronLine'
 */
public abstract class ScheduledJob extends OskariComponent {

    public abstract void execute(Map<String, Object> params);

    public String getCronLine() {
        final String key = String.format("oskari.scheduler.job.%s", getName());
        return PropertyUtil.getOptional(key + ".cronLine");
    }
}
