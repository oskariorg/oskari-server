package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.HashMap;
import java.util.List;

public abstract class AnalysisDbService extends OskariComponent {

        public abstract long insertAnalysisRow(final Analysis analysis);
        public abstract long updateAnalysisCols(final Analysis analysis);
        public abstract Analysis getAnalysisById(long id);
        public abstract List<Analysis> getAnalysisById(List<Long> idList);
        public abstract List<Analysis> getAnalysisByUid(String uid);
        public abstract List<HashMap<String,Object>> getAnalysisDataByIdUid(long id, String uid, String select_body);
        public abstract void deleteAnalysisById(final long id) throws ServiceException;
        public abstract void deleteAnalysisByUid(final String id) throws ServiceException;
        public abstract void deleteAnalysis(final Analysis analysis) throws ServiceException;
        public abstract void mergeAnalysis(final Analysis analysis, final List<Long> ids) throws ServiceException;
        public abstract long updatePublisherName(final long id, final String uuid, final String name);

        public abstract JSONObject getFeatures(int layerId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException;
}
