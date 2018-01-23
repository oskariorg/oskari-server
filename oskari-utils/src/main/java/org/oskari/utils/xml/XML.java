package org.oskari.utils.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XML {

    public static Document readDocument(InputStream in) throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        in.close();
        return doc;
    }

    public static Optional<Element> getChild(Element e, String localName) {
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
                return Optional.of((Element) node);
            }
            node = node.getNextSibling();
        }
        return Optional.empty();
    }

    public static List<Element> getChildren(Element e, String localName) {
        List<Element> list = new ArrayList<>();
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
                list.add((Element) node);
            }
            node = node.getNextSibling();
        }
        return list;
    }

    public static List<Element> getChildren(Element e) {
        List<Element> list = new ArrayList<>();
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                list.add((Element) node);
            }
            node = node.getNextSibling();
        }
        return list;
    }

    public static Optional<Element> getFirstChildElement(Element e) {
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return Optional.of((Element) node);
            }
            node = node.getNextSibling();
        }
        return Optional.empty();
    }

    public static Optional<String> getChildText(Element e, String localName) {
        Optional<Element> child = getChild(e, localName);
        if (child.isPresent()) {
            String text = child.get().getTextContent();
            if (text != null && !text.isEmpty()) {
                return Optional.of(text);
            }
        }
        return Optional.empty();
    }

    public static String getChildValue(Element e, String localName) {
        Optional<Element> child = getChild(e, localName);
        return child.isPresent() ? child.get().getTextContent() : null;
    }

    public static List<String> getChildrenValues(Element e, String localName) {
        List<String> list = new ArrayList<>();
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
                String text = node.getTextContent();
                if (text != null && text.length() > 0) {
                    list.add(text);
                }
            }
            node = node.getNextSibling();
        }
        return list;
    }

}
