package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;

import java.util.Map;

/**
 * Used to instruct Jackson mapper to ignore some fields when serializing indicators to list
 */
abstract class JacksonIndicatorListMixin {
    @JsonIgnore
    abstract Map<String, String> getSource();
    @JsonIgnore
    abstract Map<String, String> getDescription();
    @JsonIgnore
    abstract StatisticalIndicatorDataModel getDataModel();
}
