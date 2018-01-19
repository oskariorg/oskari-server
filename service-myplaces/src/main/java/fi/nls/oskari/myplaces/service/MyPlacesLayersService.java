package fi.nls.oskari.myplaces.service;

import java.util.List;
import java.util.Optional;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.service.ServiceException;

public interface MyPlacesLayersService {

    public Optional<MyPlaceCategory> getById(long id) throws ServiceException;
    public List<MyPlaceCategory> getByIds(long[] ids) throws ServiceException;
    public List<MyPlaceCategory> getByUserId(String uuid) throws ServiceException;
    public int insert(List<MyPlaceCategory> categories) throws ServiceException;
    public int update(List<MyPlaceCategory> categories) throws ServiceException;
    public int delete(long[] ids) throws ServiceException;

}
