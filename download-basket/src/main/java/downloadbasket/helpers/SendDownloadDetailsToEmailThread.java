package downloadbasket.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBException;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import downloadbasket.data.LoadZipDetails;
import downloadbasket.data.NormalWayDownloads;
import downloadbasket.data.ZipDownloadDetails;
import org.apache.commons.mail.HtmlEmail;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.mail.smtp.SMTPTransport;

/**
 * Send download details email service (thread).
 */
public class SendDownloadDetailsToEmailThread extends Thread{
	JSONArray downLoadDetails;
	JSONObject userDetails;
	String language = "fi";

	private final Logger LOGGER = LogFactory.getLogger(SendDownloadDetailsToEmailThread.class);
    private final String PARAM_CROPPING_MODE = "croppingMode";
    private final String PARAM_CROPPING_LAYER = "croppingLayer";
    private final String PARAM_LAYER = "layer";
    private final String PARAM_WMS_URL = "wmsUrl";
	
	/**
	 * Constructor.
	 * @param downLoadDetails download details
	 * @param userDetails user details
	 */
	public SendDownloadDetailsToEmailThread(JSONArray downLoadDetails, JSONObject userDetails){
		this.downLoadDetails = downLoadDetails;
		this.userDetails = userDetails;
	}
	
	/**
	 * 
	 * Overrides the run method.
	 * Collects the download materials and sends them using the variables given in constructor.
	 * 
	 */
    @Override
    public void run() {
    	
    	try {
			DownloadServices ds = new DownloadServices();
			ArrayList<ZipDownloadDetails> mergeThese = new ArrayList<ZipDownloadDetails>();
			final String strTempDir = PropertyUtil.get("hsy.wfs.download.folder.name");
			String normalWayDownload = PropertyUtil.get("hsy.wfs.download.normal.way.downloads");
			String[] temp = normalWayDownload.split(",");
			NormalWayDownloads normalDownloads = new NormalWayDownloads();
			for (int i = 0; i < temp.length; i++) {
				normalDownloads.addDownload(temp[i]);
			}

			for(int i=0;i<downLoadDetails.length();i++){
				JSONObject download = downLoadDetails.getJSONObject(i);
                final String croppingMode = download.getString(PARAM_CROPPING_MODE);
                String croppingLayer = "";
                if(download.has(PARAM_CROPPING_LAYER)){
                	croppingLayer = download.getString(PARAM_CROPPING_LAYER);
                }

                LoadZipDetails ldz = new LoadZipDetails();
                ldz.setTemporaryDirectory(strTempDir);
                ldz.setUserEmail(userDetails.getString("email"));
                ldz.setLanguage(this.language);
                ldz.setDownloadNormalWay(normalDownloads.isNormalWayDownload(croppingMode, croppingLayer));

                if(ldz.isDownloadNormalWay()) {

                	OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
            		OskariLayer oskariLayer = mapLayerService.find(download.getString(PARAM_WMS_URL));
            		String wfsUrl = "";
            		
            		if(oskariLayer != null){
            			wfsUrl = oskariLayer.getUrl();
            		}

                    ldz.setGetFeatureInfoRequest(OGCServices.getFilter(download, true));
                    ldz.setWFSUrl(OGCServices.doGetFeatureUrl(wfsUrl, download, false));
                } else {

                    ldz.setGetFeatureInfoRequest(OGCServices.getPluginFilter(download, true, true));
                    ldz.setWFSUrl(OGCServices.doGetFeatureUrl(PropertyUtil.get("hsy.wfs.service.url"), download, true));
                }

                final String fileLocation = ds.loadZip(ldz);
				
				//LOGITUS POISTETTU
				
		        if(fileLocation!=null) {		        
					ZipDownloadDetails zdd = new ZipDownloadDetails();
					zdd.setFileName(fileLocation);
					final String sLayer = Helpers.getLayerNameWithoutNameSpace(download.getString(PARAM_LAYER));
					zdd.setLayerName(sLayer);
					mergeThese.add(zdd);
		        }
			}

			
			ZipOutputStream out = null;
			String strZipFileName = UUID.randomUUID().toString() + ".zip";
			try {							
				File f = new File(strTempDir);
				f.mkdirs();
				out = new ZipOutputStream(new FileOutputStream(strTempDir + "/" + strZipFileName));
				
				Hashtable<String, Integer> indeksit = new Hashtable<String, Integer>(); 
				byte[] buffer = new byte[1024];
				
				for(int i=0;i<mergeThese.size();i++){
					ZipInputStream in = null;
					
					try{						
						ZipDownloadDetails zdd = mergeThese.get(i);
                        String strTempFile = zdd.getFileName();

                        Integer index = indeksit.get(zdd.getLayerName());
                        if(index==null){
                            index = 0;
                        } else {
                            index++;
                            indeksit.remove(zdd.getLayerName());
                        }

                        indeksit.put(zdd.getLayerName(), index);

                        String folderName = zdd.getLayerName() + "_"+index+"/";
                        out.putNextEntry(new ZipEntry(folderName));

                        in = new ZipInputStream(new FileInputStream(strTempFile));
                        ZipEntry ze = in.getNextEntry();
                        while(ze!=null){
                            String fileName = ze.getName();
                            out.putNextEntry(new ZipEntry(folderName+fileName));
                            int len;
                            while ((len = in.read(buffer)) > 0) {
                                out.write(buffer, 0, len);
                            }
                            ze = in.getNextEntry();
                        }

                        out.closeEntry();
                        in.close();
                        deleteFile(strTempFile);
					} catch (Exception ex) {
                        LOGGER.error("Cannot  parse JSON download details", ex);
					} finally{
						if(in!=null) in.close();
					}
				}
				
				
			} catch(FileNotFoundException fe){
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				if (out != null) {
					try {
						out.close();
					}
					catch (Exception ex) {
                        LOGGER.error("Cannot close output", ex);
                    }
				}
			}
			sendZipFile(strZipFileName);
    	}
    	catch (Exception ex) {
			LOGGER.error("Cannot download shape zip.", ex);
    	}
    }
    
