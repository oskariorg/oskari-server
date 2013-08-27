package fi.mml.portti.service.ogc.executor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fi.nls.oskari.log.LogFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import fi.nls.oskari.log.Logger;

/**
 * Thread pool for executing WFS related queries
 */
public class WfsExecutorService {
	
	/** Logger */
	private static Logger log = LogFactory.getLogger(WfsExecutorService.class);
	
	/** Actual executor */
	private static ExecutorService executor;
	
	/**
	 * Hide constructor
	 */
	private WfsExecutorService() {
		
	}
	
	/**
	 * Returns our executor service
	 * @return
	 */
	private static ExecutorService getExecutorService() {
		if (executor == null) {
			throw new RuntimeException("Executor service not yet started. Call start first!");	
		}
		
		return executor;
	}
	
	/**
	 * Schedules worker to be executed 
	 * 
	 * @param worker
	 * @return Future
	 */
	public static Future<WFSResponseCapsule> schedule(GetFeaturesWorker worker) {
		return getExecutorService().submit(worker);
	}
	
	/**
	 * Starts executor, if not yet started
	 */
	public static void start(int threadCount) {
		log.info("Starting executor service with " + threadCount + " threads...");
		if (executor == null) {
			executor = Executors.newFixedThreadPool(threadCount);
		} else {
			log.warn("Tried to start already started executor service");
		}
	}
	
	/**
	 * Shuts down executor service
	 */
	public static void shutDown() {
		log.warn("Shutting down executor service.");
        if(executor != null) {
            getExecutorService().shutdown();
            executor = null;
        }
	}
	
	@Override
	protected void finalize() throws Throwable {
		shutDown();
		super.finalize();
	}
	
	/**
	 * Utility method for parsing results from futures
	 *  
	 * @param futures
	 * @return
	 */
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> collectFeaturesFromFutures(List<Future<WFSResponseCapsule>> futures) {
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = FeatureCollections.newCollection();
		
        for (Future<WFSResponseCapsule> future: futures) {
        	try {
	        	features.addAll(future.get().getFeatures());
        	} catch (Exception e) {
        		// Bummer, do not throw exception because it will ruin results from every worker.
        		e.printStackTrace();
        	}
        }
        
        return features;
	}
	
}
