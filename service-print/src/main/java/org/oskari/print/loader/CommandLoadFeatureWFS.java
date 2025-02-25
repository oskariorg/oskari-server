package org.oskari.print.loader;

import java.util.Optional;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.print.request.PrintLayer;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs.client.OskariFeatureClient;

import fi.nls.oskari.domain.map.OskariLayer;

public class CommandLoadFeatureWFS {
    public static SimpleFeatureCollection getFeatures(OskariFeatureClient featureClient, PrintLayer layer,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs) {
        try {
            String id = layer.getLayerId();
            OskariLayer oskariLayer = layer.getOskariLayer();
            Optional<UserLayerService> processor = layer.getProcessor();
            return featureClient.getFeatures(id, oskariLayer, bbox, crs, processor);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
