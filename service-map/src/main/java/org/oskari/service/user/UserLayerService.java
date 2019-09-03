package org.oskari.service.user;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.opengis.filter.Filter;

public abstract class UserLayerService extends OskariComponent {
    //public abstract void getLayers(User user) throws ServiceException;
    //public abstract void getLayer(String layerId, User user) throws ServiceException;
    public abstract boolean isUserContentLayer(String layerId);
    public abstract int getBaselayerId();
    public abstract int parseId(String layerId);
    public abstract JSONObject getOskariStyle(String layerId);
    /**
     * Assumes that layer permissions are checked elsewhere in the code with hasViewPermission() for example
     * @param layerId
     * @param bbox
     * @return
     */
    public abstract Filter getWFSFilter(String layerId, ReferencedEnvelope bbox);
    public SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
        // do nothing, but allows overriding
        return sfc;
    }

    public abstract boolean hasViewPermission(String id, User user);
}