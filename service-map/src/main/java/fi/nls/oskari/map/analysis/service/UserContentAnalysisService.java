package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.user.User;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("analysis")
public class UserContentAnalysisService extends UserContentService {

    private AnalysisDbService analysisService;

    @Override
    public void init() {
        super.init();
        if(DatasourceHelper.isModuleEnabled(getName())) {
            analysisService = OskariComponentManager.getComponentOfType(AnalysisDbService.class);
        }
    }

    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        analysisService.deleteAnalysisByUid(user.getUuid());
    }
}