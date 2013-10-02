package fi.nls.oskari.image;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.WFSLayerPermissionsStore;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wfs.WFSImage;

/**
 * Serves images from cache (also temp)
 */
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private static final Logger log = LogFactory.getLogger(ImageServlet.class);

    public static final String ENCODING = "utf8";
    public static final String CONTENT_TYPE = "image/png";
    public static final String FORMAT = "png";

    public static final String PARAM_SESSION = "session";
    public static final String PARAM_LAYER_ID = "layerId";
    public static final String PARAM_STYLE = "style";
    public static final String PARAM_SRS = "srs";
    public static final String PARAM_BBOX = "bbox";
    public static final String PARAM_ZOOM = "zoom";

    public static final String DEFAULT_STYLE = "default";
    public static final String DEFAULT_SRS = "EPSG:3067";

    /**
     * Route for getting an image (tile or map)
     * 
     * Uses WFSLayerPermissionsStore to check the user's layer permissions with session. 
     * Gets the image from cache with given request parameters.
     * 
     * @param request
     * @param response
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// params
		request.setCharacterEncoding(ENCODING);
	    String session = ConversionHelper.getString(request.getParameter(PARAM_SESSION), null);

	    String layerId = request.getParameter(PARAM_LAYER_ID);
        String style = ConversionHelper.getString(request.getParameter(PARAM_STYLE), DEFAULT_STYLE);

	    String srs = ConversionHelper.getString(request.getParameter(PARAM_SRS), DEFAULT_SRS);

	    Double[] bbox = new Double[4];
        String bboxstr = ConversionHelper.getString(request.getParameter(PARAM_BBOX), null);
        if(bboxstr != null) {
            String[] bboxarr = bboxstr.split(",");
            for(int i = 0; i < bboxarr.length; i++) {
                bbox[i] =  ConversionHelper.getDouble(bboxarr[i], 0);
            }
        } else {
            log.warn("No image could be found from cache (bbox missing)", session, layerId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No image could be found from cache (bbox missing)");
            return;
        }

	    long zoom = ConversionHelper.getLong(request.getParameter(PARAM_ZOOM), 0);

		log.debug("image", session, layerId, srs, bbox, zoom);

		response.setContentType(CONTENT_TYPE);
		
	    // permissions
    	try {
			if(!isPermission(session, layerId)) {
				log.warn("No permissions for user (" + session + ") on layer " + layerId);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
				return;
			}
		} catch (Exception e) {
			log.error(e, "Permission parsing failed for user (" + session + ") on layer " + layerId);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
			return;
		}
		
		// get image from cache (persistant)
		BufferedImage bufferedImage = WFSImage.getCache(layerId, style, srs, bbox, zoom);
		if(bufferedImage == null) { // check temp cache
			bufferedImage = WFSImage.getCache(layerId, style, srs, bbox, zoom, false);
		}
		
		if(bufferedImage != null) {
			// send image
    		try {
				OutputStream out = response.getOutputStream();
				ImageIO.write(bufferedImage, FORMAT, out);
				out.close();
    		} catch (Exception e) {
	    		log.error(e, "Sending image failed");
    		}
			return;
		} else {
    		log.warn("No image could be found from cache", session, layerId, srs, bbox, zoom);
    		response.sendError(HttpServletResponse.SC_NOT_FOUND, "No image could be found from cache");
    		return;
		}
	}
	
	/**
	 * Checks if user has permissions for a layer
	 * 
	 * @param session
	 * @param layerId
	 * @return <code>true</code> if user may use the layer; <code>false</code>
	 *         otherwise.
	 * @throws IOException 
	 */
	private boolean isPermission(String session, String layerId) throws IOException {
    	String json = WFSLayerPermissionsStore.getCache(session);
    	WFSLayerPermissionsStore permissions;
    	if(json != null) {
	    	permissions = WFSLayerPermissionsStore.setJSON(json);
			return permissions.isPermission(layerId);
    	}
    	return false;
	}
}