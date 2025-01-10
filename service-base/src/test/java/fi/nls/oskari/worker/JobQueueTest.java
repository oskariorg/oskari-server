package fi.nls.oskari.worker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
		Assertions.assertTrue(job.goNext() == true, "Should be created");
		Thread.sleep(500); // wait that its running
		Assertions.assertTrue(job.isStarted() == true, "Should run");
		TestJob job2 = new TestJob(2);
    	jobs.add(job2);
		Assertions.assertTrue(job2.goNext() == true, "Should be created");
		Thread.sleep(500); // wait that its running
		Assertions.assertTrue(job2.isStarted() == true, "Should run");
		TestJob job3 = new TestJob(3);
    	jobs.add(job3);
		Assertions.assertTrue(job3.goNext() == true, "Should be created");
		Thread.sleep(500); // wait that its running
		Assertions.assertTrue(job3.isStarted() == false, "Should run");
    	jobs.remove(job);
		Thread.sleep(500); // wait that pool gives turn..
		Assertions.assertTrue(job.goNext() == false, "Should be stopped");
		Assertions.assertTrue(job3.isStarted() == true, "Should run");
	}

}
