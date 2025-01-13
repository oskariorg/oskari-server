package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterANALYSIS;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

public class AnalysisDataService {
    private static final String ANALYSIS_BASELAYER_PROPERTY = "analysis.baselayer.id";
    private static final int ANALYSIS_BASELAYER_ID = PropertyUtil.getOptional(ANALYSIS_BASELAYER_PROPERTY, -1);

    private static final Logger log = LogFactory.getLogger(AnalysisDataService.class);

    private static final OskariLayerService mapLayerService = new OskariLayerServiceMybatisImpl();
    private static final LayerJSONFormatterANALYSIS FORMATTER = new LayerJSONFormatterANALYSIS();

    /**
     * Returns the base WFS-layer for analysis
     */
    public static OskariLayer getBaseLayer() {
        if (ANALYSIS_BASELAYER_ID == -1) {
            log.error("Analysis baseId not defined. Please define", ANALYSIS_BASELAYER_PROPERTY, "property with value pointing to the baselayer in database.");
            return null;
        }
        return mapLayerService.find(ANALYSIS_BASELAYER_ID);
    }

    public static JSONObject parseAnalysis2JSON(Analysis layer, String srs) {
        return parseAnalysis2JSON(layer, srs, PropertyUtil.getDefaultLanguage());
    }

    public static JSONObject parseAnalysis2JSON(final Analysis layer, final String srs, final String lang) {
        OskariLayer baseLayer = getBaseLayer();
        return FORMATTER.getJSON(baseLayer, layer, srs, lang);
    }
}
