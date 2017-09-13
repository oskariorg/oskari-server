package fi.nls.oskari.control.users.model;

import fi.nls.oskari.domain.User;

import java.sql.Timestamp;
import java.util.Date;

public class EmailToken extends User {
		
	private Timestamp expiryTimestamp = null;
	private String password = null;

	public Timestamp getExpiryTimestamp() {
		return expiryTimestamp;
	}

	public void setExpiryTimestamp(Timestamp expiryTimestamp) {
		this.expiryTimestamp = expiryTimestamp;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean hasExpired() {
		return expiryTimestamp == null || new Date().after(expiryTimestamp);
	}
	
}
