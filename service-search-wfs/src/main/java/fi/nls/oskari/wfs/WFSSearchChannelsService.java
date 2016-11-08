package fi.nls.oskari.wfs;

import java.util.List;

import fi.nls.oskari.search.channel.ChannelProvider;
import fi.nls.oskari.service.OskariComponent;

public abstract class WFSSearchChannelsService extends OskariComponent {
    public abstract List<WFSSearchChannelsConfiguration> findChannels();
    public abstract WFSSearchChannelsConfiguration findChannelById(final long channelId);
    public abstract void delete(final long channelId);
    public abstract long insert(final WFSSearchChannelsConfiguration channel);
    public abstract void update(final WFSSearchChannelsConfiguration channel);
}
