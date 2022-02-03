package org.oskari.capabilities.ogc;

import org.oskari.capabilities.LayerCapabilities;
import java.util.Set;

import static org.oskari.capabilities.ogc.CapabilitiesConstants.*;

public class LayerCapabilitiesOGC extends LayerCapabilities {

    public LayerCapabilitiesOGC(String name, String title) {
        super(name, title);
    }

    public void setFormats(Set<String> formats) {
        addCapabilityData(FORMATS, formats);
    }

    public void setInfoFormats(Set<String> infoFormats) {
        addCapabilityData(INFO_FORMATS, infoFormats);
        // is there any point setting this?
        // isqueryable is NOT used for WMTS currently
        addCapabilityData(IS_QUERYABLE, !infoFormats.isEmpty());
    }

}
