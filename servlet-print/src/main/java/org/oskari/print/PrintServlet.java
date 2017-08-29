package org.oskari.print;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * PrintServlet handles POST requests made to /print/*
 */
public class PrintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintRequest printRequest = parseRequestGET(request);
            String validationError = PrintService.validate(printRequest);
            if (validationError != null) {
                ResponseHelper.badRequest(response, validationError);
            } else {
                handle(printRequest, response);
            }
        } catch (IllegalArgumentException e) {
            ResponseHelper.badRequest(response, e.getMessage());
        } catch (IOException e) {
            ResponseHelper.serverError(response);
        }
    }


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintRequest printRequest = parseRequestPOST(request);
            String validationError = PrintService.validate(printRequest);
            if (validationError != null) {
                ResponseHelper.badRequest(response, validationError);
            } else {
                handle(printRequest, response);
            }
        } catch (IllegalArgumentException e) {
            ResponseHelper.badRequest(response, e.getMessage());
        } catch (Exception e) {
            ResponseHelper.serverError(response);
        }
    }


    private PrintRequest parseRequestGET(HttpServletRequest request) 
            throws IllegalArgumentException, IOException {
        String json = request.getParameter("json");
        if (json == null || json.length() == 0) {
            throw new IllegalArgumentException("Missing 'json'");
        }
        try {
            return OBJECT_MAPPER.readValue(json, PrintRequest.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    private PrintRequest parseRequestPOST(HttpServletRequest request) 
            throws IllegalArgumentException, IOException {
        try (InputStream in = request.getInputStream()) {
            return OBJECT_MAPPER.readValue(in, PrintRequest.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    private void handle(PrintRequest printRequest, HttpServletResponse response) {
        final String contentType = printRequest.getFormat();
        final PrintFormat format = PrintFormat.getByContentType(contentType);
        if (format == null) {
            ResponseHelper.badRequest(response, "Invalid format!");
            return;
        }

        byte[] data;

        switch (format) {
        case PDF:
            data = PrintService.getPDF(printRequest);
            break;
        case PNG:
            data = PrintService.getPNG(printRequest, format.fileExtension);
            break;
        default:
            data = null;
        }

        if (data == null) {
            ResponseHelper.serverError(response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLength(data.length);
        response.setContentType(contentType);
        try (OutputStream out = response.getOutputStream()) {
            out.write(data);
        } catch (IOException e) {
            // Too late to do anything
            // Could log the Exception, but the response is already ruined...
        }
    }

}