    /**
	 * 
	 * Sends the zip file to current user's email address.
	 * @param strZipFileName zip file name
	 */
	public void sendZipFile(final String strZipFileName) {

		try {
			HtmlEmail email = new HtmlEmail();
			
			int smtpPort = Integer.parseInt(PropertyUtil.getNecessary("hsy.wfs.download.smtp.port"));
			email.setSmtpPort(smtpPort);
			email.setHostName(PropertyUtil.getNecessary("hsy.wfs.download.smtp.host"));
			email.setFrom(PropertyUtil.getNecessary("hsy.wfs.download.email.from"));
			email.setSubject(PropertyUtil.getNecessary("hsy.wfs.download.email.subject"));
			email.setCharset("UTF-8");			
			
			StringBuilder htmlHeader = new StringBuilder();
			StringBuilder htmlMsg = new StringBuilder();
			StringBuilder htmlFooter = new StringBuilder();
			
			StringBuilder txtHeader = new StringBuilder();
			StringBuilder txtMsg = new StringBuilder();
			StringBuilder txtFooter = new StringBuilder();
			
			
			htmlHeader.append(PropertyUtil.getNecessary("hsy.wfs.download.email.header"));
			txtHeader.append(PropertyUtil.getNecessary("hsy.wfs.download.email.header"));

			htmlHeader.append("<br/><br/>");
			txtHeader.append("\n\n");
			htmlMsg.append(PropertyUtil.getNecessary("hsy.wfs.download.email.message"));
    		txtMsg.append(PropertyUtil.getNecessary("hsy.wfs.download.email.message"));

			htmlMsg.append("<br/>");
			txtMsg.append("\n");

            String url = PropertyUtil.getNecessary("hsy.wfs.download.link.url.prefix")+strZipFileName;
			htmlMsg.append("<a href=\"" + url +"\">"+url+"</a>");
			txtMsg.append(url);

            htmlFooter.append("<br/><br/>");
            txtFooter.append("\n\n");
            String f = PropertyUtil.get("hsy.wfs.download.email.footer","");
            String ff = f.replaceAll("\\{RIVINVAIHTO\\}", "\n");
            f = f.replaceAll("\\{RIVINVAIHTO\\}", "<br/>");
            htmlFooter.append(f);
            txtFooter.append(ff);
            String d = PropertyUtil.get("hsy.wfs.download.email.message.datadescription","");
					String dd =  d.replaceAll("\\{RIVINVAIHTO\\}", "\n");
            d = d.replaceAll("\\{RIVINVAIHTO\\}", "<br/>");
            htmlFooter.append(d);
            txtFooter.append(dd);
            htmlFooter.append(PropertyUtil.get("hsy.wfs.download.email.datadescription_link",""));
            txtFooter.append(PropertyUtil.get("hsy.wfs.download.email.datadescription_link",""));

			String htmlFullMessage = "<html>" + htmlHeader.toString() 
					+ htmlMsg.toString() 
					+ htmlFooter.toString() 
			+ "</html>";
			
			String txtFullMessage = txtHeader.toString() 
					+ txtMsg.toString() 
					+ txtFooter.toString();
			
			email.setHtmlMsg(htmlFullMessage);
			email.setTextMsg(txtFullMessage);			
			email.addTo(userDetails.getString("email"));			
			email.send();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
    
	/**
	 * Delete temp files.
	 * @param strFilePath temp path
	 */
    private void deleteFile(String strFilePath) {

		File f = new File(strFilePath);
		if (f.exists() && f.canWrite()) {
			f.delete();
		}
	}

}
