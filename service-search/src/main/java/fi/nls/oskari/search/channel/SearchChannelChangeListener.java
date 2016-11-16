package fi.nls.oskari.search.channel;

/**
 * Created by SMAKINEN on 7.11.2016.
 */
public interface SearchChannelChangeListener {

    void onAdd(SearchChannel channel);
    void onRemove(SearchChannel channel);
}
