package fi.nls.oskari.search.channel;

import java.util.List;

public interface SearchAutocomplete {
    List<String> doSearchAutocomplete(String searchString);
}