package org.oskari.service.user;

import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.user.User;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Envelope;

public abstract class UserLayerService extends OskariComponent {

    public abstract OskariLayer getOskariLayer(String layerId);
    public abstract boolean isUserContentLayer(String layerId);
    public abstract boolean hasViewPermission(String layerId, User user);
    public abstract WFSLayerOptions getWFSLayerOptions(String layerId);
    public abstract SimpleFeatureCollection getFeatures(String layerId, Envelope bbox) throws ServiceException;

    public LayerExtendedOutput describeLayer(String layerId, String lang, CoordinateReferenceSystem crs) {
        return null;
    }
}
