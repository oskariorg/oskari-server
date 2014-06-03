package fi.nls.oskari.wfs.extension;

/**
 * WFS geotools filters for Analysis
 * <p/>
 * Gives out filter as XML for WFS requests.
 */
public class AnalysisFilter extends AdditionalIdFilter {
    public static final String ANALYSIS_BASE_LAYER_ID = "analysis.baselayer.id";
    public static final String ANALYSIS_PREFIX = "analysis_";
    public static final String ANALYSIS_ID_FIELD = "analysis_id";
    public static final String ANALYSIS_UID_FIELD = "uuid";

    public AnalysisFilter() {
        super(ANALYSIS_PREFIX, ANALYSIS_ID_FIELD, ANALYSIS_UID_FIELD);
    }

    /**
     * Defines a radius factor of point sizes for filtering
     *
     * @return factor
     */
    @Override
    public double getSizeFactor() {
        return 5.0;
    }

}