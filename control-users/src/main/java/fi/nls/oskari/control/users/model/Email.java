package fi.nls.oskari.control.users.model;

import java.sql.Timestamp;

import fi.nls.oskari.domain.User;

public class Email extends User {
		
	private Timestamp expiryTimestamp = null;

	public Timestamp getExpiryTimestamp() {
		return expiryTimestamp;
	}

	public void setExpiryTimestamp(Timestamp expiryTimestamp) {
		this.expiryTimestamp = expiryTimestamp;
	}
	
}
