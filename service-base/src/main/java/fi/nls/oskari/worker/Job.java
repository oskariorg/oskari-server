package fi.nls.oskari.worker;

/**
 * Defines Job interface for JobQueue
 */
public interface Job<T>  {

	/**
	 * Terminates the job
	 */
    public void terminate();

    /**
     * Gets job unique key
     * 
     * @return key
     */
	public String getKey();

    /**
     * Actual implementation method, should be called by execute
     * @return
     * @throws Exception
     */
    public T run() throws Exception;

    /**
     * JobQueue now calls execute() to start up the job
     * @return
     */
    public T execute();

	/**
	 * Checks if job is running
	 * 
	 * @return <code>true</code> if job is running; <code>false</code>
	 *         otherwise.
	 */
    public boolean goNext();

    /**
     * Can be used to hook some post processing stuff. Think of this as "finally".
     */
    public void teardown();

	/**
	 * Checks if job is running and hasNext is true
	 * 
	 * @param hasNext
	 * @return <code>true</code> if job is running and hasNext is true; <code>false</code>
	 *         otherwise.
	 */
    public boolean goNext(final boolean hasNext);
}