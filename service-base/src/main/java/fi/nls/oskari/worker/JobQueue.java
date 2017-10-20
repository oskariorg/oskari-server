package fi.nls.oskari.worker;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.*;

/**
 * Manages workers for different kind of jobs
 */
public class JobQueue
{
	private static final Logger log = LogFactory.getLogger(JobQueue.class);
			
    private final int nWorkers;
    private final Worker[] workers;
    private final Map<String, Job> jobs;
    private final LinkedList<Job> queue;

    private long maxQueueLength = 0;
    private long maxJobLength = 0;
    private long minJobLength = Long.MAX_VALUE;
    private long jobCount = 0;
    private long avgRuntime = 0;
    private String firstCrashedJob = null;
    private long crashedJobCount = 0;

    /**
     * Initializes a queue and workers
     * 
     * @param nWorkers
     */
    public JobQueue(int nWorkers)
    {
        this.nWorkers = nWorkers;
        queue = new LinkedList<Job>();
        workers = new Worker[this.nWorkers];
        jobs = new HashMap<String, Job>();

        for (int i = 0; i < this.nWorkers; i++) {
        	workers[i] = new Worker();
        	workers[i].start();
        }
    }
    public long getMaxQueueLength() {
        return maxQueueLength;
    }

    public long getMaxJobLength() {
        return maxJobLength;
    }

    public long getMinJobLength() {
        return minJobLength;
    }

    public long getJobCount() {
        return jobCount;
    }

    public long getAvgRuntime() {
        return avgRuntime;
    }
    public long getQueueSize() {
        return queue.size();
    }

    public String getFirstCrashedJob() {
        return firstCrashedJob;
    }

    public long getCrashedJobCount() {
        return crashedJobCount;
    }

    public List<String> getQueuedJobNames() {
        List<String> names = new ArrayList<String>(queue.size());
        for(Job j : queue) {
            names.add(j.getKey());
        }
        return names;
    }

    public void cleanup(boolean force) {
        for(Job j : queue) {
            if(force) {
                remove(j);
            }
        }
    }

    /**
     * Adds a new job into queue and notifies workers
     * 
     * @param job
     */
    public void add(Job job) {
        // removed previous job with same key
        remove(job);
    	String key = job.getKey();
    	jobs.put(key, job);
        synchronized(queue) {
            queue.addLast(job);
            queue.notify();
        }
        if(maxQueueLength < queue.size()) {
            maxQueueLength = queue.size();
        }
        log.debug("Added", key);
    }

    public void addJobCount() {
        jobCount++;
    }

    public void setupTimingStatistics(long runTimeMS) {
        if(runTimeMS > maxJobLength) {
            maxJobLength = runTimeMS;
        }
        if(runTimeMS < minJobLength) {
            minJobLength = runTimeMS;
        }
        if(avgRuntime == 0) {
            avgRuntime = runTimeMS;
        }
        else {
            avgRuntime = ((avgRuntime * (jobCount -1)) + runTimeMS) / jobCount;
        }
    }

    /**
     * Removes a job from queue and terminates a running job
     * 
     * @param job
     */
    public void remove(Job job) {
    	String key = job.getKey();
    	Job processedJob = jobs.get(key);
		if(processedJob != null)
			processedJob.terminate();
        synchronized(queue) {
        	queue.remove(job);
        }
        log.debug("Removed", key);
    }

    public void onJobSuccess(final Job job, final Object value) {
        // convenience method for extension hooks
        log.debug("Job success");
    }

    /**
     * Note! Throwable can be null if some unexpected error occured.
     * @param job
     * @param value
     */
    public void onJobFailed(final Job job, final Throwable value) {
        final boolean knownCause = value != null;
        // convenience method for extension hooks
        log.error("Exception while running job:", knownCause ? value.getMessage() : "Unknown error", job.getKey());
        if(knownCause) {
            log.debug(value, "Stacktrace");
        }
    }
    
    /**
     * Defines a worker thread for queue's job
     */
    private class Worker extends Thread { 
    	
    	/**
    	 * Processes queues jobs
    	 * 
    	 * Waits for the queue to have some jobs.
    	 * Always takes the first job available, runs it and removes it from queue.
    	 * 
    	 */
        public void run() {
            Job r;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) { }
                    }
                    r = queue.removeFirst();
                }
                final long startTime = System.nanoTime();
                addJobCount();
                boolean notified = false;
                try {
                    final Object o = r.run();
                    onJobSuccess(r, o);
                    notified = true;
                } catch (Exception e) {
                    onJobFailed(r, e);
                    notified = true;
                }
                catch (OutOfMemoryError e) {
                    crashedJobCount++;
                    log.error("OutOfMemory while running job:", r.getKey(), "- message", e.getMessage());
                    if(firstCrashedJob == null) {
                        firstCrashedJob = r.getKey();
                    }
                    onJobFailed(r, e);
                    notified = true;
                    throw e;
                }
                finally {
                    if(!notified) {
                        onJobFailed(r, null);
                    }
                    r.teardown();
                    jobs.remove(r.getKey());
                    log.debug("Finished", r.getKey());
                    final long runTimeMS = (System.nanoTime() - startTime) / 1000000L;
                    setupTimingStatistics(runTimeMS);
                }
            }
        }
    }
}