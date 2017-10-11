package org.oskari.wcs.parser;

import java.util.List;
import java.util.Optional;
import org.oskari.utils.common.StringUtils;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.coverage.RectifiedGridCoverage;
import org.oskari.wcs.coverage.function.GridFunction;
import org.oskari.wcs.coverage.function.SequenceRule;
import org.oskari.wcs.gml.Envelope;
import org.oskari.wcs.gml.GridEnvelope;
import org.oskari.wcs.gml.Point;
import org.oskari.wcs.gml.RectifiedGrid;
import org.w3c.dom.Element;

public class RectifiedGridCoverageParser {

    public static RectifiedGridCoverage parse(Element coverageDescriptionE, String coverageId,
            Envelope boundedBy, String nativeFormat) throws IllegalArgumentException {
        Element domainSetE = XML.getChild(coverageDescriptionE, "domainSet")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find domainSet from CoverageDescription"));

        Element rectifiedGridE = XML.getChild(domainSetE, "RectifiedGrid")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find RectifiedGrid from domainSet"));

        RectifiedGrid domainSet = parseRectifiedGrid(rectifiedGridE);

        GridFunction gridFunction = parseGridFunction(
                XML.getChild(coverageDescriptionE, "coverageFunction"), domainSet);

        return new RectifiedGridCoverage(coverageId, boundedBy, nativeFormat, gridFunction,
                domainSet);
    }

