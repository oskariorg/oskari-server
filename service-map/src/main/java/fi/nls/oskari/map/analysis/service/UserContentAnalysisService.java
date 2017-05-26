package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentAnalysisService extends UserContentService {

    private AnalysisDbServiceIbatisImpl analysisService = new AnalysisDbServiceIbatisImpl();

    public void deleteUserContent(User user) throws ServiceException {
        analysisService.deleteAnalysisByUid(user.getUuid());
    }
}