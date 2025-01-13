package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.HashMap;
import java.util.List;

public abstract class AnalysisDbService extends OskariComponent {

        public abstract Analysis getAnalysisById(long id);
        public abstract List<Analysis> getAnalysisByUid(String uid);
        public abstract List<HashMap<String,Object>> getAnalysisDataByIdUid(long id, String uid, String select_body);
        public abstract void deleteAnalysisByUid(final String id) throws ServiceException;
        public abstract void deleteAnalysis(final Analysis analysis) throws ServiceException;
        public abstract long updatePublisherName(final long id, final String uuid, final String name);

        public abstract SimpleFeatureCollection getFeatures(int layerId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException;
}
