package org.oskari.service.wfs3;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.wfs3.model.WFS3CollectionInfo;
import org.oskari.service.wfs3.model.WFS3Exception;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class WFS3Capabilities {
    public static final String VERSION_WFS3 = "3.0.0";

    public static JSONObject getWFSCapabilities(String url, String user, String pw, String currentCrs) throws WFS3Exception, IOException {
        WFS3Service service = WFS3Service.fromURL(url, user, pw);
        List<JSONObject> layers = service.getCollections().stream()
                .map(collectionInfo -> toOskariLayer(url, collectionInfo))
                .map(layer -> wfsLayerToJSON(layer, currentCrs, user, pw))
                .collect(Collectors.toList());
        return JSONHelper.createJSONObject("layers", new JSONArray(layers));
    }

    public static JSONObject getLayerCapabilities (OskariLayer ml) throws WFS3Exception, IOException, NoSuchElementException {
        WFS3Service service = WFS3Service.fromURL(ml.getUrl(), ml.getUsername(), ml.getPassword());
        WFS3CollectionInfo collection = service.getCollection(ml.getName()).get();
        return getLayerCapabilities(collection);
    }

    public static JSONObject getLayerCapabilities (WFS3CollectionInfo collection) {
        JSONObject capabilities = new JSONObject(); // override
        Set<String> epsgs = collection.getCrs()
                .stream()
                .map(WFS3Service::convertCrsToEpsg)
                .filter(epsg -> epsg != null)
                .collect(Collectors.toSet());
        List<String> types = collection.getLinks()
                .stream()
                .filter (link -> "item".equals(link.getRel()))
                .map(link -> link.getType())
                .filter (type -> type != null)
                .collect(Collectors.toList());
        JSONHelper.put(capabilities, "srs", new JSONArray(epsgs));
        JSONObject formats = JSONHelper.createJSONObject("available", new JSONArray(types));
        JSONHelper.put(capabilities, "srs", new JSONArray(epsgs));
        JSONHelper.putValue(capabilities, "formats", formats);
        return capabilities;
    }

    private static OskariLayer toOskariLayer(String url, WFS3CollectionInfo collection) {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setVersion(VERSION_WFS3);
        layer.setUrl(url);
        layer.setName(collection.getId());
        layer.setMaxScale(1d);
        layer.setMinScale(1500000d);
        layer.setCapabilities(getLayerCapabilities(collection));
        String title = collection.getTitle() != null ? collection.getTitle() : collection.getId();
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            layer.setName(lang, title);
        }

        return layer;
    }

    private static JSONObject wfsLayerToJSON(OskariLayer layer, String crs, String user, String pw) {
        LayerJSONFormatterWFS formatter = new LayerJSONFormatterWFS();
        String lang = PropertyUtil.getDefaultLanguage();
        JSONObject obj = formatter.getJSON(layer, lang, false, crs);
        OskariLayerWorker.modifyCommonFieldsForEditing(obj, layer);
        WFSLayerConfiguration lc = layerToWfs30LayerConfiguration(layer, crs, user, pw);
        JSONObject admin = JSONHelper.getJSONObject(obj, "admin");
        JSONHelper.putValue(admin, "passthrough", JSONHelper.createJSONObject(lc.getAsJSON()));
        // NOTE! Important to remove id since this is at template
        obj.remove("id");
        // Admin layer tools needs for listing layers
        JSONHelper.putValue(obj, "title", layer.getName());
        return obj;
    }

    private static WFSLayerConfiguration layerToWfs30LayerConfiguration (OskariLayer layer, String crs, String user, String pw) {
        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        // Use defaults for now, modify if needed
        String name = layer.getName();
        lc.setDefaults();
        lc.setURL(layer.getUrl());
        lc.setUsername(user);
        lc.setPassword(pw);
        lc.setLayerName(name); // or WFS3CollectionInfo getTitle()
        lc.setLayerId("layer_" + name);
        lc.setSRSName(crs);
        lc.setGMLGeometryProperty("geometry");
        lc.setWFSVersion(VERSION_WFS3);
        lc.setFeatureElement(name);
        lc.setFeatureNamespace("");
        lc.setFeatureNamespaceURI("");
        lc.setJobType("default");
        return lc;

    }
}
