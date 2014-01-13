package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
// import org.quartz.Job;
// import org.quartz.JobExecutionContext;
// import org.quartz.JobExecutionException;

//TODO: should be replaced to other package - this boms currently liferay quartz management

public class WFSLayerConfigurationUpdater { //implements Job {
 /*   private final static Logger log = LogFactory.getLogger(WFSLayerConfigurationUpdater.class);

    public static final String LAYER_CHANNEL = "layerConfiguration";

    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        log.warn("1. UPDATING WFS CONF");
        // TODO: put all layer information in redis and then send publish message on some channel to transport so that the schema task can start
        // this can be done last because we have some conf @ redis already
        JedisManager.publish(LAYER_CHANNEL, "updated");
    } */

}
