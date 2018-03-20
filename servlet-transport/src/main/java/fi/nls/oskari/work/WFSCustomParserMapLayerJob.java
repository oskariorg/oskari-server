package fi.nls.oskari.work;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.WFSParser;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.Reader;

/**
 * Job for WFS Map Layer
 */
public class WFSCustomParserMapLayerJob extends  WFSMapLayerJob {

    private static final Logger log = LogFactory.getLogger(WFSCustomParserMapLayerJob.class);

    public WFSCustomParserMapLayerJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer) {
        this(service, type, store, layer, true, true, true);
    }

    public WFSCustomParserMapLayerJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer,
                          boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        super(service,type,store,layer,reqSendFeatures,reqSendImage,reqSendHighlight);

    }
    /**
     * Parses response to features
     *
     * @param layer
     * @return features
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse requestResponse) {
        Reader response = ((WFSRequestResponse) requestResponse).getResponse();

        log.debug("Custom parser layer id: ", layer.getLayerId());
        WFSParser parser = new WFSParser(response, layer);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = parser.parse();
        IOHelper.close(response);
        return features;
    }
}