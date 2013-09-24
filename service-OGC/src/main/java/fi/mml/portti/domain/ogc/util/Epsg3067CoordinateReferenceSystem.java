package fi.mml.portti.domain.ogc.util;

import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Epsg3067CoordinateReferenceSystem {

	private static Object lock = new Object();

	private static CoordinateReferenceSystem crs;

	/**
	 * This is basically crap.
	 * 
	 * Problem is that geotools does not currently (10.1.2011) provide EPSG:3067
	 * implementation that could be used. Secondly, we could not figure out
	 * how to load CRS without using URL. You should refactor this
	 * when geotools supports EPSG:3067.
	 * 
	 * Therefore, we load epsg.properties from production server
	 * 
	 *  
	 */
	public static CoordinateReferenceSystem crs() {
		if (crs != null) {
			return crs;
		}

		synchronized (lock) {
			try {
				loadFrom();
				crs = CRS.decode("EPSG:3067");
				return crs;
			} catch (Exception e) {
				throw new RuntimeException("Failed to create CRS", e);
			}
		}
	}
	
	private static void loadFrom() {
		try {
			Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);
			ReferencingFactoryContainer referencingFactoryContainer = ReferencingFactoryContainer.instance(hints);
			PropertyAuthorityFactory factory = null;
			factory = new PropertyAuthorityFactory(referencingFactoryContainer, Citations.fromName("EPSG"), new EpsgUrl().getPropertyUrl());
			ReferencingFactoryFinder.addAuthorityFactory(factory);
			ReferencingFactoryFinder.scanForPlugins();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
