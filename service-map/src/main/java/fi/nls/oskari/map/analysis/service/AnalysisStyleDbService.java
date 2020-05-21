package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.service.ServiceException;

public interface AnalysisStyleDbService {

    public long insertAnalysisStyleRow(UserDataStyle analysisStyle) throws ServiceException;

}
