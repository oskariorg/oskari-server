package fi.nls.oskari.search.channel;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import fi.nls.oskari.wfs.WFSSearchChannelsService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Oskari
public class WFSChannelProvider extends ChannelProvider {

    public Set<SearchChannel> getChannels() {
        if (PropertyUtil.getOptional("search.ignoreWFSchannels", false)) {
            // skip initializing wfs search channels. Mainly for preventing unnecessary noise when running unit tests
            // as tests throw SQL errors since there is no db tables/content for test to use
            return Collections.emptySet();
        }
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
