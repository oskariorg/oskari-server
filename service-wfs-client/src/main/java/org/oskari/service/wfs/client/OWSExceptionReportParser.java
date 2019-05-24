package org.oskari.service.wfs.client;

import java.io.InputStream;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fi.nls.oskari.util.XmlHelper;

public class OWSExceptionReportParser {

    public static OWSException parse(InputStream in) throws Exception {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        Element root = doc.getDocumentElement();
        if (!"ExceptionReport".equals(root.getLocalName())) {
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
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
                return Optional.of((Element) node);
            }
        }
        return Optional.empty();
    }

}
