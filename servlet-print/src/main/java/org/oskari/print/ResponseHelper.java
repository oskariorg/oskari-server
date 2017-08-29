package org.oskari.print;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Collection of utility functions for HttpServletResponse
 */
public class ResponseHelper {
    
    public static final String PLAIN_TEXT = "text/plain";
    
    private static final byte[] BAD_REQUEST = "Bad Request!".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NOT_FOUND = "Not found!".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SERVER_ERROR = "Server error!".getBytes(StandardCharsets.US_ASCII);
    
    public static void ok(HttpServletResponse resp, String contentType, byte[] body) {
    	write(resp, HttpServletResponse.SC_OK, contentType, body);
    }
    
    public static void badRequest(HttpServletResponse resp) {
        write(resp, HttpServletResponse.SC_BAD_REQUEST, PLAIN_TEXT, BAD_REQUEST);
    }
    
    public static void badRequest(HttpServletResponse resp, String msg) {
    	byte[] body = msg != null ? msg.getBytes(StandardCharsets.UTF_8) : BAD_REQUEST;
        write(resp, HttpServletResponse.SC_BAD_REQUEST, PLAIN_TEXT, body);
    }
    
    public static void notFound(HttpServletResponse resp) {
        write(resp, HttpServletResponse.SC_NOT_FOUND, PLAIN_TEXT, NOT_FOUND);
    }
    
    public static void notFound(HttpServletResponse resp, String msg) {
    	byte[] body = msg != null ? msg.getBytes(StandardCharsets.UTF_8) : NOT_FOUND;
        write(resp, HttpServletResponse.SC_NOT_FOUND, PLAIN_TEXT, body);
    }
    
    public static void serverError(HttpServletResponse resp) {
        write(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PLAIN_TEXT, SERVER_ERROR);
    }
    
    public static void serverError(HttpServletResponse resp, String msg) {
    	byte[] body = msg != null ? msg.getBytes(StandardCharsets.UTF_8) : SERVER_ERROR;
        write(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, PLAIN_TEXT, body);
    }

    public static void write(HttpServletResponse resp, int sc, String contentType, byte[] body) {
        resp.setStatus(sc);
        resp.setContentType(contentType);
        resp.setContentLength(body.length);
        try (OutputStream out = resp.getOutputStream()) {
            out.write(body);
        } catch (IOException ignore) {
        	// Not much to do ...
        }
    }

}
