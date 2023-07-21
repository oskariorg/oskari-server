package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * {
 *     "renderMode": "vector",
 *     "clusteringDistance": 10,
 *     "labelProperty": "name",
 *     // only for UserData layers
 *     "styles": {
 *          "default": {
 *              "featureStyle": {
 *                  // oskari style
 *              },
 *              "optionalStyles: []
 *          }
 *      },
 *      "hover": {
 *          featureStyle: {
 *              //oskari style
 *              "inherit": true,
 *         	    "effect": "darken"
 *          },
 *          "content": [
 *              { "key": "Feature Data"  }
 *          ]
 *      }
 * }
 */
public class WFSLayerOptions {
    private static final String KEY_RENDER_MODE = "renderMode";
    private static final String KEY_CLUSTER = "clusteringDistance";
    private static final String KEY_LABEL = "labelProperty";
    private static final String KEY_STYLES = "styles";
    private static final String KEY_DEFAULT_STYLE = "default";
    private static final String KEY_FEATURE_STYLE = "featureStyle";
    private static final String DEFAULT_RENDER_MODE = "vector";

    private JSONObject options;

    public WFSLayerOptions() {
        options = new JSONObject();
    }
    public WFSLayerOptions(JSONObject wfsOptions) {
        if(wfsOptions == null) {
            options = new JSONObject();
            return;
        }
        options = wfsOptions;
    }
    public JSONObject getOptions() {
        return options;
    }
    public void setOptions (JSONObject opts) {
        options = opts;
    }

    /*-- UserDataLayer related styles --*/
    public void injectBaseLayerOptions (JSONObject baseLayerOptions) {
        setOptions(JSONHelper.merge(baseLayerOptions, getOptions()));
        String labelProperty =  options.optString(KEY_LABEL);
        if (!labelProperty.isEmpty()) {
            JSONHelper.putValue(getDefaultFeatureStyle(), "text", getTextStyle(labelProperty));
        }
    }
    public void setDefaultFeatureStyle(JSONObject style) {
        JSONObject styles = JSONHelper.getJSONObject(options, KEY_STYLES);
        if (styles == null) {
            styles = new JSONObject();
            JSONHelper.putValue(options, KEY_STYLES, styles);
        }
        JSONObject defaultStyle = JSONHelper.getJSONObject(styles, KEY_DEFAULT_STYLE);
        if (defaultStyle == null) {
            defaultStyle = new JSONObject();
            JSONHelper.putValue(styles, KEY_DEFAULT_STYLE, defaultStyle);
        }
        JSONHelper.putValue(defaultStyle, KEY_FEATURE_STYLE, style);
    }
    public JSONObject getDefaultFeatureStyle () {
        JSONObject styles = JSONHelper.getJSONObject(options, KEY_STYLES);
        JSONObject defaultStyle = JSONHelper.getJSONObject(styles, KEY_DEFAULT_STYLE);
        JSONObject featureStyle =  JSONHelper.getJSONObject(defaultStyle, KEY_FEATURE_STYLE);
        if (featureStyle == null) {
            // all UserDataLayer should have one 'default' named style with featureStyle definitions
            return getDefaultOskariStyle();
        }
        return featureStyle;
    }
    /*-- Common --*/
    public void setProperty (String key, Object value) {
        JSONHelper.putValue(options, key, value);
    }

    public void setRenderMode (String renderMode) {
        JSONHelper.putValue(options, KEY_RENDER_MODE, renderMode);
    }
    public String getRenderMode () {
        return JSONHelper.optString(options, KEY_RENDER_MODE, DEFAULT_RENDER_MODE);
    }
    public int getClusteringDistance() {
        return options.optInt(KEY_CLUSTER, -1);
    }
    public void setClusteringDistance (int cluster) {
        JSONHelper.putValue(options, KEY_CLUSTER, cluster);
    }

    // fallback for VectorStyleService and UserDataLayer getDefaultFeatureStyle
    public static JSONObject getDefaultOskariStyle () {
        JSONObject json = new JSONObject();
        // dot
        JSONObject image = new JSONObject();
        JSONObject imageFill = new JSONObject();
        JSONHelper.putValue(imageFill, "color", "#FAEBD7");
        JSONHelper.putValue(image, "fill", imageFill);
        JSONHelper.putValue(image, "shape", 5);
        JSONHelper.putValue(image, "size", 3);
        JSONHelper.putValue(json, "image", image);
        // line
        JSONObject stroke = new JSONObject();
        JSONHelper.putValue(stroke, "color", "#000000");
        JSONHelper.putValue(stroke, "width",1);
        JSONHelper.putValue(stroke, "lineDash", "solid");
        JSONHelper.putValue(stroke, "lineCap", "round" );
        JSONHelper.putValue(stroke, "lineJoin", "round");
        // area
        JSONObject strokeArea = new JSONObject();
        JSONHelper.putValue(strokeArea, "color", "#000000");
        JSONHelper.putValue(strokeArea, "width", 1);
        JSONHelper.putValue(strokeArea, "lineDash", "solid");
        JSONHelper.putValue(strokeArea, "lineJoin", "round");
        JSONHelper.putValue(stroke, "area", strokeArea);
        JSONHelper.putValue(json, "stroke", stroke);
        JSONObject fill = new JSONObject();
        JSONHelper.putValue(fill, "color", "#FAEBD7");
        JSONObject fillArea = new JSONObject();
        JSONHelper.putValue(fillArea, "pattern", -1);
        JSONHelper.putValue(fill, "area", fillArea);
        JSONHelper.putValue(json, "fill", fill);
        return json;
    }
    public JSONObject getTextStyle(String labelProperty) {
        JSONObject text = new JSONObject();

        JSONObject textFill = new JSONObject();
        JSONHelper.putValue(textFill, "color", "#000000");
        JSONHelper.putValue(text, "fill", textFill);

        JSONObject textStroke = new JSONObject();
        JSONHelper.putValue(textStroke, "color", "#FFFFFF");
        JSONHelper.putValue(textStroke, "width", 1);
        JSONHelper.putValue(text, "stroke", textStroke);

        JSONHelper.putValue(text, "font", "bold 14px sans-serif");
        JSONHelper.putValue(text, "textAlign", "center");
        JSONHelper.putValue(text, "offsetX", 0);
        JSONHelper.putValue(text, "offsetY", 10);

        JSONHelper.putValue(text, "labelProperty", labelProperty);
        return text;
    }
}
