package org.oskari.print;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.request.PrintRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * PrintServlet handles POST requests made to /print/*
 */
public class PrintServlet extends HttpServlet {

    private static final Logger LOG = LogFactory.getLogger(PDF.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long serialVersionUID = 1L;

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        String method = req.getMethod();
        switch (method) {
        case "GET":
            doService(req, resp, true);
            break;
        case "POST":
            doService(req, resp, false);
            break;
        default:
            ResponseHelper.notImplemented(resp);
            break;
        }
    }

    private void doService(HttpServletRequest req, HttpServletResponse resp, boolean get) {
        try {
            PrintRequest pr = get ? parseRequestGET(req) : parseRequestPOST(req);
            PrintService.validate(pr);
            handle(pr, resp);
        } catch (IllegalArgumentException e) {
            ResponseHelper.badRequest(resp, e.getMessage());
        } catch (Exception e) {
            ResponseHelper.serverError(resp);
        }
    }

    private PrintRequest parseRequestGET(HttpServletRequest req)
            throws IllegalArgumentException, IOException {
        String json = req.getParameter("json");
        if (json == null || json.length() == 0) {
            throw new IllegalArgumentException("Missing 'json'");
        }
        try {
            return OBJECT_MAPPER.readValue(json, PrintRequest.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private PrintRequest parseRequestPOST(HttpServletRequest req)
            throws IllegalArgumentException, IOException {
        try (InputStream in = req.getInputStream()) {
            return OBJECT_MAPPER.readValue(in, PrintRequest.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void handle(PrintRequest pr, HttpServletResponse resp) {
        final String contentType = pr.getFormat();
        final PrintFormat format = PrintFormat.getByContentType(contentType);
        if (format != null) {
            if (format == PrintFormat.PDF) {
                handlePDF(pr, resp);
                return;
            } else if (format == PrintFormat.PNG) {
                handlePNG(pr, resp);
                return;
            }
        }
        ResponseHelper.badRequest(resp, "Invalid format!");
    }

    private void handlePNG(PrintRequest pr, HttpServletResponse resp) {
        try {
            BufferedImage bi = PrintService.getPNG(pr);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, PrintFormat.PNG.fileExtension, baos);
            ResponseHelper.ok(resp, PrintFormat.PNG.contentType, baos.toByteArray());
        } catch (IOException e) {
            LOG.warn(e);
            ResponseHelper.serverError(resp);
        }
    }

    private void handlePDF(PrintRequest pr, HttpServletResponse resp) {
        try (PDDocument doc = new PDDocument()) {
            PrintService.getPDF(pr, doc);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            ResponseHelper.ok(resp, PrintFormat.PDF.contentType, baos.toByteArray());
        } catch (IOException e) {
            LOG.warn(e);
            ResponseHelper.serverError(resp);
        }
    }

}
