package org.oskari.service.wfs.client.geojson;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.oskari.service.wfs.client.WFS3Link;

class MapToWFS3Links {

    @SuppressWarnings("unchecked")
    static List<WFS3Link> tryConvertToLinks(List<Object> arrayOflinks) {
        return arrayOflinks.stream()
                .map(obj -> (Map<String, String>) obj)
                .map(MapToWFS3Links::mapToLink)
                .collect(Collectors.toList());
    }

    static WFS3Link mapToLink(Map<String, String> map) {
        String href = map.get("href");
        String rel = map.get("rel");
        String type = map.get("type");
        String hreflang = map.get("hreflang");
        String title = map.get("title");
        return new WFS3Link(href, rel, type, hreflang, title);
    }

}