    private static RectifiedGrid parseRectifiedGrid(Element rectifiedGridE) {
        int dimension = CommonParser.parseInt(rectifiedGridE.getAttribute("dimension"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid Rectified, invalid dimension value"));

        Element limitsE = XML.getChild(rectifiedGridE, "limits")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid RectifiedGrid, missing limits element"));
        Element gridEnvelopeE = XML.getChild(limitsE, "GridEnvelope")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid limits, missing GridEnvelope element"));
        GridEnvelope limits = parseGridEnvelope(gridEnvelopeE, dimension);

        String[] axes = parseAxisLabels(rectifiedGridE, dimension);
        Point origin = parseOrigin(rectifiedGridE, dimension);
        Point[] offsetVectors = parseOffsetVectors(rectifiedGridE, dimension);

        return new RectifiedGrid(limits, axes, dimension, origin, offsetVectors);
    }

    private static Point[] parseOffsetVectors(Element gridEnvelopeE, int dimension) {
        List<Element> offsetVectors = XML.getChildren(gridEnvelopeE, "offsetVector");
        Point[] offsetVectorArr = new Point[offsetVectors.size()];
        for (int i = 0; i < offsetVectorArr.length; i++) {
            offsetVectorArr[i] = parseOffsetVector(offsetVectors.get(i), dimension);
        }
        return offsetVectorArr;
    }

    private static Point parseOffsetVector(Element offsetVector, int dimension) {
        String srsName = offsetVector.getAttribute("srsName");
        double[] vector = StringUtils.parseDoubleArray(offsetVector.getTextContent(), ' ')
                .orElseThrow(() -> new IllegalArgumentException("Invalid offsetVector"));
        if (vector.length != dimension) {
            throw new IllegalArgumentException("offsetVector length differs from dimension");
        }
        return new Point(srsName, dimension, vector);
    }

    private static Point parseOrigin(Element gridEnvelopeE, int dimension) {
        Element originE = XML.getChild(gridEnvelopeE, "origin")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid RectifiedGrid, missing origin element"));
        Element pointE = XML.getChild(originE, "Point").orElseThrow(
                () -> new IllegalArgumentException("Invalid origin, missing Point element"));
        return CommonParser.parsePoint(pointE, dimension);
    }

    private static String[] parseAxisLabels(Element gridEnvelopeE, int dimension) {
        Optional<Element> axisLabelsE = XML.getChild(gridEnvelopeE, "axisLabels");
        if (axisLabelsE.isPresent()) {
            String labels = axisLabelsE.get().getTextContent();
            if (labels == null || labels.isEmpty()) {
                throw new IllegalArgumentException("axisLabels missing values");
            }
            String[] axisLabels = labels.split(" ");
            if (axisLabels.length != dimension) {
                throw new IllegalArgumentException("Number of axisLabels differs from dimension");
            }
            return axisLabels;
        }

        List<Element> axisNames = XML.getChildren(gridEnvelopeE, "axisName");
        if (axisNames.size() == 0) {
            throw new IllegalArgumentException(
                    "Could not find either axisLabel nor axisName elements");
        }
        if (axisNames.size() != dimension) {
            throw new IllegalArgumentException("Number of axisName elements differ from dimension");
        }

        String[] axisNameArr = new String[dimension];
        for (int i = 0; i < dimension; i++) {
            Element axisName = axisNames.get(i);
            String s = axisName.getTextContent();
            if (s == null || s.isEmpty()) {
                throw new IllegalArgumentException("Empty axisName");
            }
            axisNameArr[i] = s;
        }
        return axisNameArr;
    }

    private static GridEnvelope parseGridEnvelope(Element gridEnvelopeE, int dimension) {
        String lowStr = XML.getChildText(gridEnvelopeE, "low")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid GridEnvelope, missing low element"));
        int[] low = StringUtils.parseIntArray(lowStr, ' ')
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid low value in GridEnvelope"));
        if (low.length != dimension) {
            throw new IllegalArgumentException(
                    "GridEnvelope low length does not match dimension");
        }

        String highStr = XML.getChildText(gridEnvelopeE, "high")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid GridEnvelope, missing high element"));
        int[] high = StringUtils.parseIntArray(highStr, ' ')
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid high value in GridEnvelope"));
        if (high.length != dimension) {
            throw new IllegalArgumentException(
                    "GridEnvelope high length does not match dimension");
        }

        return new GridEnvelope(low, high);
    }

    private static GridFunction parseGridFunction(Optional<Element> coverageFunction,
            RectifiedGrid domainSet) {
        if (!coverageFunction.isPresent()) {
            // Return default values
            return new GridFunction(
                    GridFunction.DEFAULT_SEQUENCE_RULE,
                    domainSet.getLimits().getLow());
        }

        Element gridFunction = XML.getChild(coverageFunction.get(), "GridFunction")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find GridFunction from coverageFunction"));

        SequenceRule sequenceRule = parseSequenceRule(gridFunction);

        Optional<String> startPointStr = XML.getChildText(gridFunction, "startPoint");
        int[] startPoint;
        if (startPointStr.isPresent()) {
            startPoint = StringUtils.parseIntArray(startPointStr.get(), ' ')
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid GridFunction, invalid startPoint"));
        } else {
            startPoint = domainSet.getLimits().getLow();
        }

        return new GridFunction(sequenceRule, startPoint);
    }

    private static SequenceRule parseSequenceRule(Element gridFunction) {
        Optional<Element> sequenceRule = XML.getChild(gridFunction, "sequenceRule");
        if (!sequenceRule.isPresent()) {
            return GridFunction.DEFAULT_SEQUENCE_RULE;
        }

        Element elem = sequenceRule.get();

        String ruleStr = elem.getTextContent();
        SequenceRule.Rule rule;
        if (ruleStr == null || ruleStr.isEmpty()) {
            rule = SequenceRule.Rule.Linear;
        } else {
            rule = SequenceRule.Rule.valueOf(ruleStr.replace('-', '_'));
            if (rule == null) {
                throw new IllegalArgumentException("Unknown sequenceRule");
            }
        }

        String axisOrderStr = elem.getAttribute("axisOrder");
        int[] axisOrder;
        if (axisOrderStr != null && !axisOrderStr.isEmpty()) {
            axisOrder = StringUtils.parseIntArray(axisOrderStr, ' ')
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid sequenceRule, invalid axisOrder"));
        } else {
            axisOrder = SequenceRule.getDefaultAxisOrder();
        }

        return new SequenceRule(rule, axisOrder);
    }

}
