package fi.nls.oskari.myplaces.service;

import java.util.List;

import org.json.JSONObject;

import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.service.ServiceException;

public interface MyPlacesFeaturesService {

    /**
     * All of the getFeaturesBy** request must return
     * GeoJSON FeatureCollection as a JSONObject
     */
    public JSONObject getFeaturesByCategoryId(long categoryId, String crs) throws ServiceException;
    public JSONObject getFeaturesByUserId(String uuid, String crs) throws ServiceException;
    public JSONObject getFeaturesByMyPlaceId(long[] ids, String crs) throws ServiceException;

    /**
     * Returns ids of inserted features
     * TODO: Just set the ids for the existing objects
     */
    public long[] insert(List<MyPlace> places) throws ServiceException;
    public int update(List<MyPlace> places) throws ServiceException;
    public int delete(long[] ids) throws ServiceException;

}
