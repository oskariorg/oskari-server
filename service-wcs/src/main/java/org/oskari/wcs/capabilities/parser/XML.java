package org.oskari.wcs.capabilities.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XML {

    public static Optional<Element> getChild(Element e, String localName) {
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getNodeName())) {
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
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getNodeName())) {
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

    public static String getChildValue(Element e, String localName) {
        Optional<Element> child = getChild(e, localName);
        return child.isPresent() ? child.get().getTextContent() : null;
    }

    public static List<String> getChildrenValues(Element e, String localName) {
        List<String> list = new ArrayList<>();
        Node node = e.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getNodeName())) {
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
