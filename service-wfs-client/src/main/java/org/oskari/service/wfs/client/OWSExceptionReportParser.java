package org.oskari.service.wfs.client;

import java.io.InputStream;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.oskari.xml.XmlHelper;

public class OWSExceptionReportParser {

    public static OWSException parse(InputStream in) throws Exception {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        Element root = doc.getDocumentElement();

        if (!"ExceptionReport".equals(XmlHelper.getLocalName(root))) {
            throw new IllegalArgumentException("Invalid root element");
        }

        Element exception = getFirstChildElement(root, "Exception")
                .orElseThrow(() -> new IllegalArgumentException("Missing Exception element"));
        String exceptionCode = exception.getAttribute("exceptionCode");
        String locator = exception.getAttribute("locator");
        String exceptionText = getFirstChildElement(exception, "ExceptionText")
                .map(e -> e.getTextContent())
                .orElse("");

        return new OWSException(exceptionCode, locator, exceptionText);
    }

    private static Optional<Element> getFirstChildElement(Element parent, String localName) {
        return Optional.ofNullable(XmlHelper.getFirstChild(parent, localName));
    }

}
