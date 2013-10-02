package fi.nls.oskari.work;

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