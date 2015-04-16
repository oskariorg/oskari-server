package fi.nls.oskari.worker;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Defines an extendable Job for JobQueue
 */
public abstract class AbstractJob<T> implements Job<T> {

    private static final Logger log = LogFactory.getLogger(AbstractJob.class);
	private volatile boolean running = true;

	/**
	 * Empty constructor
	 */
	public AbstractJob() {	}
	
	/**
	 * Terminates the job
	 */
    public final void terminate() {
    	running = false;
    }

    /**
     * Gets job unique key
     * 
     * @return key
     */
	public abstract String getKey();

    /**
     * Calls run, catches and logs any errors.
     * @return
     */
    public T execute() {
        try {
            return run();
        } catch (Exception e) {
            log.error(e, "Error executing job with key", getKey());
        }
        return null;
    }


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