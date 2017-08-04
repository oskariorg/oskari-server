package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("analysis")
public class UserContentAnalysisService extends UserContentService {

    private AnalysisDbServiceMybatisImpl analysisService = null;

    @Override
    public void init() {
        super.init();
        if(DatasourceHelper.isModuleEnabled(getName())) {
            analysisService = new AnalysisDbServiceMybatisImpl();
        }
    }

    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        analysisService.deleteAnalysisByUid(user.getUuid());
    }
}