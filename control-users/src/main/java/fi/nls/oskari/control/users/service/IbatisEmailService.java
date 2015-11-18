package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.service.db.BaseIbatisService;

public class IbatisEmailService  extends BaseIbatisService<Email>{

	@Override
	protected String getNameSpace() {
		return "Emails";
	}

	public Long addEmail(Email email) {
        return queryForObject(getNameSpace() + ".addEmail", email);
    }
}
