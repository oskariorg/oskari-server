package fi.nls.oskari.work.hystrix;

import com.netflix.hystrix.*;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.worker.Job;

/**
 * Replacement for Job in service-base.
 * Properties to configure jobs:
 *  - oskari.[groupName].job.pool.size=100
 *  - oskari.[groupName].job.pool.limit=100
 *  - oskari.[groupName].job.timeoutms=15000
 */
public abstract class HystrixJob extends HystrixCommand<String> implements Job<String> {

    private volatile boolean running = true;
    private long startTime = System.nanoTime();

    public HystrixJob(final String groupName, final String commandName) {
        // Check http://www.nurkiewicz.com/2014/12/benchmarking-impact-of-batching-in.html
        super(Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupName)) // "transport"
                        .andCommandKey(HystrixCommandKey.Factory.asKey(commandName))
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(PropertyUtil.getOptional("oskari." + groupName + ".job.pool.size", 100))
                                    .withMaxQueueSize(PropertyUtil.getOptional("oskari." + groupName + ".job.pool.limit", 100))
                                    .withQueueSizeRejectionThreshold(PropertyUtil.getOptional("oskari." + groupName + ".job.pool.limit", 100)))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional("oskari." + groupName + ".job.timeoutms", 25000))
                                    .withCircuitBreakerRequestVolumeThreshold(PropertyUtil.getOptional("oskari." + groupName + ".failrequests", 5))
                                    .withMetricsRollingStatisticalWindowInMilliseconds( PropertyUtil.getOptional("oskari." + groupName + ".rollingwindow", 100000))
                                    .withCircuitBreakerSleepWindowInMilliseconds(PropertyUtil.getOptional("oskari." + groupName + ".sleepwindow", 20000)))
        );
    }


    public void setStartTime() {
        startTime = System.nanoTime();
    }
    public long getStartTime() {
        return startTime;
    }
    /**
     * Terminates the job
     */
    public void terminate() {
        running = false;
    }

    /**
     * Gets job unique key
     *
     * @return key
     */
    public abstract String getKey();

    /**
     * Checks if job is running
     *
     * @return <code>true</code> if job is running; <code>false</code>
     *         otherwise.
     */
    public boolean goNext() {
        return running;
    }

    /**
     * Can be used to hook some post processing stuff. Think of this as "finally".
     */
    public void teardown() {
    }

    /**
     * Checks if job is running and hasNext is true
     *
     * @param hasNext
     * @return <code>true</code> if job is running and hasNext is true; <code>false</code>
     *         otherwise.
     */
    public boolean goNext(final boolean hasNext) {
        return running && hasNext;
    }
}
