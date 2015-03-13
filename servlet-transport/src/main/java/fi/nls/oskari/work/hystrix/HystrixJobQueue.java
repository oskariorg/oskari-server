package fi.nls.oskari.work.hystrix;

import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.worker.Job;
import fi.nls.oskari.worker.JobQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Adds support for Hystrix commands, defaults to internal threading solution for non-Hystrix jobs
 */
public class HystrixJobQueue extends JobQueue {
    private static final Logger log = LogFactory.getLogger(HystrixJobQueue.class);
    private Map<String, Future<String>> commandsMapping = new ConcurrentHashMap<String, Future<String>>(100);

    private long mapSize = 0;

    public HystrixJobQueue(int nWorkers) {
        super(nWorkers);

        HystrixPlugins.getInstance().registerCommandExecutionHook(new HystrixCommandExecutionHook() {
            @Override
            public <T> void onSuccess(HystrixInvokable<T> commandInstance) {
                if(commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob)commandInstance;
                    jobEnded(job, true, "Job success", job.getKey());
                }
                super.onSuccess(commandInstance);
            }
            @Override
            public <T> Exception onError(HystrixInvokable<T> commandInstance, HystrixRuntimeException.FailureType failureType, Exception e) {
                if(commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob)commandInstance;
                    jobEnded(job, false, "Error on job", job.getKey(), failureType, e.getMessage());
                }
                return super.onError(commandInstance, failureType, e);
            }
            @Override
            public <T> void onThreadComplete(HystrixInvokable<T> commandInstance) {
                if(commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob)commandInstance;
                    jobEnded(job, true, "Job completed", job.getKey());
                }
                super.onThreadComplete(commandInstance);
            }
            public <T> void onFallbackSuccess(HystrixInvokable<T> commandInstance) {
                if(commandInstance instanceof HystrixJob) {
                    HystrixJob job = (HystrixJob)commandInstance;
                    jobEnded(job, true, "Job fallback", job.getKey());
                }
                super.onFallbackSuccess(commandInstance);
            }

        });
    }

    /**
     * Clean up and log.warn if there was an error
     * @param job
     * @param success
     * @param args
     */
    private void jobEnded(HystrixJob job, boolean success, Object... args) {
        if(success) {
            log.debug(args);
        }
        else {
            log.warn(args);
        }
        // statistics
        setupTimingStatistics(job.getExecutionTimeInMilliseconds());

        commandsMapping.get(job.getKey());
        job.teardown();
        // jobs stick around for some reason, clean the map when job has ended
        cleanup(false);
    }

    public void cleanup(boolean force) {
        List<String> doneJobs = new ArrayList<String>();
        for(Map.Entry<String, Future<String>> entry : commandsMapping.entrySet()) {
            if(entry.getValue().isDone()) {
                doneJobs.add(entry.getKey());
            } else if(force) {
                entry.getValue().cancel(true);
                doneJobs.add(entry.getKey());
            }
        }
        for(String key : doneJobs) {
            commandsMapping.remove(key);
        }
    }

    public long getQueueSize() {
        return super.getQueueSize() + commandsMapping.size();
    }

    public long getMaxQueueLength() {
        return super.getMaxQueueLength() + mapSize;
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
        if(job instanceof HystrixJob) {
            final HystrixJob j = (HystrixJob) job;
            Future<String> existing = commandsMapping.get(j.getKey());
            if (existing != null) {
                existing.cancel(true);
            }
            addJobCount();
            commandsMapping.put(j.getKey(), j.queue());
            if(mapSize < commandsMapping.size()) {
                mapSize = commandsMapping.size();
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
        if(job instanceof HystrixJob) {
            Future<String> existing = commandsMapping.get(job.getKey());
            if (existing != null) {
                commandsMapping.remove(job.getKey());
                existing.cancel(true);
            }
        }
        else {
            super.remove(job);
        }
    }
}
