package org.oskari.service.user;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.service.OskariComponent;
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
    protected abstract OskariLayer getBaseLayer();
    protected abstract UserDataLayer getLayer(int id);

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

    public WFSLayerOptions getWFSLayerOptions(String layerId) {
        int id = parseId(layerId);
        WFSLayerOptions wfsOpts = getLayer(id).getWFSLayerOptions();
        OskariLayer baseLayer = getBaseLayer();
        JSONObject baseOptions = baseLayer == null ? new JSONObject() : baseLayer.getOptions();
        wfsOpts.injectBaseLayerOptions(baseOptions);
        return wfsOpts;
    }
}
