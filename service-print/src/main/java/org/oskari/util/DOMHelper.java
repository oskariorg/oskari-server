package org.oskari.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility functions for with org.w3c.dom.Element
 */
public class DOMHelper {

    public static Element getFirst(Element parent, String tagName) {
        if (parent != null) {
            Node node = parent.getFirstChild();
            while (node != null) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (tagName.equals(node.getLocalName())) {
                        return (Element) node;
                    }
                }
                node = node.getNextSibling();
            }
        }
        return null;
    }

    public static List<Element> getAll(Element parent, String tagName) {
        List<Element> toReturn = new ArrayList<>();
        if (parent != null) {
            Node node = parent.getFirstChild();
            while (node != null) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (tagName.equals(node.getLocalName())) {
                        toReturn.add((Element) node);
                    }
                }
                node = node.getNextSibling();
            }
        }
        return toReturn;
    }

    public static int getInt(Element element, int defaultValue) {
        if (element != null) {
            String content = element.getTextContent();
            if (content != null && content.length() > 0) {
                try {
                    return Integer.parseInt(content);
                } catch (NumberFormatException ignore) {}
            }
        }
        return defaultValue;
    }

    public static double getDouble(Element element, double defaultValue) {
        if (element != null) {
            String content = element.getTextContent();
            if (content != null && content.length() > 0) {
                try {
                    return Double.parseDouble(content);
                } catch (NumberFormatException ignore) {}
            }
        }
        return defaultValue;
    }

}
