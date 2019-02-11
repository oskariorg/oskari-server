package org.oskari.service.wfs3.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geojson.HandlerBase;
import org.geotools.geojson.IContentHandler;
import org.json.simple.parser.ParseException;

public class WFS3LinksHandler extends HandlerBase implements IContentHandler<List<WFS3Link>> {

    private final List<WFS3Link> links = new ArrayList<>();
    private final WFS3LinkBuilder builder = new WFS3LinkBuilder();
    private String key;

    public boolean startObject() throws ParseException, IOException {
        builder.clear();
        return true;
    }

    public boolean endObject() throws ParseException, IOException {
        links.add(builder.build());
        return true;
    }

    public boolean startObjectEntry(String key) throws ParseException, IOException {
        this.key = key;
        return true;
    }

    public boolean primitive(Object value) throws ParseException, IOException {
        if (key == null) {
            throw new IOException("Expected key!");
        }
        String str = value == null ? null : value.toString();
        builder.set(key, str);
        key = null;
        return true;
    }

    @Override
    public List<WFS3Link> getValue() {
        return links;
    }

}
