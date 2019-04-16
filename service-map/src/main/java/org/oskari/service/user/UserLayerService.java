package org.oskari.service.user;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;

public abstract class UserLayerService extends OskariComponent {
    //public abstract void getLayers(User user) throws ServiceException;
    //public abstract void getLayer(String layerId, User user) throws ServiceException;
    public abstract boolean isUserContentLayer(String layerId);
    public abstract int getBaselayerId();
    public abstract int parseId(String layerId);
    public abstract Filter getWFSFilter(String layerId, String userUuid, ReferencedEnvelope bbox);
    public SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
        // do nothing, but allows overriding
        return sfc;
    }
}