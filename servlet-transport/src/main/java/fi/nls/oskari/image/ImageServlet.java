package fi.nls.oskari.image;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.WFSLayerPermissionsStore;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.WFSProcess;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

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
    public static final String PARAM_TYPE = "type"; // "normal" | "highlight"
    public static final String PARAM_STYLE = "style";
    public static final String PARAM_SRS = "srs";
    public static final String PARAM_BBOX = "bbox";
    public static final String PARAM_ZOOM = "zoom";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_HEIGHT = "height";

    public static final String PARAM_FEATURE_IDS = "featureIds";

    public static final String DEFAULT_TYPE = "normal";
    public static final String DEFAULT_STYLE = "default";
    public static final String DEFAULT_SRS = "EPSG:3067";

    public static final String TYPE_HIGHLIGHT = "highlight";

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
        String type = ConversionHelper.getString(request.getParameter(PARAM_TYPE), DEFAULT_TYPE);

        String style;
        if(type.equals(TYPE_HIGHLIGHT)) {
            style = TYPE_HIGHLIGHT + "_" + session;
        } else {
            style = ConversionHelper.getString(request.getParameter(PARAM_STYLE), DEFAULT_STYLE);
            if(style.startsWith(WFSImage.PREFIX_CUSTOM_STYLE) || style.equals(TYPE_HIGHLIGHT)) {
                style += "_" + session;
            }
        }

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

        long width = ConversionHelper.getLong(request.getParameter(PARAM_WIDTH), 0);
        long height = ConversionHelper.getLong(request.getParameter(PARAM_HEIGHT), 0);

        List<String> featureIds = null;
        String featureIdsStr = ConversionHelper.getString(request.getParameter(PARAM_FEATURE_IDS), null);
        if(featureIdsStr != null) {
            featureIds = Arrays.asList(featureIdsStr.split(","));
        } else if(type.equals("highlight")) {
            log.warn("No highlight image could be drawn (featureIds missing)", session, layerId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No highlight image could be drawn (featureIds missing)");
            return;
        }

		log.debug("image", session, layerId, type, style, srs, bbox, zoom, width, height, featureIds);

		response.setContentType(CONTENT_TYPE);
		
	    // permissions
        if(!type.equals(TYPE_HIGHLIGHT)) {
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
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Sending image failed");
    		}
		} else {
            if(!type.equals(TYPE_HIGHLIGHT)) {
                log.warn("No image could be found from cache", session, layerId, srs, bbox, zoom);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No image could be found from cache");
                return;
            } else {
                try {
                    // create & send image
                    bufferedImage = WFSProcess.highlight(session, layerId, featureIds, bbox, srs, zoom, width, height);
                    OutputStream out = response.getOutputStream();
                    ImageIO.write(bufferedImage, FORMAT, out);
                    out.close();
                } catch (Exception e) {
                    log.error(e, "Sending image failed");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
                    return;
                }
            }
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

    private CoordinateReferenceSystem getCrs(String srs) {
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode(srs, true);
        } catch (FactoryException e) {
            log.error(e, "CRS decoding failed");
        }
        return crs;
    }
}