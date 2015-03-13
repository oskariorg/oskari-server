package fi.nls.oskari.work;

import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.WFSParser;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Job for WFS Map Layer
 */
public class WFSCustomParserMapLayerJob extends  WFSMapLayerJob {

    public WFSCustomParserMapLayerJob(ResultProcessor service, JobType type, SessionStore store, String layerId) {
        this(service, type, store, layerId, true, true, true);
    }

    public WFSCustomParserMapLayerJob(ResultProcessor service, JobType type, SessionStore store, String layerId,
                          boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        super(service,type,store,layerId,reqSendFeatures,reqSendImage,reqSendHighlight);

    }
    /**
     * Parses response to features
     *
     * @param layer
     * @return features
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse requestResponse) {
        BufferedReader response = ((WFSRequestResponse) requestResponse).getResponse();

        log.debug("Custom parser layer id: ", layer.getLayerId());
        WFSParser parser = new WFSParser(response, layer);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = parser.parse();

        try {
            response.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return features;
    }
}