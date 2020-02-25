package fi.nls.oskari.control.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceUnauthorizedException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.admin.LayerCapabilitiesHelper;
import org.oskari.maplayer.admin.LayerValidator;
import org.oskari.maplayer.model.ServiceCapabilitiesResult;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

@OskariActionRoute("ServiceCapabilities")
public class ServiceCapabilitiesHandler extends AbstractLayerAdminHandler {
    private static final String PARAM_CAPABILITIES_URL = "url";
    private static final String PARAM_CURRENT_SRS = "srs";
    private static final String PARAM_VERSION = "version";
    private static final String PARAM_USERNAME = "user";
    private static final String PARAM_PASSWORD = "pw";
    private static final String PARAM_TYPE = "type";
    private static final String ERROR_INVALID_FIELD_VALUE = "invalid_field_value";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Get layer for editing or list available layers for adding new layer
     *
     * Changes to LayerAdmin:
     * old -> this one
     * id (not sent) -> -1
     * is_base -> base_map
     * internal (not sent) -> boolean
     * parentid -1 -> (not sent)
     * capabilities_update_rate -> capabilities_update_rate_sec
     * dataprovider_id (not sent) -> 0
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        if (!userHasAddPermission(params.getUser())) {
            throw new ActionDeniedException("User doesn't have add layer permission");
        }
        //GetCapabilities
        try {
            ServiceCapabilitiesResult results = getLayersFromService(params);
            final String output = OBJECT_MAPPER.writeValueAsString(results);
            params.getResponse().setCharacterEncoding("UTF-8");
            params.getResponse().setContentType("application/json;charset=UTF-8");
            ResponseHelper.writeResponse(params, output);
        } catch (Exception e) {
            Throwable rootcause = getRootCause(e);
            if (rootcause instanceof ServiceUnauthorizedException) {
                ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
            } else if (rootcause instanceof SocketTimeoutException) {
                ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_REQUEST_TIMEOUT);
            } else if (rootcause instanceof IOException) {
                ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            } else if (rootcause instanceof XMLStreamException || rootcause instanceof ServiceException) {
                ResponseHelper.writeError(params, e.getMessage(), HttpServletResponse.SC_EXPECTATION_FAILED);
            } else {
                ResponseHelper.writeError(params, e.getMessage());
            }
        }
    }

    private ServiceCapabilitiesResult getLayersFromService(ActionParameters params) throws ServiceException, ActionException {
        final String url = LayerValidator.validateUrl(params.getRequiredParam(PARAM_CAPABILITIES_URL));
        final String type = params.getRequiredParam(PARAM_TYPE);
        final String version = params.getRequiredParam(PARAM_VERSION);
        final String username = params.getHttpParam(PARAM_USERNAME, "");
        final String password = params.getHttpParam(PARAM_PASSWORD, "");
        final String currentSrs = params.getHttpParam(PARAM_CURRENT_SRS, PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
        return LayerCapabilitiesHelper.getCapabilitiesResults(url, type, version, username, password, currentSrs);
    }

    private boolean isServiceUnauthrorizedException(Throwable t) {
        return getRootCause(t) instanceof ServiceUnauthorizedException;

    }

    private Throwable getRootCause(Throwable e) {
        if (e == null) {
            return null;
        }
        Throwable cause = e.getCause();
        if (e == cause || cause == null) {
            return e;
        }
        return getRootCause(cause);
    }
}
