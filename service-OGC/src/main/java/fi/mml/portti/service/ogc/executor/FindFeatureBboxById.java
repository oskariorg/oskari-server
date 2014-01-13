package fi.mml.portti.service.ogc.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.service.WFSCompatibilityHelper;
import fi.nls.oskari.util.ConversionHelper;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayer;

/**
 * Get geometry properties of feature by feature id
 * e.g.  bounding box
 * TODO: class name should be changed e.g. FindGeometryPropertiesOfWfsFeatures
 */
public class FindFeatureBboxById {

    private final static Logger log = LogFactory.getLogger(FindFeatureBboxById.class);
	/**
	 * Find bounding box of wfs_features by wfs feature ids
	 * 
	 * @param list of SearchResultItems (get from search channel search)
	 * @param wfs_id  Wfs layer id for features
	 * @return double[] bbox  (min_west,min_south,maxx_east,maxy_north)
	 */
	public double[] getFeatureBbox(List<SearchResultItem> list, String wfs_id) {

		double[] bbox = { 0.0, 0.0, 0.0, 0.0 };
        final int layerId = ConversionHelper.getInt(wfs_id, -1);
        if(layerId == -1) {
            log.debug("Couldn't get wfs layer id. List:", list);
            return bbox;
        }
		final WFSLayer wfsLayer = WFSCompatibilityHelper.getLayer(layerId);
        final String[] featureIdsWithQnames = new String[list.size()];
        for(int i = 0; i < featureIdsWithQnames.length; ++i) {
            featureIdsWithQnames[i] = list.get(i).getResourceId();
        }

		List<Future<WFSResponseCapsule>> futures = new ArrayList<Future<WFSResponseCapsule>>();

		/* Create workers */
        final FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
		for (SelectedFeatureType sft : wfsLayer.getSelectedFeatureTypes()) {
			Set<FeatureId> fids = findFeatureIdsForQname(ff, sft
					.getFeatureType().getQname().toString(),
					featureIdsWithQnames);
			if (fids.size() == 0) {
				continue;
			}

			Filter filter = ff.id(fids);
			GetFeaturesWorker worker = new GetFeaturesWorker(sft, filter, true);
			Future<WFSResponseCapsule> future = WfsExecutorService
					.schedule(worker);
			futures.add(future);
		}

		if (futures.size() == 0) {
			return bbox;
		}

		/* collect results */
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = WfsExecutorService
				.collectFeaturesFromFutures(futures);

		// Get min-max of selected features
		if (features != null && features.size() > 0) {
			FeatureIterator<SimpleFeature> simpleFeatures = features.features();
			double fminx = 10000000.0;
			double fminy = 10000000.0;
			double fmaxx = -10000000.0;
			double fmaxy = -10000000.0;
			while (simpleFeatures.hasNext()) {
				Feature feature = simpleFeatures.next();

				BoundingBox fbox = feature.getBounds();
				if (fmaxx < fbox.getMaxX())
					fmaxx = fbox.getMaxX();
				if (fmaxy < fbox.getMaxY())
					fmaxy = fbox.getMaxY();
				if (fminx > fbox.getMinX())
					fminx = fbox.getMinX();
				if (fminy > fbox.getMinY())
					fminy = fbox.getMinY();
			}

			bbox[0] = fminx;
			bbox[1] = fminy;
			bbox[2] = fmaxx;
			bbox[3] = fmaxy;
		}

		return bbox;

	}

	/**
	 * Returns featureids for requested qname
	 * 
	 * @param ff
	 * @param requestedQname
	 * @param featureIdsWithQnames
	 * @return
	 */
	private Set<FeatureId> findFeatureIdsForQname(FilterFactory ff,
			String requestedQname, String[] featureIdsWithQnames) {
		Set<FeatureId> result = new HashSet<FeatureId>();
		for (String featureIdWithQname : featureIdsWithQnames) {
			String[] splitted = featureIdWithQname.split(":::");
			if (splitted.length == 2) {
				String featureId = splitted[0];
				String qname = splitted[1];
				if (qname.equals(requestedQname)) {
					result.add(ff.featureId(featureId));
				}
			} else {
				result.add(ff.featureId(featureIdWithQname));
			}
		}
        log.debug("findFeatureIdsForQname - qname:", requestedQname, "- featureIds:", featureIdsWithQnames, "- Result:\n", result);
		return result;
	}
}
