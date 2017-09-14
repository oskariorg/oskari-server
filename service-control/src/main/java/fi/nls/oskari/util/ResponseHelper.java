package fi.nls.oskari.util;

import java.io.ByteArrayOutputStream;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Convenience methods for writing a response.
 */
public class ResponseHelper {

    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";

    private static final Logger LOG = LogFactory.getLogger(ResponseHelper.class);

    /**
     * Writes out the given response
     *
     * @param params   reference to params to get the writer
     * @param response response to write
     */
    public static final void writeResponse(ActionParameters params, final Object response) {
        try {
            if(response instanceof JSONObject || response instanceof JSONArray) {
                params.getResponse().setCharacterEncoding("UTF-8");
                params.getResponse().setContentType("application/json;charset=UTF-8");
            }
            params.getResponse().getWriter().print(response);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Writes out the given response as JSON
     *
     * @param params reference to ActionParams
     * @param sc HTTP Status Code to send
     * @param json JSONObject to send
     */
    public static final void writeResponse(ActionParameters params, int sc, JSONObject json) {
        final byte[] b = json.toString().getBytes(StandardCharsets.UTF_8);
        writeResponse(params, sc, CONTENT_TYPE_JSON_UTF8, b);
    }

    /**
     * Writes out the given response
     *
     * @param params reference to ActionParams
     * @param sc HTTP Status Code to send
     * @param contentType of the response
     * @param b byte array containing the response body
     */
    public static final void writeResponse(ActionParameters params, int sc, String contentType, byte[] b) {
        final int len = b.length;
        final HttpServletResponse resp = params.getResponse();
        resp.setStatus(sc);
        resp.setContentType(contentType);
        resp.setContentLength(len);
        try (OutputStream out = resp.getOutputStream()) {
            out.write(b, 0, len);
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    /**
     * Writes out the given response
     * This method avoids creating a copy of the byte array if it is
     * buffered to a ByteArrayOutputStream
     *
     * @param params reference to ActionParams
     * @param sc HTTP Status Code to send
     * @param contentType of the response
     * @param baos ByteArrayOutputStream containing the response body
     */
    public static final void writeResponse(ActionParameters params, int sc, String contentType,
            ByteArrayOutputStream baos) {
        final int len = baos.size();
        final HttpServletResponse resp = params.getResponse();
        resp.setStatus(sc);
        resp.setContentType(contentType);
        resp.setContentLength(len);
        try (OutputStream out = resp.getOutputStream()) {
            baos.writeTo(out);
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    /**
     * Writes a generic error message as response with error code 500
     *
     * @param params reference to params to get the writer
     */
    public static final void writeError(ActionParameters params) {
        writeError(params, "generic", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Writes the given error message as response with error code 500
     *
     * @param params  reference to params to get the writer
     * @param message error message
     */
    public static final void writeError(ActionParameters params, final String message) {
        writeError(params, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Writes the given error message as response with the given error code
     *
     * @param params    reference to params to get the writer
     * @param message   error message
     * @param errorCode
     */
    public static final void writeError(ActionParameters params, final String message, final int errorCode) {
        writeError(params, message, errorCode, null);
    }

    /**
     * Writes the given error message as response with the given error code and optional info about the error
     *
     * @param params    reference to params to get the writer
     * @param message   error message
     * @param errorCode
     * @param options   JSON written as part of the error message to provide more info
     */
    public static final void writeError(ActionParameters params, final String message, final int errorCode, final JSONObject options) {
        try {
            final JSONObject error = new JSONObject();
            JSONHelper.putValue(error, "error", message);
            JSONHelper.putValue(error, "info", options);

            params.getResponse().setContentType("application/json;charset=utf-8");
            params.getResponse().setStatus(errorCode);
            params.getResponse().getWriter().print(error.toString());
            // tomcat catches sendError and writes an error page with the given message
            // we dont want that 
            //params.getResponse().sendError(errorCode, error.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
