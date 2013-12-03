package fi.mml.portti.service.db.permissions;


import fi.mml.portti.domain.permissions.TermsOfUseUser;
import fi.nls.oskari.service.db.BaseIbatisService;

public class TermsOfUseUserServiceIbatisImpl extends BaseIbatisService<TermsOfUseUser> implements TermsOfUseUserService {

	@Override
	protected String getNameSpace() {
		return "TermsOfUseUser";
	}

	

}
