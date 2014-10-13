package fi.nls.oskari.scheduler;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerServiceTest {

    private static final AtomicBoolean METHOD_CALLED = new AtomicBoolean(false);

    /**
     * Called from Quartz.
     */
    public static void scheduledMethod() throws Exception {
        METHOD_CALLED.set(true);
    }

    /**
     * Currently, as the project is still on Java 6, we have to sleep for a couple
     * seconds while waiting for Quartz to do its thing and call our method.
     * TODO: When Java 7 becomes available, replace the sleep+AtomicBoolean with CountdownLatch.
     */
    @Test
    public void createSchedulerService() throws Exception {
        PropertyUtil.clearProperties();
        PropertyUtil.loadProperties("/scheduler-test.properties");
        METHOD_CALLED.set(false);
        try {
            final SchedulerService ss = new SchedulerService();
            ss.initializeScheduler();
            TimeUnit.SECONDS.sleep(2);
            ss.shutdownScheduler();
            Assert.assertTrue("Quartz called the requested method", METHOD_CALLED.get());
        } finally {
            PropertyUtil.clearProperties();
        }
    }

    @Test
    public void createEmptySchedulerService() throws Exception {
        PropertyUtil.clearProperties();
        PropertyUtil.loadProperties("/scheduler-nojobs.properties");
        try {
            final SchedulerService ss = new SchedulerService();
            ss.initializeScheduler();
            ss.shutdownScheduler();
        } finally {
            PropertyUtil.clearProperties();
        }
    }

}
