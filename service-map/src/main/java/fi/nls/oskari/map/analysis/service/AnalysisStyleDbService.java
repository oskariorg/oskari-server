package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.service.ServiceException;

public interface AnalysisStyleDbService {

    public long insertAnalysisStyleRow(AnalysisStyle analysisStyle) throws ServiceException;

}
