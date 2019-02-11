package org.oskari.service.wfs3.client;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.geotools.geojson.feature.FeatureCollectionHandler;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Modified FeatureCollectionHandler implementation that can handle "links"
 * object found in WFS 3 GeoJSON responses
 */
public class WFS3FeatureCollectionHandler extends FeatureCollectionHandler {

    private List<WFS3Link> links;
    int level = 0;

    public WFS3FeatureCollectionHandler(SimpleFeatureType sft) {
        super(sft, null);
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (level == 1 && "links".equals(key)) {
            delegate = new WFS3LinksHandler();
            return true;
        }
        return super.startObjectEntry(key);
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        level++;
        return super.startObject();
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        boolean b = super.endObject();
        level--;
        if (level == 0 && delegate instanceof WFS3LinksHandler) {
            WFS3LinksHandler linksHandler = (WFS3LinksHandler) delegate;
            links = linksHandler.getValue();
            delegate = NULL;
            return true;
        }
        return b;
    }

    public List<WFS3Link> getLinks() {
        return links == null ? Collections.emptyList() : links;
    }

}
