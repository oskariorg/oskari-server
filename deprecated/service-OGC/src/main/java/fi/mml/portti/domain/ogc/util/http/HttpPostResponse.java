package fi.mml.portti.domain.ogc.util.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class HttpPostResponse {

	private int responseCode;

	private InputStream response;

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}	

	public void setResponse(InputStream response) {
		this.response = response;
	}

	public InputStream getResponseAsInputStream() {
		return response;
	}

	public boolean wasSuccessful() {
		return (responseCode == 200);
	}

	public boolean wasUnauthorized() {
		return (responseCode == 401 || responseCode == 403);
	}

	public boolean wasServiceDown() {
		if (responseCode == 401 || responseCode == 403 || responseCode == 200) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Note this should not be used when performance is an issue
	 * 
	 * @return
	 */
	public String getResponseAsString() {
		if (response == null) {
			return "Response is null, response code: " + responseCode;
		}
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len;
		    int size = 1024;
		    byte[] buf = new byte[size];
		    while ((len = response.read(buf, 0, size)) != -1) {
		    	bos.write(buf, 0, len);
		    }
		    return bos.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Closes response stream
	 */
	public void closeResponseStream() {
		if (this.response == null) {
			return;
		}
		
		try {
			this.response.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}



}
