package org.oskari.print.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.service.wfs.client.OskariFeatureClient;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;

public class AsyncFeatureLoader {

    public static final String GROUP_KEY = "LoadFeatureFromURL";

    public static Map<Integer, Future<SimpleFeatureCollection>> initLayers(PrintRequest request,
            OskariFeatureClient featureClient) throws ServiceException {
        Map<Integer, Future<SimpleFeatureCollection>> featureCollections = new HashMap<>();

        List<PrintLayer> requestedLayers = request.getLayers();
        if (requestedLayers == null) {
            return featureCollections;
        }
        
        String uuid = request.getUser().getUuid();

        CoordinateReferenceSystem crs = request.getCrs();
        double[] bbox = request.getBoundingBox();
        ReferencedEnvelope bbox1 = new ReferencedEnvelope(bbox[0], bbox[2], bbox[1], bbox[3], crs);
        
        for (PrintLayer layer : requestedLayers) {
            switch (layer.getType()) { 
            case OskariLayer.TYPE_WFS:
                featureCollections.put(layer.getZIndex(), new CommandLoadFeatureWFS(
                        featureClient, layer, uuid, bbox1, crs).queue());
                break;
            }
        }

        return featureCollections;
    }

    

    
}
