package fi.nls.oskari.map.analysis.service;


import fi.nls.oskari.domain.map.analysis.AnalysisStyle;

public interface AnalysisStyleDbService {
        long insertAnalysisStyleRow(final AnalysisStyle analysisStyle);
}
