package fi.nls.oskari.search.channel;

import fi.nls.oskari.service.OskariComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Search service will query for channel providers and add all SearchChannels returned by
 * getChannels() to available channels
 */
public abstract class ChannelProvider extends OskariComponent {
    private List<SearchChannelChangeListener> listeners = new ArrayList<>();
    public abstract Set<SearchChannel> getChannels();

    public void channelAdded(SearchChannel channel) {
        for(SearchChannelChangeListener handler : listeners) {
            handler.onAdd(channel);
        }
    }
    public void channelRemoved(SearchChannel channel) {
        for(SearchChannelChangeListener handler : listeners) {
            handler.onRemove(channel);
        }
    }

    public void addListener(SearchChannelChangeListener handler) {
        listeners.add(handler);
    }
    public void removeListener(SearchChannelChangeListener handler) {
        listeners.remove(handler);
    }

}
