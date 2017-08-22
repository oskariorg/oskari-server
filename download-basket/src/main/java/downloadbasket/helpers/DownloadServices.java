package downloadbasket.helpers;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import downloadbasket.data.ErrorReportDetails;
import downloadbasket.data.LoadZipDetails;
import org.apache.commons.mail.MultiPartEmail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Download services.
 *
 */
public class DownloadServices {
	public static final String WFS_GETCAPABILITIES_XML_JSON_VALUE = "WFSGetCapabilitiesXML";
	public static final String WFS_METADATA_URL_JSON_VALUE = "linkForMetadata";
	public static final String WFS_USED_URL = "url";
	public static final String WFS_FEATURETYPES = "featureTypes";
	private final Logger LOGGER = LogFactory.getLogger(DownloadServices.class);

	/**
	 * Default Constructor.
	 */
	public DownloadServices() {
	}

	/**
	 * Enum of Http methods.
	 */
	public enum HttpMetodes {
		POST("POST"), GET("GET");
		private final String stringValue;

		private HttpMetodes(final String s) {
			stringValue = s;
		}

		public String toString() {
			return stringValue;
		}
	}

	/**
	 * Load shape-ZIP from Geoserver.
	 * 
	 * @param ldz
	 *            load zip details
	 * @return filename file name
	 * @throws IOException
	 */
	public String loadZip(LoadZipDetails ldz) throws IOException {
		String realFileName = "";
		String returnFileName = "";
		OutputStreamWriter writer = null;
		HttpURLConnection conn = null;
		InputStream istream = null;
		OutputStream ostream = null;

		try {
			System.out.println(ldz.getWFSUrl());

			if (ldz.isDownloadNormalWay()) {
				System.out.println("Download normal way");
			} else {
				System.out.println("Download plugin way");
			}

			if (ldz.getGetFeatureInfoRequest().isEmpty()) {
				return null;
			}

			LOGGER.debug("WFS URL: " + ldz.getWFSUrl());
			LOGGER.debug("-- filtter: " + ldz.getGetFeatureInfoRequest());
			final URL url = new URL(ldz.getWFSUrl() + ldz.getGetFeatureInfoRequest());

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(600000);

			conn.connect();

			String filename = UUID.randomUUID().toString();
			istream = conn.getInputStream();

			String strTempDir = ldz.getTemporaryDirectory();

			File dir0 = new File(strTempDir);
			dir0.mkdirs();

			String slashType = "";
			String myFullFileName = dir0.getName();
			if (myFullFileName.lastIndexOf("\\") > 0) {
				slashType = "\\";
			} else {
				slashType = "/";
			}

			realFileName = strTempDir + slashType + filename + ".zip";

			returnFileName = strTempDir + slashType + filename + ".zip";
			ostream = new FileOutputStream(realFileName);

			final byte[] buffer = new byte[8 * 1024];

			while (true) {
				int len = istream.read(buffer);
				if (len <= 0) {
					break;
				}
				ostream.write(buffer, 0, len);
			}

			if (!isValid(new File(realFileName))) {
				ErrorReportDetails erd = new ErrorReportDetails();
				erd.setErrorFileLocation(realFileName);
				erd.setWfsUrl(ldz.getWFSUrl());
				erd.setXmlRequest(ldz.getGetFeatureInfoRequest());
				erd.setUserEmail(ldz.getUserEmail());
				erd.setLanguage(ldz.getLanguage());
				sendErrorReportToEmail(erd);
				sendErrorReportToUserEmail(erd);
			}

		} catch (Exception ex) {
			LOGGER.error(ex, "Error");
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
			if (istream != null) {
				istream.close();
			}
			if (ostream != null) {
				ostream.close();
			}
		}
		return returnFileName;
	}

