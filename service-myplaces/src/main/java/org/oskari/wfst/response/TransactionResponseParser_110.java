package org.oskari.wfst.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.oskari.utils.xml.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Parses WFS-T TransactionResponse
 */
public class TransactionResponseParser_110 {

    public static TransactionResponse_110 parse(byte[] b)
            throws ParserConfigurationException, SAXException, IOException {
        return parse(new ByteArrayInputStream(b));
    }

    public static TransactionResponse_110 parse(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        Document doc = XML.readDocument(in);
        Element transactionResponse = doc.getDocumentElement();
        if (!"TransactionResponse".equals(transactionResponse.getLocalName())) {
            throw new IllegalArgumentException("Root node must be 'TransactionResponse'");
        }
        if (!"1.1.0".equals(transactionResponse.getAttribute("version"))) {
            throw new IllegalArgumentException("Attribute 'version' must be '1.1.0'");
        }

        Element summary = XML.getChild(transactionResponse, "TransactionSummary")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing required element 'TransactionSummary'"));
        int totalInserted = getRequiredIntFromChild(summary, "totalInserted");
        int totalUpdated = getRequiredIntFromChild(summary, "totalUpdated");
        int totalDeleted = getRequiredIntFromChild(summary, "totalDeleted");

        List<InsertedFeature> insertedFeatures;
        Optional<Element> insertResults = XML.getChild(transactionResponse, "InsertResults");
        if (insertResults.isPresent()) {
            insertedFeatures = parseInsertedFeatures(insertResults.get());
        } else {
            insertedFeatures = Collections.emptyList();
        }

        return new TransactionResponse_110(totalInserted, totalUpdated,
                totalDeleted, insertedFeatures);
    }

    private static List<InsertedFeature> parseInsertedFeatures(Element insertResults) {
        List<Element> features = XML.getChildren(insertResults, "Feature");
        List<InsertedFeature> insertedFeatures = new ArrayList<>(features.size());
        for (int i = 0; i < features.size(); i++) {
            Element feature = features.get(i);
            String fid = XML.getChild(feature, "FeatureId")
                    .map(FeatureId -> FeatureId.getAttribute("fid"))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Expected ogc:FeatureId element inside Feature"));
            String handle = feature.getAttribute("handle");
            insertedFeatures.add(new InsertedFeature(fid, handle));
        }
        return insertedFeatures;
    }

    private static int getRequiredIntFromChild(Element parent, String element) {
        return XML.getChildText(parent, element)
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Missing required element '%s'", element)));
    }

}
