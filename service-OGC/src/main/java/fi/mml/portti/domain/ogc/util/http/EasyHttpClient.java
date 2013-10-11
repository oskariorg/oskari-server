package fi.mml.portti.domain.ogc.util.http;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * General Http client that can be used to make http queries
 *
 */
public class EasyHttpClient {
	
	/** Logger */
	private static Logger log = LogFactory.getLogger(EasyHttpClient.class);
	
	private static String proxyHost;
	
	private static Integer proxyPort;

	/**
	 * Does an http post to given endpoint
	 * 
	 * @param endpoint url
	 * @param username username, can be empty
	 * @param password password, can be empty
	 * @param postData data to be set as payload
	 * @return HttpPostResponse
	 */
	public static HttpPostResponse post(String endpoint, String username, String password, String postData, boolean useProxy) {
						
		if (!endpoint.toLowerCase().startsWith("http")) {
			throw new RuntimeException("EasyHttpClient cannot handle URIs that start with anything else than 'http'. You are trying to get data from '" + endpoint + "'");
		}
		
		HttpClient client = new HttpClient();
		if (useProxy) {
			/* Use proxy if host is defined */
			String host = getProxyHost();
			Integer port = getProxyPort();
			if (host != null && !host.equals("")) {
				client.getHostConfiguration().setProxy(host, port);
			}
		}
		
		/* Use authentication only if username is provided */
		if (username != null && !username.equals("")) {
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			client.getState().setCredentials(AuthScope.ANY, credentials);
		}
		
		PostMethod postMethod = new PostMethod(endpoint);
		
		/* Set payload, if available */
		if (postData != null) {
            try {
			    RequestEntity entity = new StringRequestEntity(postData, null, null);
			    postMethod.setRequestEntity(entity);
            } catch(UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
		}
		 
		HttpPostResponse response = new HttpPostResponse();
		
		try {
			int responseCode = client.executeMethod(postMethod);
			response.setResponseCode(responseCode);
			response.setResponse(postMethod.getResponseBodyAsStream());			
		} catch (HttpException e) {
			response.setResponseCode(-1);
		} catch (IOException e) {
			response.setResponseCode(-1);
		}	
		
		return response;
	}

	private static int getProxyPort() {
		if (proxyPort != null) {
			return proxyPort;
		}

		proxyPort = ConversionHelper.getInt(PropertyUtil.getOptional("easyhttpclient.proxy.port"), -1);
		return proxyPort;
	}

	private static String getProxyHost() {
		if (proxyHost != null) {
			return proxyHost;
		}

		proxyHost = PropertyUtil.getOptional("easyhttpclient.proxy.host");
		return proxyHost;
	}
	
	public static void setProxyPort(int port) {
		proxyPort = port;
	}
	
	public static void setProxyHost(String host) {
		proxyHost = host;
	}
	
}
