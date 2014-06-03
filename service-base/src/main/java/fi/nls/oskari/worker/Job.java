package fi.nls.oskari.worker;

/**
 * Defines Job interface for JobQueue
 */
public abstract class Job implements Runnable {
	private volatile boolean running = true;

	/**
	 * Empty constructor
	 */
	public Job() {	}
	
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
	 * Checks if job is running
	 * 
	 * @return <code>true</code> if job is running; <code>false</code>
	 *         otherwise.
	 */
	protected boolean goNext() {
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
    protected boolean goNext(final boolean hasNext) {
    	return running && hasNext;
    }
}