	/**
	 * Check at is zipfile valid.
	 * 
	 * @param file
	 *            zip file
	 * @return
	 */
	static boolean isValid(final File file) {
		ZipFile zipfile = null;

		try {
			zipfile = new ZipFile(file);
			return true;
		} catch (ZipException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (zipfile != null) {
					zipfile.close();
					zipfile = null;
				}
			} catch (IOException e) {
			}
		}
	}

	/***
	 * Send error report to support email.
	 * 
	 * @param xmlRequest
	 *            xml request
	 * @param wfsUrl
	 *            wfs url
	 */

	/**
	 * Send error report to support email.
	 * 
	 * @param errorDetails
	 *            error report details
	 */
	private void sendErrorReportToEmail(ErrorReportDetails errorDetails) {
		try {
			String msg = "";
			msg += "<b>Tapahtui virhe WFS-latauksessa</b><br/><br/>WFS-url:<br/>" + errorDetails.getWfsUrl()
					+ "<br/><br/>"
					+ "WFS-pyyntö ja GeoServerin vastaus liitetiedostona.<br/><br/><b>HUOM!</b> Tämä on järjestelmän generoima virheilmoitus, älä vastaa tähän viestiin.";

			// Using Multipart because HtmlEmail doesn't handle attachments very
			// well.
			MultiPartEmail email = new MultiPartEmail();
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(msg, "text/html; charset=UTF-8");
			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			byte[] bytes = errorDetails.getXmlRequest().getBytes();

			DataSource dataSource = new ByteArrayDataSource(bytes, "application/xml");
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setDataHandler(new DataHandler(dataSource));
			bodyPart.setFileName("wfs_request.xml");
			multipart.addBodyPart(bodyPart);

			if (errorDetails.getErrorFileLocation() != null) {
				DataSource source = new FileDataSource(errorDetails.getErrorFileLocation());
				MimeBodyPart part = new MimeBodyPart();
				part.setDataHandler(new DataHandler(source));
				part.setFileName("geoserver_wfs_response.xml");
				multipart.addBodyPart(part);
			}

			email.setSmtpPort(Integer.parseInt(PropertyUtil.getNecessary(("hsy.wfs.download.smtp.port"))));
			email.setCharset("UTF-8");

			email.setContent(multipart);
			email.setHostName(PropertyUtil.getNecessary("hsy.wfs.download.smtp.host"));
			email.setFrom(PropertyUtil.getNecessary("hsy.wfs.download.email.from"));
			email.setSubject(PropertyUtil.getNecessary("hsy.wfs.download.error.report.subject"));
			email.addTo(PropertyUtil.getNecessary("hsy.wfs.download.error.report.support.email"));
			email.send();
		} catch (Exception ex) {
			LOGGER.error(ex, "Error: e-mail was not sent");
		}
	}

	/**
	 * Send error report to user email.
	 * 
	 * @param errorDetails
	 *            error report details
	 */
	private void sendErrorReportToUserEmail(ErrorReportDetails errorDetails) {
		try {
			String topic = PropertyUtil.getNecessary("hsy.wfs.download.email.error.user.topic");
			String msg = "<b>" + topic + "</b><br/><br/>"
					+ PropertyUtil.getNecessary("hsy.wfs.download.email.error.user.message") + "<br/><br/>"
					+ PropertyUtil.getNecessary("hsy.wfs.download.email.error.user.automatic");

			// Using Multipart because HtmlEmail doesn't handle attachments very
			// well.
			MultiPartEmail email = new MultiPartEmail();
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(msg, "text/html; charset=UTF-8");
			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			byte[] bytes = errorDetails.getXmlRequest().getBytes();

			DataSource dataSource = new ByteArrayDataSource(bytes, "application/xml");
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setDataHandler(new DataHandler(dataSource));

			email.setSmtpPort(Integer.parseInt(PropertyUtil.getNecessary("hsy.wfs.download.smtp.port")));
			email.setCharset("UTF-8");

			email.setContent(multipart);
			email.setHostName(PropertyUtil.getNecessary("hsy.wfs.download.smtp.host"));
			email.setFrom(PropertyUtil.getNecessary("hsy.wfs.download.email.from"));
			email.setSubject(topic);
			email.addTo(errorDetails.getUserEmail());
			email.send();
		} catch (Exception ex) {
			LOGGER.error("Cannot send error report to user", ex);
		}
	}

}