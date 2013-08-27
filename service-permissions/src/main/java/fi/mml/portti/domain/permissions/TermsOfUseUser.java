package fi.mml.portti.domain.permissions;

import java.util.Date;

public class TermsOfUseUser implements Comparable<TermsOfUseUser> {
	
	int id;
	int userid;
	int termsOfUseLicenseId;
	Date update_time;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getTermsOfUseLicenseId() {
		return termsOfUseLicenseId;
	}
	public void setTermsOfUseLicenseId(int termsOfUseLicenseId) {
		this.termsOfUseLicenseId = termsOfUseLicenseId;
	}
	public Date getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}
	
	@Override
	public int compareTo(TermsOfUseUser arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
