package fi.nls.oskari.search.channel;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import fi.nls.oskari.wfs.WFSSearchChannelsService;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by SMAKINEN on 4.11.2016.
 */
@Oskari
public class WFSChannelProvider extends ChannelProvider {

    public Set<SearchChannel> getChannels() {
        WFSSearchChannelsService channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
        Set<SearchChannel> list = new HashSet<>();
        for(WFSSearchChannelsConfiguration config : channelService.findChannels()) {
            WFSSearchChannel channel = new WFSSearchChannel(config);
            channel.init();
            list.add(channel);
        }
        return list;
    }
    public void channelRemoved(WFSSearchChannelsConfiguration config) {
        channelRemoved(new WFSSearchChannel(config));
    }
    public void channelAdded(WFSSearchChannelsConfiguration config) {
        channelAdded(new WFSSearchChannel(config));
    }
    public void channelUpdated(WFSSearchChannelsConfiguration config) {
        WFSSearchChannel channel = new WFSSearchChannel(config);
        channelRemoved(channel);
        channelAdded(channel);
    }
}
