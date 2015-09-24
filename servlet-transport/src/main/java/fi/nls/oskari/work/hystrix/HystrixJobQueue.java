package fi.nls.oskari.work.hystrix;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.hystrix.metrics.AvgJobLengthGauge;
import fi.nls.oskari.work.hystrix.metrics.MaxJobLengthGauge;
import fi.nls.oskari.work.hystrix.metrics.MinJobLengthGauge;
import fi.nls.oskari.work.hystrix.metrics.TimingGauge;
import fi.nls.oskari.worker.Job;
import fi.nls.oskari.worker.JobQueue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds support for Hystrix commands, defaults to internal threading solution for non-Hystrix jobs
 */
public class HystrixJobQueue extends JobQueue {
    private static final Logger LOG = LogFactory.getLogger(HystrixJobQueue.class);
    private Map<String, Job<String>> commandsMapping = new ConcurrentHashMap<String, Job<String>>(100);
    private MetricRegistry metrics = new MetricRegistry();

    private long mapMaxSize = 0;
    private Map<String, TimingGauge> customMetrics = new ConcurrentHashMap<String, TimingGauge>();

    public HystrixJobQueue(int nWorkers) {
        super(nWorkers);

        HystrixPlugins.getInstance().registerCommandExecutionHook(new HystrixCommandExecutionHook() {
            /**
             * Actual run method completed successfully
             * @param commandInstance
             * @param <T>
             */
            @Override
            public <T> void onExecutionSuccess(HystrixInvokable<T> commandInstance) {
                if (commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob) commandInstance;
                    jobEnded(job, true, "Job completed", job.getKey());
                }
                super.onExecutionSuccess(commandInstance);
            }

            /**
             * When command fails with an exception
             * @param commandInstance
             * @param failureType
             * @param e
             * @param <T>
             * @return
             */
            @Override
            public <T> Exception onError(HystrixInvokable<T> commandInstance,
                                         HystrixRuntimeException.FailureType failureType, Exception e) {
                if (commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob) commandInstance;
                    jobEnded(job, false, "Error on job", job.getKey(), failureType, LOG.getCauseMessages(e));
                }
                return super.onError(commandInstance, failureType, e);
            }

            /**
             * Fallback is an error handler so treat as error
             * @param commandInstance
             * @param <T>
             */
            @Override
            public <T> void onFallbackSuccess(HystrixInvokable<T> commandInstance) {
                if (commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob) commandInstance;
                    jobEnded(job, false, "Job fallback", job.getKey());
                }
                super.onFallbackSuccess(commandInstance);
            }

        });
    }

    /**
     * Clean up and LOG.warn if there was an error
     * @param job
     * @param success
     * @param args
     */
    private void jobEnded(HystrixJob job, boolean success, Object... args) {
        try {
            if(success) {
                LOG.debug(args);
                onJobSuccess(job, null);
            }
            else {
                LOG.warn(args);
                onJobFailed(job, null);
            }

            // NOTE! job.getExecutionTimeInMilliseconds() doesn't seem to provide correct values
            // maybe because we use futures/queue() instead of execute()?
            final long runtimeMS = (System.nanoTime() - job.getStartTime()) / 1000000L;
            // statistics
            if(job instanceof HystrixMapLayerJob) {
                HystrixMapLayerJob mlJob = (HystrixMapLayerJob) job;
                // manually call terminate just in case (timeout and such)
                mlJob.terminate();
                mlJob.notifyCompleted(success);
                final String jobId = mlJob.getJobId();
                final Histogram timing = metrics.histogram(
                        MetricRegistry.name(HystrixMapLayerJob.class, "exec.time." + jobId));
                timing.update(runtimeMS);

                TimingGauge gauge = customMetrics.get(jobId);
                if(gauge == null) {
                    gauge = new TimingGauge();
                    customMetrics.put(jobId, gauge);
                    // first run
                    metrics.register(MetricRegistry.name(HystrixJobQueue.class, "job.length.max." + jobId), new MaxJobLengthGauge(gauge));
                    metrics.register(MetricRegistry.name(HystrixJobQueue.class, "job.length.min." + jobId), new MinJobLengthGauge(gauge));
                    metrics.register(MetricRegistry.name(HystrixJobQueue.class, "job.length.avg." + jobId), new AvgJobLengthGauge(gauge));
                }
                LOG.debug("Job completed in", runtimeMS);
                gauge.setupTimingStatistics(runtimeMS);

                if(!success) {
                    final Counter failCounter = metrics.counter(
                            MetricRegistry.name(HystrixJobQueue.class, "jobs.fails." + jobId));
                    failCounter.inc();
                }
            }
            setupTimingStatistics(runtimeMS);
        }
        finally {
            commandsMapping.remove(job.getKey());
            job.teardown();
        }
    }

    public MetricRegistry getMetricsRegistry() {
        return metrics;
    }

    public void cleanup(boolean force) {
        if(!force) {
            // do nothing
            return;
        }
        for(Job<String> job : commandsMapping.values()) {
            job.terminate();
        }
        commandsMapping.clear();
    }

    public long getQueueSize() {
        return super.getQueueSize() + commandsMapping.size();
    }

    public long getMaxQueueLength() {
        return super.getMaxQueueLength() + mapMaxSize;
    }

    public List<String> getQueuedJobNames() {
        final List<String> names = super.getQueuedJobNames();
        names.addAll(commandsMapping.keySet());
        return names;
    }
    /**
     * Custom handling for HystrixJobs, call super on other type of jobs
     * @param job
     */
    public void add(Job job) {
        if(job == null) {
            return;
        }
        // removed previous job with same key
        remove(job);
        if(job instanceof OWSMapLayerJob) {
            // wrap to HystrixMapLayerJob
            HystrixMapLayerJob hJob = new HystrixMapLayerJob((OWSMapLayerJob)job);
            Meter addMeter = metrics.meter(
                    MetricRegistry.name(HystrixJobQueue.class, "job.added." + hJob.getJobId()));
            addMeter.mark();
            addJobCount();
            hJob.queue();
            commandsMapping.put(job.getKey(), job);
            // track max size of the map
            if(mapMaxSize < commandsMapping.size()) {
                mapMaxSize = commandsMapping.size();
            }
        }
        else {
            super.add(job);
        }
    }

    /**
     * Custom handling for HystrixJobs, call super on other type of jobs
     * @param job
     */
    public void remove(Job job) {
        if(job == null) {
            return;
        }
        if(job instanceof OWSMapLayerJob || job instanceof HystrixJob) {
            Job<String> existing = commandsMapping.get(job.getKey());
            if (existing != null) {
                existing.terminate();
                commandsMapping.remove(job.getKey());
            }
        }
        else {
            super.remove(job);
        }
    }
}
