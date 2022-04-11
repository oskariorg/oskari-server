package fi.nls.oskari.wfs;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.utils.common.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WFSCapabilitiesService {
    protected static Set<String> SUPPORTED_FEATURE_FORMATS = new HashSet<>
            (Arrays.asList("text/xml; subtype=gml/3.1.1",
                    "text/xml; subtype=gml/3.2",
                    "application/gml+xml; version=3.2",
                    "application/json",
                    "text/xml"));
    private static Logger log = LogFactory.getLogger(WFSCapabilitiesService.class);

    protected static Set<String> getFormatsToStore(Set<String> formats) {
        return Sets.intersection(SUPPORTED_FEATURE_FORMATS, formats);
    }


}

