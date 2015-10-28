package fi.nls.oskari.worker;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JobQueueTest {
	private class TestJob extends AbstractJob<String> {
		private int id;
		private boolean started = false;
		
		public TestJob(int id) {	
			this.id = id;
		}
		
		public boolean isStarted() {
			return started;
		}
		
		@Override
		public String run() {
			started = true;
			while(true) {
		    	if(!goNext()) { 
		    		return null;
		    	}
			}
		}

		@Override
		public String getKey() {
			return "test" + id;
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		JobQueue jobs = new JobQueue(2);
		
		TestJob job = new TestJob(1);
    	jobs.add(job);
		assertTrue("Should be created", job.goNext() == true);
		Thread.sleep(500); // wait that its running
		assertTrue("Should run", job.isStarted() == true);
		TestJob job2 = new TestJob(2);
    	jobs.add(job2);
		assertTrue("Should be created", job2.goNext() == true);
		Thread.sleep(500); // wait that its running
		assertTrue("Should run", job2.isStarted() == true);
		TestJob job3 = new TestJob(3);
    	jobs.add(job3);
		assertTrue("Should be created", job3.goNext() == true);
		Thread.sleep(500); // wait that its running
		assertTrue("Should run", job3.isStarted() == false);
    	jobs.remove(job);
		Thread.sleep(500); // wait that pool gives turn..
		assertTrue("Should be stopped", job.goNext() == false);
		assertTrue("Should run", job3.isStarted() == true);
	}

}
