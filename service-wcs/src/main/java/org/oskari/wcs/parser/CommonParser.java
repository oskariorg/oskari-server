package org.oskari.wcs.parser;

import java.util.Optional;
import org.oskari.utils.common.StringUtils;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.gml.Envelope;
import org.oskari.wcs.gml.Point;
import org.w3c.dom.Element;

public class CommonParser {

    public static Envelope parseEnvelope(Element envelope) {
        String srsName = envelope.getAttribute("srsName");
        String tmp = envelope.getAttribute("srsDimension");
        if (tmp == null || tmp.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid Envelope, srsDimension shall not be empty");
        }
        final int srsDimension = Integer.parseInt(tmp);

        String[] axisLabels = null;
        tmp = envelope.getAttribute("axisLabels");
        if (tmp != null && !tmp.isEmpty()) {
            axisLabels = tmp.split(" ");
            if (axisLabels.length != srsDimension) {
                throw new IllegalArgumentException(
                        "Invalid Envelope, number of axisLabels differs from dimension");
            }
        }

        String[] uomLabels = null;
        tmp = envelope.getAttribute("uomLabels");
        if (tmp != null && !tmp.isEmpty()) {
            uomLabels = tmp.split(" ");
            if (uomLabels.length != srsDimension) {
                throw new IllegalArgumentException(
                        "Invalid Envelope, number of uomLabels differs from dimension");
            }
        }

        String lowerCornerStr = XML.getChildText(envelope, "lowerCorner").orElseThrow(
                () -> new IllegalArgumentException("Could not find lowerCorner from Envelope"));
        double[] lowerCorner = StringUtils.parseDoubleArray(lowerCornerStr, ' ').orElseThrow(
                () -> new IllegalArgumentException("Invalid Envelope,"
                        + " failed to parse doubleList from lowerCorner"));

        String upperCornerStr = XML.getChildText(envelope, "upperCorner").orElseThrow(
                () -> new IllegalArgumentException("Could not find lowerCorner from Envelope"));
        double[] upperCorner = StringUtils.parseDoubleArray(upperCornerStr, ' ').orElseThrow(
                () -> new IllegalArgumentException("Invalid Envelope,"
                        + " failed to parse doubleList from upperCorner"));

        return new Envelope(srsName, srsDimension, axisLabels, uomLabels, lowerCorner, upperCorner);
    }

    public static Optional<Integer> parseInt(String str) {
        if (str != null && !str.isEmpty()) {
            try {
                return Optional.of(Integer.parseInt(str));
            } catch (NumberFormatException ignore) {
            }
        }
        return Optional.empty();
    }

    public static Point parsePoint(Element elem, int dimension) {
        String srsName = elem.getAttribute("srsName");
        Element posE = XML.getChild(elem, "pos").orElseThrow(
                () -> new IllegalArgumentException("Invalid Point, missing pos element"));
        double[] pos = StringUtils.parseDoubleArray(posE.getTextContent(), ' ').orElseThrow(
                () -> new IllegalArgumentException("Invalid Point, failed to parse doubleList"));
        if (pos.length != dimension) {
            throw new IllegalArgumentException(
                    "Invalid Point, number of pos elements differ from dimension");
        }
        return new Point(srsName, dimension, pos);
    }
}
