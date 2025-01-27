package fi.nls.oskari.myplaces.service;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.List;

public interface MyPlacesFeaturesService {

    /**
     * All of the getFeaturesBy** request must return
     * GeoJSON FeatureCollection as a JSONObject
     */
    public JSONObject getFeaturesByCategoryId(long categoryId, String crs) throws ServiceException;
    public JSONObject getFeaturesByUserId(String uuid, String crs) throws ServiceException;
    public JSONObject getFeaturesByMyPlaceId(long[] ids, String crs) throws ServiceException;

    public SimpleFeatureCollection getFeatures(int categoryId, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException;

    /**
     * Returns ids of inserted features
     * TODO: Just set the ids for the existing objects
     */
    public long[] insert(List<MyPlace> places) throws ServiceException;
    public int update(List<MyPlace> places) throws ServiceException;
    public int delete(long[] ids) throws ServiceException;

}
