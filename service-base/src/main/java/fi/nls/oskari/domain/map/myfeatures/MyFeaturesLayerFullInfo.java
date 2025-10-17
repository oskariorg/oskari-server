package fi.nls.oskari.domain.map.myfeatures;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;

public class MyFeaturesLayerFullInfo {

    private String id;
    private String type = "myf";
    private Instant created;
    private Instant updated;
    private int featureCount;
    // No actual model for these (yet)
    private JSONObject options;
    private JSONObject attributes;
    private Map<String, Map<String, Object>> locale;
    private List<MyFeaturesFieldInfo> layerFields;
    private String coverage;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public JSONObject getOptions() {
        return options;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public Map<String, Map<String, Object>> getLocale() {
        return locale;
    }

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public String getCoverage() {
        return coverage;
    }

    public static MyFeaturesLayerFullInfo from(MyFeaturesLayer layer) {
        MyFeaturesLayerFullInfo info = new MyFeaturesLayerFullInfo();
        info.id = info.type + "_" + layer.getId();
        info.created = layer.getCreated();
        info.updated = layer.getUpdated();
        info.featureCount = layer.getFeatureCount();
        info.options = layer.getOptions();
        info.attributes = layer.getAttributes();
        info.locale = layer.getLocale() == null ? null : layer.getLocale().keySet().stream()
                .collect(Collectors.toMap(lang -> lang, lang -> layer.getLocale().getJSONObject(lang).toMap()));
        info.layerFields = layer.getLayerFields();
        info.coverage = getCoverage(layer.getExtent());
        return info;
    }

    public static Polygon toGeometry(Envelope e) {
        if (e == null) {
            return null;
        }
        Coordinate[] cornerCoordinates = new Coordinate[] {
            new Coordinate(e.getMinX(), e.getMinY()),
            new Coordinate(e.getMinX(), e.getMaxY()),
            new Coordinate(e.getMaxX(), e.getMaxY()),
            new Coordinate(e.getMaxX(), e.getMinY()),
            new Coordinate(e.getMinX(), e.getMinY()),
        };
        return new GeometryFactory().createPolygon(cornerCoordinates);
    }

    private static String getCoverage(Envelope e) {
        if (e == null) {
            return null;
        }
        return new WKTWriter(2).write(toGeometry(e));
    }

}
