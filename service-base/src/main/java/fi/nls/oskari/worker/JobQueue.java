package fi.nls.oskari.worker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

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

    /**
     * Adds a new job into queue and notifies workers
     * 
     * @param job
     */
    public void add(Job job) {
    	String key = job.getKey();
    	jobs.put(key, job);
        synchronized(queue) {
            queue.addLast(job);
            queue.notify();
        }
        log.debug("Added", key);
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
            Runnable r;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) { }
                    }
                    r = queue.removeFirst();
                }

                try {
                    r.run();
                } catch (Exception e) {
                    log.error("Exception while running job:", e.getMessage());
                    log.debug(e, "Here's the stacktrace");
                }
                finally {
                    ((Job) r).teardown();
                    jobs.remove(((Job) r).getKey());
                    log.debug("Finished", ((Job) r).getKey());
                }
            }
        }
    }
}